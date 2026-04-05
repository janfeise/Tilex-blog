package blog.tilex.backend.Tilex_blog_backend.service.gallery;

import blog.tilex.backend.Tilex_blog_backend.config.GalleryConfig;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.URLComponents;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;


/**
 * AList 整合服务
 * 与 AList 服务的所有通信
 * 与业务逻辑解耦
 */
@Service
@Slf4j
public class AlistService {

    private final GalleryConfig galleryConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String alistToken;
    private long tokenExpireTime = 0;
    
    /**
     * 递归深度控制 - 防止无限递归
     * 用于uploadFile和getSignedUrl中的登录重试
     */
    private final ThreadLocal<Integer> uploadRetryCount = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<Integer> signedUrlRetryCount = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_RETRY_COUNT = 1; // 最多只重试登录一次

    public AlistService(GalleryConfig galleryConfig, RestTemplate restTemplate) {
        this.galleryConfig = galleryConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * 上传文件到 AList
     * 
     * @param file 要上传的文件
     * @return 完整的访问 URL
     * @throws IOException 如果文件无法读取
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("Starting upload file: {}", file.getOriginalFilename());
        
        try {
            // 重置重试计数器
            uploadRetryCount.set(0);
            
            return uploadFileInternal(file, 0);
        } finally {
            // 清理ThreadLocal资源
            uploadRetryCount.remove();
        }
    }
    
    /**
     * 内部上传方法，支持有限次数的重试
     * @param file 要上传的文件
     * @param retryCount 当前重试次数
     */
    private String uploadFileInternal(MultipartFile file, int retryCount) throws IOException {
        // 确保有有效的token
        ensureToken();

        // 1. 生成新文件名称(有时间戳和随机后缀)
        String newFilename = generateFilename(file.getOriginalFilename());

        // 2. 构建存储路径(按日期自动分类)
        String fullPath = buildStoragePath(newFilename);
        log.info("Full path for upload: {}", fullPath);

        // 3. 准备上传请求 - 使用流式上传with headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        // 使用token认证
        if (alistToken != null && !alistToken.isEmpty()) {
            headers.set("Authorization", alistToken);
            log.debug("Using Authorization token for upload");
        }
        
        // File-Path必须放在header中
        try {
            String encodedPath = encodeUrlPath(fullPath);
            headers.set("File-Path", encodedPath);
            log.info("Set File-Path header: {}", encodedPath);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding File-Path: {}", e.getMessage());
            throw new IOException("Failed to encode file path", e);
        }
        
        // 使用流式方式上传，避免一次性加载整个文件到内存
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        
        HttpEntity<ByteArrayResource> requestEntity = new HttpEntity<>(fileResource, headers);

        // 4. 调用 AList 上传 API
        String uploadUrl = buildUploadUrl();
        log.info("Upload URL: {}", uploadUrl);
        log.debug("Upload headers: {}", headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            String responseBody = response.getBody();
            log.info("AList response status: {}, body: {}", response.getStatusCode(), responseBody);

            // 检查HTTP状态码
            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                String errorMsg = "AList upload failed with status: " + response.getStatusCode();
                log.error(errorMsg + ", response: {}", responseBody);
                throw new RuntimeException(errorMsg);
            }

            // 验证AList响应
            if (!isAlistResponseSuccess(responseBody)) {
                // 如果返回认证错误且还有重试次数，尝试重新登录
                if (isAuthenticationError(responseBody) && retryCount < MAX_RETRY_COUNT) {
                    log.warn("Authentication error detected, attempting to re-login (retry count: {})", retryCount + 1);
                    if (login()) {
                        // 使用有限的重试，不是无限递归
                        return uploadFileInternal(file, retryCount + 1);
                    }
                }
                String errorMsg = "AList upload failed: " + responseBody;
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            // 获取签名 URL
            String accessUrl = getSignedUrlInternal(fullPath, newFilename, 0);
            log.info("Upload successful, URL: {}", accessUrl);
            return accessUrl;

        } catch (HttpClientErrorException e) {
            log.error("AList upload HTTP error: {} - {}", e.getStatusCode(), e.getMessage());
            log.error("Response body: {}", e.getResponseBodyAsString());
            
            // 如果是401/403认证错误且还有重试次数，尝试重新登录
            if ((e.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                 e.getStatusCode() == HttpStatus.FORBIDDEN) && retryCount < MAX_RETRY_COUNT) {
                log.warn("Authentication error ({}), attempting to re-login (retry count: {})", 
                    e.getStatusCode(), retryCount + 1);
                if (login()) {
                    return uploadFileInternal(file, retryCount + 1);
                }
            }
            throw new RuntimeException("AList upload failed: " + e.getMessage());
        }
    }

    /**
     * 确保有有效的token，如果需要则自动登录
     */
    private synchronized void ensureToken() {
        if (shouldRefreshToken()) {
            if (!login()) {
                log.error("Failed to obtain valid token from AList");
                throw new RuntimeException("Failed to authenticate with AList");
            }
        }
    }

    /**
     * 检查token是否需要刷新
     */
    private boolean shouldRefreshToken() {
        return alistToken == null || alistToken.isEmpty() || 
               System.currentTimeMillis() >= tokenExpireTime;
    }

    /**
     * 登录到AList并获取token
     * 
     * @return 登录是否成功
     */
    public boolean login() {
        try {
            log.info("Attempting to login to AList");
            
            String loginUrl = galleryConfig.getAlistBaseUrl() + "/api/auth/login";
            
            Map<String, String> loginRequest = new LinkedHashMap<>();
            loginRequest.put("username", galleryConfig.getAlistUsername());
            loginRequest.put("password", galleryConfig.getAlistPassword());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            String responseBody = response.getBody();
            log.debug("AList login response: {}", responseBody);
            
            // 解析响应并提取token
            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    
                    // 检查响应状态
                    if (jsonNode.has("code") && jsonNode.get("code").asInt() == 200) {
                        if (jsonNode.has("data") && jsonNode.get("data").has("token")) {
                            String token = jsonNode.get("data").get("token").asText();
                            if (token != null && !token.isEmpty()) {
                                this.alistToken = token;
                                // 设置token过期时间(默认7天，可以根据AList实际配置调整)
                                this.tokenExpireTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
                                log.info("Successfully obtained AList token");
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error parsing login response: {}", e.getMessage());
                }
            }
            
            log.error("Failed to login to AList: invalid response");
            return false;
            
        } catch (Exception e) {
            log.error("AList login failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查AList API响应是否成功
     * AList通常返回 {"code": 200, "message": "...", "data": ...}
     */
    private boolean isAlistResponseSuccess(String responseBody) {
        try {
            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("Empty response from AList");
                return false;
            }
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 检查code字段
            if (jsonNode.has("code")) {
                int code = jsonNode.get("code").asInt();
                if (code == 200) {
                    return true;
                }
                log.warn("AList returned error code: {}", code);
                return false;
            }
            
            // 如果没有code字段，假设成功（向后兼容）
            log.debug("No 'code' field in AList response, assuming success");
            return true;
            
        } catch (Exception e) {
            log.error("Error parsing AList response: {}", e.getMessage());
            // 如果无法解析为JSON，可能是上传成功了（AList有时返回非JSON)
            return false;
        }
    }

    /**
     * 检查响应是否表示认证错误
     */
    private boolean isAuthenticationError(String responseBody) {
        try {
            if (responseBody == null || responseBody.isEmpty()) {
                return false;
            }
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (jsonNode.has("code")) {
                int code = jsonNode.get("code").asInt();
                // 常见的认证错误代码
                return code == 401 || code == 403 || code == 400;
            }
            
            if (jsonNode.has("message")) {
                String message = jsonNode.get("message").asText().toLowerCase();
                return message.contains("unauthorized") || 
                       message.contains("authentication") ||
                       message.contains("token") ||
                       message.contains("forbidden");
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 测试与AList的连接和存储配置
     * 用于诊断"storage not found"错误
     */
    public String testAlistConnection() {
        StringBuilder report = new StringBuilder();
        
        try {
            // 1. 测试基本连接
            report.append("=== AList Connection Test ===\n");
            report.append("Base URL: ").append(galleryConfig.getAlistBaseUrl()).append("\n");
            report.append("Storage Path: ").append(galleryConfig.getAlistStoragePath()).append("\n");
            report.append("Upload URL: ").append(galleryConfig.getAlistUploadUrl()).append("\n");
            
            // 2. 测试登录
            report.append("\n--- Testing Login ---\n");
            if (login()) {
                report.append("✓ Login successful\n");
                report.append("Token: ").append(alistToken.substring(0, Math.min(20, alistToken.length()))).append("...\n");
            } else {
                report.append("✗ Login failed\n");
                return report.toString();
            }
            
            // 3. 测试基本路径
            report.append("\n--- Testing Storage Paths ---\n");
            
            // 尝试调用 /api/fs/get 来验证storage
            String[] testPaths = {
                "/blog-images",
                "/blog-images/",
                "/p/blog-images",
                "/d/blog-images"
            };
            
            for (String testPath : testPaths) {
                try {
                    String getUrl = galleryConfig.getAlistBaseUrl() + "/api/fs/get";
                    
                    Map<String, String> requestBody = new LinkedHashMap<>();
                    requestBody.put("path", testPath);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", alistToken);
                    
                    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        getUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                    );
                    
                    String responseBody = response.getBody();
                    if (isAlistResponseSuccess(responseBody)) {
                        report.append("✓ Path '").append(testPath).append("' exists\n");
                    } else {
                        report.append("✗ Path '").append(testPath).append("' error: ").append(responseBody).append("\n");
                    }
                } catch (Exception e) {
                    report.append("✗ Path '").append(testPath).append("' error: ").append(e.getMessage()).append("\n");
                }
            }
            
            report.append("\n--- Recommendation ---\n");
            report.append("If all paths fail, check:\n");
            report.append("1. Is the storage 'blog-images' created in AList?\n");
            report.append("2. What is the mount path of this storage? (usually /blog-images or /d/blog-images)\n");
            report.append("3. Check AList admin panel -> Storages for the exact path\n");
            
        } catch (Exception e) {
            report.append("Test failed: ").append(e.getMessage());
        }
        
        return report.toString();
    }

    /**
     * 解析 AList URL 与提取组件
     * 
     * @param url 来自 AList 的完整 URL
     * @return URL 组件包含域名、住沿路段和 赳旗穗指示
     */
    public URLComponents parseUrl(String url) {
        try {
            URL parsedUrl = new URL(url);

            // 提取域名和端口
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();
            String domain = host;
            if (port != -1 && port != 80 && port != 443) {
                domain += ":" + port;
            }

            // 提取路径(/d/... 或 /p/...)
            String path = parsedUrl.getPath();

            // 检查查询参数(三列印)
            String query = parsedUrl.getQuery();
            boolean hasSign = query != null && query.contains("sign=");

            log.info("URL parsed - domain: {}, path: {}, hasSign: {}", domain, path, hasSign);
            return new URLComponents(domain, path, hasSign);

        } catch (MalformedURLException e) {
            log.error("Invalid URL format: {}", url);
            throw new RuntimeException("Invalid URL format: " + url);
        }
    }

    /**
     * 生成带有时间戳和随机后缀的唯一文件名称
     * 仅保留时间戳+随机数，移除原始文件名以避免中文名称问题
     */
    private String generateFilename(String originalFilename) {
        if (originalFilename == null) {
            originalFilename = "image.bin";
        }

        String extension = getFileExtension(originalFilename);
        
        // 仅使用时间戳+随机数生成文件名，确保唯一性
        String timestamp = System.currentTimeMillis() + "";
        String randomStr = UUID.randomUUID().toString().substring(0, 8);

        String newFilename = timestamp + "-" + randomStr + "." + extension;
        log.info("Generated filename (timestamp+random): {}", newFilename);
        return newFilename;
    }

    /**
     * 构建带日期分类的完整存储路径
     */
    private String buildStoragePath(String filename) {
        LocalDate today = LocalDate.now();
        String datePath = String.format("/%d/%02d/%02d/",
            today.getYear(), today.getMonthValue(), today.getDayOfMonth());

        String fullPath = galleryConfig.getAlistStoragePath() + datePath + filename;
        log.debug("Storage path: {}", fullPath);
        return fullPath;
    }

    /**
     * 构建 AList 上传 API URL
     * 注意：根据Alist官方文档，文件路径应放在请求头(File-Path)中，而不是URL参数
     */
    private String buildUploadUrl() {
        // 只返回基础URL，路径参数将通过File-Path header传递
        String uploadUrl = galleryConfig.getAlistUploadUrl();
        log.debug("AList upload URL: {}", uploadUrl);
        return uploadUrl;
    }

    /**
     * 对URL路径进行编码，保留斜杠
     * 例如：/blog-images/2026/03/23/file【中文】.png 
     * 编码为：/blog-images/2026/03/23/file%E3%80%90...%E3%80%91.png
     */
    private String encodeUrlPath(String path) throws UnsupportedEncodingException {
        if (path == null || path.isEmpty()) {
            return path;
        }
        
        // 先分割路径（保留斜杠），然后编码每个部分
        String[] parts = path.split("/");
        StringBuilder encoded = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                encoded.append("/");
            }
            // 只编码非空的部分
            if (!parts[i].isEmpty()) {
                encoded.append(URLEncoder.encode(parts[i], "UTF-8"));
            }
        }
        
        return encoded.toString();
    }

    /**
     * 构建图片的完整访问 URL
     */
    private String buildAccessUrl(String fullPath, String filename) {
        String baseUrl = galleryConfig.getCompleteDomain();
        String accessUrl = baseUrl + galleryConfig.getAlistAccessPrefix() + fullPath;
        log.debug("Access URL: {}", accessUrl);
        return accessUrl;
    }

    /**
     * 从 AList 获取签名 URL (公开方法)
     * 使用Token认证调用 /api/fs/get 接口，获取已签名的可访问URL
     */
    private String getSignedUrl(String fullPath, String filename) {
        try {
            signedUrlRetryCount.set(0);
            return getSignedUrlInternal(fullPath, filename, 0);
        } finally {
            signedUrlRetryCount.remove();
        }
    }
    
    /**
     * 从 AList 获取签名 URL (内部方法，支持有限重试)
     * 使用Token认证调用 /api/fs/get 接口，获取已签名的可访问URL
     * 这样URL不会包含密码，且携带有效的签名
     */
    private String getSignedUrlInternal(String fullPath, String filename, int retryCount) {
        try {
            // 确保有有效的token
            ensureToken();
            
            // 调用 AList /api/fs/get 接口获取签名 URL
            String getUrl = galleryConfig.getAlistBaseUrl() + "/api/fs/get";
            
            // 准备请求
            Map<String, String> requestBody = new LinkedHashMap<>();
            requestBody.put("path", fullPath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 使用Token认证
            if (alistToken != null && !alistToken.isEmpty()) {
                headers.set("Authorization", alistToken);
                log.debug("Using Bearer token for /api/fs/get request");
            }
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    getUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
                );
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String responseBody = response.getBody();
                    log.debug("AList /api/fs/get response: {}", responseBody);
                    
                    // 使用ObjectMapper解析JSON
                    try {
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        
                        // AList响应格式检查
                        if (jsonNode.has("code") && jsonNode.get("code").asInt() == 200) {
                            if (jsonNode.has("data")) {
                                JsonNode data = jsonNode.get("data");
                                
                                // 优先获取raw_url (原始URL，适合直接访问)
                                if (data.has("raw_url") && data.get("raw_url") != null) {
                                    String rawUrl = data.get("raw_url").asText();
                                    if (rawUrl != null && !rawUrl.isEmpty()) {
                                        log.info("Got signed raw_url from AList: {}", rawUrl);
                                        return rawUrl;
                                    }
                                }
                                
                                // 其次尝试url字段
                                if (data.has("url") && data.get("url") != null) {
                                    String url = data.get("url").asText();
                                    if (url != null && !url.isEmpty()) {
                                        log.info("Got signed url from AList: {}", url);
                                        return url;
                                    }
                                }
                            }
                        } else {
                            log.warn("AList returned error: code={}, message={}", 
                                jsonNode.has("code") ? jsonNode.get("code").asInt() : "unknown",
                                jsonNode.has("message") ? jsonNode.get("message").asText() : "no message");
                        }
                    } catch (Exception parseError) {
                        log.warn("Failed to parse AList response: {}", parseError.getMessage());
                    }
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP error getting signed URL: {} - {}", e.getStatusCode(), e.getMessage());
                // 如果是认证错误且还有重试次数，尝试重新登录
                if ((e.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                     e.getStatusCode() == HttpStatus.FORBIDDEN) && retryCount < MAX_RETRY_COUNT) {
                    log.warn("Authentication error ({}), attempting to re-login (retry count: {})", 
                        e.getStatusCode(), retryCount + 1);
                    if (login()) {
                        // 使用有限的重试，不是无限递归
                        return getSignedUrlInternal(fullPath, filename, retryCount + 1);
                    }
                }
                throw e;
            } catch (Exception e) {
                log.error("Failed to get signed URL via /api/fs/get: {}", e.getMessage());
            }
            
            // 如果所有方案都失败，抛出异常而不是返回无效URL
            throw new RuntimeException("Failed to obtain signed URL from AList for path: " + fullPath);
            
        } catch (Exception e) {
            log.error("Error getting signed URL: {}", e.getMessage());
            throw new RuntimeException("Failed to get signed URL from AList: " + e.getMessage(), e);
        }
    }

    /**
     * 提取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "bin";
        }
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1) : "bin";
    }

    /**
     * 获取最大上传大小 MB
     */
    public int getMaxUploadSizeMb() {
        return galleryConfig.getMaxUploadSize();
    }

    /**
     * 获取最大批量操作限制
     */
    public int getBatchMaxLimit() {
        return galleryConfig.getBatchMaxLimit();
    }
}
