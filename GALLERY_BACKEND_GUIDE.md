# 画廊后端详细文档

## 一、环境配置

### 1.1 .env 环境变量配置

```properties
# ==================== 画廊配置 ====================

# 画廊域名配置（用于生成完整URL）
GALLERY_DOMAIN=oss.tilex.world
GALLERY_DOMAIN_PORT=80
# 完整访问域名：http://oss.tilex.world 或 http://oss.tilex.world:5244

# AList相关配置
ALIST_BASE_URL=http://localhost:5244
# 注意：开发环境使用localhost，生产环境部署时改为127.0.0.1或内网IP
# 因为后端和AList都在同一服务器，内部通信用本地地址提升性能

# AList上传接口URL（完整路径）
ALIST_UPLOAD_URL=http://oss.tilex.world:5244/api/fs/put
# 生产环境：http://127.0.0.1:5244/api/fs/put

# AList认证信息（可选，如果AList启用了身份认证）
ALIST_USERNAME=admin
ALIST_PASSWORD=admin123

# 画廊存储路径（在AList中的目录）
ALIST_STORAGE_PATH=/blog-images
# 实际存储在AList的路径格式：/p/blog-images/2026/03/20/
# /p 表示私有文件（仅认证用户可访问）
# /d 表示任意访问文件

# 图片生成的访问路径前缀（/p 或 /d）
ALIST_ACCESS_PREFIX=/d
# /p = Private，私有访问
# /d = Download/Direct，直接访问（推荐用于公开图片）

# 图片上传大小限制（MB）
MAX_UPLOAD_SIZE=50

# 每次批量操作的最大条数
BATCH_MAX_LIMIT=100
```

### 1.2 环境变量说明

| 变量 | 说明 | 开发环境值 | 生产环境值 |
|------|------|----------|----------|
| `GALLERY_DOMAIN` | 画廊访问域名 | localhost | oss.tilex.world |
| `ALIST_BASE_URL` | AList服务地址 | http://localhost:5244 | http://127.0.0.1:5244 |
| `ALIST_UPLOAD_URL` | AList上传接口 | http://localhost:5244/api/fs/put | http://127.0.0.1:5244/api/fs/put |
| `ALIST_STORAGE_PATH` | AList存储根目录 | /blog-images | /blog-images |
| `ALIST_ACCESS_PREFIX` | 访问前缀 | /d | /d |
| `MAX_UPLOAD_SIZE` | 单文件大小限制 | 50 | 50 |
| `BATCH_MAX_LIMIT` | 批量操作限制 | 100 | 100 |

### 1.3 Spring Boot 配置读取

```java
@Configuration
public class GalleryConfig {
    
    @Value("${gallery.domain:localhost}")
    private String galleryDomain;
    
    @Value("${gallery.domain.port:80}")
    private int galleryDomainPort;
    
    @Value("${alist.base-url:http://localhost:5244}")
    private String alistBaseUrl;
    
    @Value("${alist.upload-url}")
    private String alistUploadUrl;
    
    @Value("${alist.storage-path:/blog-images}")
    private String alistStoragePath;
    
    @Value("${alist.access-prefix:/d}")
    private String alistAccessPrefix;
    
    @Value("${max.upload-size:50}")
    private int maxUploadSize;
    
    @Value("${batch.max-limit:100}")
    private int batchMaxLimit;
    
    // Getters...
}
```

---

## 二、URL生成机制与AList集成

### 2.1 AList URL格式详解

```
完整URL示例：
http://oss.tilex.world/d/blog-images/2026/03/20/image-20250817180720236-1755485186694-1.png?sign=Cja4Ph5_ppeZ5nhjAApm5JXlWKMV7OHaUBfGX9aTQ6k=:0

结构分解：
├─ 协议 & 域名：http://oss.tilex.world
├─ 访问前缀：/d （或 /p）
│  └─ /d = Direct Access，普通用户可直接访问
│  └─ /p = Private，仅认证用户可访问
├─ 存储路径：/blog-images/2026/03/20/
│  └─ 基础路径：/blog-images （对应 ALIST_STORAGE_PATH）
│  └─ 日期分类：/2026/03/20/
│  └─ 格式：年份/月份/日期
├─ 文件名：image-20250817180720236-1755485186694-1.png
│  └─ 原始名称-时间戳-随机数.扩展名
└─ 签名参数：?sign=Cja4Ph5_ppeZ5nhjAApm5JXlWKMV7OHaUBfGX9aTQ6k=:0
   └─ 由AList生成，用于验证和防盗链
```

### 2.2 为什么存在 /p 和 /d

- **`/p` (Private)**：私有访问模式
  - 需要通过认证才能访问
  - 更好地保护敏感图片
  - 需要设置特殊的访问权限

- **`/d` (Direct/Download)**：直接访问模式
  - 任何人都可以直接访问
  - 适合公开的图片内容
  - 加速文件分发（可配合CDN）

**选择建议**：由于是画廊公开展示，推荐使用 `/d` 模式

### 2.3 后端上传流程图

```
用户上传文件
    ↓
后端接收文件（FormData）
    ↓
生成唯一文件名(时间戳+随机数)
    ↓
调用AList上传接口
    ↓
AList存储到指定目录
    ↓
AList返回生成的URL
│ 示例：http://alist-domain/d/blog-images/2026/03/20/filename.png?sign=xxx
├─ 后端解析URL
│  ├─ 提取 alist_path: /d/blog-images/2026/03/20/filename.png
│  ├─ 提取 alist_domain: alist-domain
│  └─ 检测 has_sign_param: true
├─ 保存到数据库
│  ├─ image_url: 完整URL
│  ├─ alist_path: 相对路径
│  ├─ alist_domain: 域名
│  └─ has_sign_param: 1/0
└─ 返回成功响应给前端，包含image_id
```

### 2.4 开发vs生产部署差异

#### 开发环境
```java
// .env
ALIST_BASE_URL=http://localhost:5244
ALIST_UPLOAD_URL=http://localhost:5244/api/fs/put
GALLERY_DOMAIN=localhost:8080

// 后端和AList都在本地运行
// 通过 localhost 访问AList提升开发效率
```

#### 生产环境
```java
// .env
ALIST_BASE_URL=http://127.0.0.1:5244
// 或内网IP: http://192.168.1.100:5244
ALIST_UPLOAD_URL=http://127.0.0.1:5244/api/fs/put
GALLERY_DOMAIN=oss.tilex.world

// 后端和AList都在同一服务器
// 使用127.0.0.1或内网IP进行内部通信
// 对外提供真实域名访问
```

---

## 三、字段权限与修改规则

### 3.1 字段权限矩阵

| 字段 | 创建时 | 更新时 | 说明 |
|------|--------|--------|------|
| `id` | 系统生成 | ✌️ 不可修改 | 主键，永远不可修改 |
| `title` | ✅ 可输入 | ✅ 可修改 | 用户可自定义标题 |
| `description` | ✅ 可输入 | ✅ 可修改 | 用户可自定义描述 |
| `image_url` | ❌ 系统生成 | ✌️ 不可修改 | AList返回，禁止修改 |
| `alist_path` | ❌ 系统生成 | ✌️ 不可修改 | 由后端解析生成，禁止修改 |
| `alist_domain` | ❌ 系统管理 | ⚠️ 限制修改 | 仅在域名迁移时修改，通常由系统管理员操作 |
| `source_type` | ❌ 系统生成 | ✌️ 不可修改 | 标记来源（alist/local/cdn） |
| `has_sign_param` | ❌ 系统生成 | ✌️ 不可修改 | 由系统自动检测 |
| `sort_order` | ✅ 可输入 | ✅ 可修改 | 用户可调整排序 |
| `created_at` | ❌ 系统生成 | ✌️ 不可修改 | 创建时间，永久记录 |
| `updated_at` | ❌ 系统生成 | ✌️ 不可修改 | 自动更新，不人为干预 |
| `is_deleted` | ✅ 默认0 | ✅ 可修改 | 软删除标识 |

### 3.2 字段修改规则实现

```java
// 上传接口 - 仅接收必要字段
@PostMapping("/upload")
public Result uploadImage(@RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) String title,
                         @RequestParam(required = false) String description) {
    // 只处理title和description
    // image_url、alist_path等由系统自动生成
}

// 更新接口 - 验证字段权限
@PutMapping("/{id}")
public Result updateImage(@PathVariable Long id, 
                         @RequestBody ImageUpdateRequest request) {
    // 允许修改的字段白名单
    Set<String> allowedFields = Set.of("title", "description", "sort_order");
    
    // 验证request中的字段
    // 如果包含禁止字段，返回400错误
}

// 数据库更新时 - 只更新允许的字段
public Result updateImage(Long id, ImageUpdateRequest request) {
    GalleryImage image = galleryImageRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Image not found"));
    
    // 仅更新允许的字段
    image.setTitle(request.getTitle());
    image.setDescription(request.getDescription());
    image.setSortOrder(request.getSortOrder());
    
    // 禁止这些字段被修改：
    // image.setImageUrl(...)  ✌️ NO
    // image.setAlistPath(...) ✌️ NO
    // image.setAlistDomain(...) ✌️ NO
    // image.setCreatedAt(...) ✌️ NO
    
    return galleryImageRepository.save(image);
}
```

### 3.3 alist_domain 管理策略

**方案选择：后端env管理（推荐）**

```java
// 方案1：完全由后端管理（推荐）
@Configuration
public class GalleryConfig {
    @Value("${alist.domain:oss.tilex.world}")
    private String alistDomain;
    
    // 对外提供接口获取当前域名
    public String getCurrentAlistDomain() {
        return alistDomain;
    }
}

// 前台生成URL时调用
public String buildImageUrl(GalleryImage image) {
    String domain = galleryConfig.getCurrentAlistDomain();
    return "http://" + domain + image.getAlistPath() + "?sign=...";
}

// 方案2：前端负责获取
@GetMapping("/config")
public Result getGalleryConfig() {
    return Result.ok(Map.of(
        "alistDomain", galleryConfig.getCurrentAlistDomain(),
        "maxUploadSize", galleryConfig.getMaxUploadSize()
    ));
}
// 前端从接口获取alist_domain后，拼接URL
```

**优势对比**：
| 方案 | 优点 | 缺点 |
|------|------|------|
| 后端管理 | 安全集中、易维护、不需前端参与、可灾难恢复 | 需要接口支持查询 |
| 前端管理 | 减轻后端压力、灵活性高 | 分散难维护、安全风险、前端改动需发布 |

**建议使用：后端env管理** ✅

---

## 四、核心后端接口设计

### 4.1 批量上传图片接口

**端点**：`POST /api/gallery/batch-upload`

**功能**：批量上传图片到AList，自动生成gallery_images记录

**请求**：

```
Content-Type: multipart/form-data

字段：
- files: File[] （多个文件，限制条数）
- titles: String[] （可选，对应文件的标题）
- descriptions: String[] （可选，对应文件的描述）
- tags: String[] （可选，JSON格式 ["tag1", "tag2"]）
```

**请求示例**：

```bash
curl -X POST http://localhost:8080/api/gallery/batch-upload \
  -F "files=@pic1.jpg" \
  -F "files=@pic2.jpg" \
  -F "titles=山水风景" \
  -F "titles=建筑夜景" \
  -F "descriptions=这是第一张图" \
  -F "descriptions=这是第二张图" \
  -F "tags=[\"风景\",\"建筑\"]"  # 所有图片共用这个标签
```

**接口实现**：

```java
@PostMapping("/batch-upload")
public Result batchUploadImages(
    @RequestParam("files") MultipartFile[] files,
    @RequestParam(value = "titles", required = false) String[] titles,
    @RequestParam(value = "descriptions", required = false) String[] descriptions,
    @RequestParam(value = "tags", required = false) String tagsJson,
    HttpServletRequest request) {
    
    // 1. 验证
    if (files == null || files.length == 0) {
        return Result.fail("未上传任何文件");
    }
    if (files.length > MAX_BATCH_LIMIT) {
        return Result.fail("单次最多上传" + MAX_BATCH_LIMIT + "张图片");
    }
    
    // 2. 处理标签
    List<Long> tagIds = new ArrayList<>();
    if (tagsJson != null && !tagsJson.isEmpty()) {
        List<String> tagNames = JSON.parseArray(tagsJson, String.class);
        tagIds = galleryTagService.getOrCreateTagIds(tagNames);
    }
    
    // 3. 批量上传到AList
    List<Long> imageIds = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    
    for (int i = 0; i < files.length; i++) {
        try {
            MultipartFile file = files[i];
            
            // 3.1 上传到AList
            String imageUrl = alistService.uploadFile(file);
            
            // 3.2 解析URL
            URLComponents components = parseAlistUrl(imageUrl);
            
            // 3.3 创建图片记录
            GalleryImage image = new GalleryImage();
            image.setTitle(titles != null ? titles[i] : file.getOriginalFilename());
            image.setDescription(descriptions != null ? descriptions[i] : "");
            image.setImageUrl(imageUrl);
            image.setAlistPath(components.getPath());
            image.setAlistDomain(components.getDomain());
            image.setSourceType("alist");
            image.setHasSignParam(components.hasSignParam() ? 1 : 0);
            image.setIsDeleted(0);
            
            GalleryImage savedImage = galleryImageRepository.save(image);
            imageIds.add(savedImage.getId());
            
            // 3.4 关联标签（如果有）
            if (!tagIds.isEmpty()) {
                for (Long tagId : tagIds) {
                    galleryImageTagRepository.save(
                        new GalleryImageTag(savedImage.getId(), tagId));
                }
            }
        } catch (Exception e) {
            errors.add("文件" + i + ": " + e.getMessage());
        }
    }
    
    // 4. 返回结果
    Map<String, Object> result = Map.of(
        "uploadedCount", imageIds.size(),
        "totalCount", files.length,
        "imageIds", imageIds,
        "errors", errors
    );
    
    return imageIds.isEmpty() 
        ? Result.fail(result, "全部上传失败")
        : Result.ok(result, "上传成功 " + imageIds.size() + "/" + files.length);
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "uploadedCount": 2,
    "totalCount": 2,
    "imageIds": [1, 2],
    "errors": []
  },
  "message": "上传成功 2/2"
}
```

**错误处理**：

```json
{
  "code": 400,
  "data": {
    "uploadedCount": 1,
    "totalCount": 2,
    "imageIds": [1],
    "errors": [
      "文件1: 文件过大，超过50MB限制"
    ]
  },
  "message": "上传成功 1/2"
}
```

---

### 4.2 批量更新图片信息接口

**端点**：`PUT /api/gallery/batch-update`

**功能**：批量修改图片的可修改字段（title、description、sort_order等）

**请求**：

```json
{
  "updates": [
    {
      "id": 1,
      "title": "新标题1",
      "description": "新描述1",
      "sort_order": 1
    },
    {
      "id": 2,
      "title": "新标题2",
      "description": "新描述2",
      "sort_order": 2
    }
  ]
}
```

**接口实现**：

```java
@PutMapping("/batch-update")
public Result batchUpdateImages(@RequestBody BatchUpdateRequest request) {
    
    if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
        return Result.fail("更新列表为空");
    }
    
    if (request.getUpdates().size() > MAX_BATCH_LIMIT) {
        return Result.fail("单次最多更新" + MAX_BATCH_LIMIT + "张图片");
    }
    
    int successCount = 0;
    List<String> errors = new ArrayList<>();
    
    for (ImageUpdateItem item : request.getUpdates()) {
        try {
            // 1. 查询图片
            GalleryImage image = galleryImageRepository.findById(item.getId())
                .orElseThrow(() -> new EntityNotFoundException("图片不存在"));
            
            // 2. 验证字段权限 - 只允许修改这些字段
            if (item.getTitle() != null) {
                image.setTitle(item.getTitle());
            }
            if (item.getDescription() != null) {
                image.setDescription(item.getDescription());
            }
            if (item.getSortOrder() != null) {
                image.setSortOrder(item.getSortOrder());
            }
            
            // 3. 禁止修改的字段检查
            // 通过反射或字段验证确保不修改：
            // imageUrl, alistPath, alistDomain, createdAt 等
            
            // 4. 保存
            galleryImageRepository.save(image);
            successCount++;
            
        } catch (Exception e) {
            errors.add("ID " + item.getId() + ": " + e.getMessage());
        }
    }
    
    return Result.ok(Map.of(
        "successCount", successCount,
        "totalCount", request.getUpdates().size(),
        "errors", errors
    ));
}
```

**Request DTO**：

```java
@Data
public class BatchUpdateRequest {
    private List<ImageUpdateItem> updates;
}

@Data
public class ImageUpdateItem {
    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
    
    // 无法访问以下字段（即使传入也会被忽略）
    @JsonIgnore
    private String imageUrl;
    
    @JsonIgnore
    private String alistPath;
    
    @JsonIgnore
    private String alistDomain;
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "successCount": 2,
    "totalCount": 2,
    "errors": []
  },
  "message": "成功"
}
```

---

### 4.3 批量添加标签接口

**端点**：`POST /api/gallery/tags/batch-create`

**功能**：批量创建新标签（如果不存在则创建）

**请求**：

```json
{
  "tags": [
    {
      "tagName": "风景",
      "tagDescription": "自然风景及美好风光",
      "colorCode": "#FF6B6B",
      "sortOrder": 1
    },
    {
      "tagName": "建筑",
      "tagDescription": "建筑与人工结构",
      "colorCode": "#4ECDC4",
      "sortOrder": 2
    }
  ]
}
```

**接口实现**：

```java
@PostMapping("/tags/batch-create")
public Result batchCreateTags(@RequestBody BatchCreateTagRequest request) {
    
    if (request.getTags() == null || request.getTags().isEmpty()) {
        return Result.fail("标签列表为空");
    }
    
    if (request.getTags().size() > MAX_BATCH_LIMIT) {
        return Result.fail("单次最多创建" + MAX_BATCH_LIMIT + "个标签");
    }
    
    List<GalleryTag> createdTags = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    int skipCount = 0;
    
    for (TagCreateItem item : request.getTags()) {
        try {
            // 1. 检查标签名是否已存在
            Optional<GalleryTag> existTag = 
                galleryTagRepository.findByTagNameAndIsDeleted(item.getTagName(), 0);
            
            if (existTag.isPresent()) {
                skipCount++;
                continue;
            }
            
            // 2. 创建新标签
            GalleryTag tag = new GalleryTag();
            tag.setTagName(item.getTagName());
            tag.setTagDescription(item.getTagDescription());
            tag.setColorCode(item.getColorCode() != null ? 
                item.getColorCode() : "#999999");
            tag.setSortOrder(item.getSortOrder() != null ? 
                item.getSortOrder() : 0);
            tag.setIsDeleted(0);
            
            GalleryTag savedTag = galleryTagRepository.save(tag);
            createdTags.add(savedTag);
            
        } catch (Exception e) {
            errors.add(item.getTagName() + ": " + e.getMessage());
        }
    }
    
    Map<String, Object> result = Map.of(
        "createdCount", createdTags.size(),
        "skippedCount", skipCount,
        "totalCount", request.getTags().size(),
        "createdTags", createdTags,
        "errors", errors
    );
    
    return Result.ok(result);
}
```

**Request DTO**：

```java
@Data
public class BatchCreateTagRequest {
    private List<TagCreateItem> tags;
}

@Data
public class TagCreateItem {
    @NotBlank(message = "标签名不能为空")
    private String tagName;
    
    private String tagDescription;
    
    @Pattern(regexp = "^#[0-9A-F]{6}$", message = "颜色码格式不正确")
    private String colorCode;
    
    private Integer sortOrder;
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "createdCount": 2,
    "skippedCount": 0,
    "totalCount": 2,
    "createdTags": [
      {
        "id": 1,
        "tagName": "风景",
        "tagDescription": "自然风景及美好风光",
        "colorCode": "#FF6B6B",
        "sortOrder": 1
      },
      {
        "id": 2,
        "tagName": "建筑",
        "tagDescription": "建筑与人工结构",
        "colorCode": "#4ECDC4",
        "sortOrder": 2
      }
    ],
    "errors": []
  }
}
```

---

### 4.4 批量图片-标签关联接口

**端点**：`POST /api/gallery/image-tags/batch-associate`

**功能**：批量为图片添加标签关联

**请求方式1：一图多标签**

```json
{
  "associations": [
    {
      "imageId": 1,
      "tagIds": [1, 2, 3]
    },
    {
      "imageId": 2,
      "tagIds": [2, 4]
    }
  ]
}
```

**请求方式2：多图一标签**

```json
{
  "imageIds": [1, 2, 3],
  "tagIds": [1, 2]
}
```

**接口实现**：

```java
@PostMapping("/image-tags/batch-associate")
public Result batchAssociateImageTags(
    @RequestBody BatchAssociateRequest request) {
    
    // 支持两种模式：associations模式 或 imageIds+tagIds模式
    List<ImageTagAssociation> associations = new ArrayList<>();
    
    if (request.getAssociations() != null && !request.getAssociations().isEmpty()) {
        // 模式1：associations
        associations = request.getAssociations();
    } else if (request.getImageIds() != null && request.getTagIds() != null) {
        // 模式2：imageIds + tagIds 笛卡尔积
        for (Long imageId : request.getImageIds()) {
            for (Long tagId : request.getTagIds()) {
                ImageTagAssociation assoc = new ImageTagAssociation();
                assoc.setImageId(imageId);
                assoc.setTagId(tagId);
                associations.add(assoc);
            }
        }
    } else {
        return Result.fail("必须提供associations或者imageIds+tagIds");
    }
    
    // 检查数量限制
    if (associations.size() > MAX_BATCH_LIMIT) {
        return Result.fail("关联数量过多，单次最多" + MAX_BATCH_LIMIT + "条");
    }
    
    int successCount = 0;
    List<String> errors = new ArrayList<>();
    
    for (ImageTagAssociation assoc : associations) {
        try {
            // 1. 验证图片和标签是否存在
            if (!galleryImageRepository.existsByIdAndIsDeleted(assoc.getImageId(), 0)) {
                throw new EntityNotFoundException("图片ID" + assoc.getImageId() + "不存在");
            }
            if (!galleryTagRepository.existsByIdAndIsDeleted(assoc.getTagId(), 0)) {
                throw new EntityNotFoundException("标签ID" + assoc.getTagId() + "不存在");
            }
            
            // 2. 检查关联是否已存在（UNIQUE约束）
            Optional<GalleryImageTag> existAssoc = 
                galleryImageTagRepository.findByImageIdAndTagId(
                    assoc.getImageId(), assoc.getTagId());
            
            if (existAssoc.isPresent()) {
                // 已存在，跳过
                continue;
            }
            
            // 3. 创建关联
            GalleryImageTag imageTag = new GalleryImageTag();
            imageTag.setImageId(assoc.getImageId());
            imageTag.setTagId(assoc.getTagId());
            
            galleryImageTagRepository.save(imageTag);
            successCount++;
            
        } catch (Exception e) {
            errors.add("图片:" + assoc.getImageId() + 
                      " 标签:" + assoc.getTagId() + " " + e.getMessage());
        }
    }
    
    return Result.ok(Map.of(
        "successCount", successCount,
        "totalCount", associations.size(),
        "errors", errors
    ));
}
```

**Request DTO**：

```java
@Data
public class BatchAssociateRequest {
    // 模式1：一图多标签
    private List<ImageTagAssociation> associations;
    
    // 模式2：多图多标签笛卡尔积
    private List<Long> imageIds;
    private List<Long> tagIds;
}

@Data
public class ImageTagAssociation {
    private Long imageId;
    private List<Long> tagIds;
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "successCount": 5,
    "totalCount": 6,
    "errors": [
      "图片:3 标签:1 关联已存在"
    ]
  },
  "message": "成功"
}
```

---

### 4.5 批量解除标签关联接口（补充）

**端点**：`DELETE /api/gallery/image-tags/batch-disassociate`

**功能**：批量移除图片的标签关联

**请求**：

```json
{
  "associations": [
    {
      "imageId": 1,
      "tagIds": [1, 2]
    }
  ]
}
```

**实现**：

```java
@DeleteMapping("/image-tags/batch-disassociate")
public Result batchDisassociateImageTags(
    @RequestBody BatchDisassociateRequest request) {
    
    int successCount = 0;
    List<String> errors = new ArrayList<>();
    
    for (ImageTagAssociation assoc : request.getAssociations()) {
        for (Long tagId : assoc.getTagIds()) {
            try {
                galleryImageTagRepository.deleteByImageIdAndTagId(
                    assoc.getImageId(), tagId);
                successCount++;
            } catch (Exception e) {
                errors.add("删除失败: " + e.getMessage());
            }
        }
    }
    
    return Result.ok(Map.of(
        "successCount", successCount,
        "errors", errors
    ));
}
```

---

## 五、AList集成服务

### 5.1 AList文件上传服务

```java
@Service
public class AlistService {
    
    @Value("${alist.upload-url}")
    private String alistUploadUrl;
    
    @Value("${alist.storage-path}")
    private String alistStoragePath;
    
    @Value("${alist.access-prefix}")
    private String alistAccessPrefix;
    
    @Resource
    private RestTemplate restTemplate;
    
    /**
     * 上传文件到AList
     * @param file 上传的文件
     * @return 完整的访问URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        
        // 1. 生成文件名（包含时间戳和随机数）
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = System.currentTimeMillis() + "";
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = originalFilename.substring(0, 
            originalFilename.lastIndexOf(".")) + "-" + timestamp + 
            "-" + randomStr + "." + extension;
        
        // 2. 构建存储路径（自动按日期分类）
        LocalDate today = LocalDate.now();
        String datePath = String.format("/%d/%02d/%02d/", 
            today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        String fullPath = alistStoragePath + datePath;
        
        // 3. 准备上传请求
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return newFilename;
            }
        });
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        // 4. 调用AList上传接口
        // AList API: PUT /api/fs/put?path=/blog-images/2026/03/20/
        String uploadUrl = alistUploadUrl + "?path=" + fullPath;
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.PUT,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 从AList响应中提取信息并构建完整URL
                return buildAccessUrl(fullPath, newFilename);
            } else {
                throw new RuntimeException("AList上传失败: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("AList上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建访问URL
     */
    private String buildAccessUrl(String path, String filename) {
        String domain = System.getenv("GALLERY_DOMAIN");
        String port = System.getenv("GALLERY_DOMAIN_PORT");
        
        String baseUrl = "http://" + domain;
        if (port != null && !port.equals("80") && !port.equals("443")) {
            baseUrl += ":" + port;
        }
        
        return baseUrl + alistAccessPrefix + path + filename;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "bin";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1) : "bin";
    }
}
```

### 5.2 URL解析工具

```java
@Component
public class UrlParserUtil {
    
    /**
     * 解析AList返回的URL，提取关键信息
     */
    public static URLComponents parseAlistUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            
            // 提取域名
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();
            String domain = host;
            if (port != -1 && port != 80 && port != 443) {
                domain += ":" + port;
            }
            
            // 提取路径（/d/... 或 /p/...）
            String path = parsedUrl.getPath();
            
            // 检查是否有查询参数（签名）
            String query = parsedUrl.getQuery();
            boolean hasSign = query != null && query.contains("sign=");
            
            return new URLComponents(domain, path, hasSign);
            
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL格式不正确: " + url);
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class URLComponents {
        private String domain;      // oss.tilex.world
        private String path;        // /d/blog-images/2026/03/20/filename.png
        private boolean hasSign;    // true/false
    }
}
```

---

## 六、错误处理与验证

### 6.1 通用错误响应格式

```java
@Data
public class Result<T> {
    private int code;           // 0=成功, 非0=失败
    private T data;
    private String message;
    private long timestamp;
    
    public static <T> Result<T> ok() {
        return ok(null, "成功");
    }
    
    public static <T> Result<T> ok(T data) {
        return ok(data, "成功");
    }
    
    public static <T> Result<T> ok(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.data = data;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    public static <T> Result<T> fail(String message) {
        return fail(null, message);
    }
    
    public static <T> Result<T> fail(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.data = data;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
}
```

### 6.2 文件验证

```java
@Component
public class FileValidator {
    
    private static final List<String> ALLOWED_EXTENSIONS = 
        Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp");
    
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    public void validate(MultipartFile file) throws FileSizeLimitExceededException {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeLimitExceededException(
                "文件过大，限制50MB");
        }
        
        // 检查文件类型
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                "不支持的文件类型: " + extension);
        }
    }
    
    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
}
```

---

## 七、开发部署检查清单

### 7.1 开发环境检查

```
环境变量配置
├─ [ ] ALIST_BASE_URL = http://localhost:5244
├─ [ ] ALIST_UPLOAD_URL = http://localhost:5244/api/fs/put
├─ [ ] GALLERY_DOMAIN = localhost:8080
└─ [ ] ALIST_ACCESS_PREFIX = /d

依赖服务
├─ [ ] AList服务运行在5244端口
├─ [ ] MySQL数据库连接正常
├─ [ ] Redis缓存（可选）正常
└─ [ ] minio/对象存储（可选）正常

数据库初始化
├─ [ ] gallery_images表
├─ [ ] gallery_tags表
└─ [ ] gallery_image_tags表

功能测试
├─ [ ] 单文件上传
├─ [ ] 批量上传
├─ [ ] 图片查询
├─ [ ] 标签管理
└─ [ ] 关联关系
```

### 7.2 生产环境部署

**环境变量更新**：

```properties
# 改为内网IP或127.0.0.1
ALIST_BASE_URL=http://127.0.0.1:5244
ALIST_UPLOAD_URL=http://127.0.0.1:5244/api/fs/put

# 改为真实域名
GALLERY_DOMAIN=oss.tilex.world
GALLERY_DOMAIN_PORT=80

# 其他优化
MAX_UPLOAD_SIZE=100
BATCH_MAX_LIMIT=50
```

**性能优化**：

```java
// 添加缓存配置
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "gallery_images",
            "gallery_tags",
            "gallery_config"
        );
    }
}

// 缓存标签列表
@Cacheable(value = "gallery_tags")
public List<GalleryTag> getAllTags() {
    return galleryTagRepository.findAll();
}
```

---

## 八、常见问题

### Q1: 开发时如何快速测试上传功能？

**A**: 使用以下工具：
```bash
# curl 上传单个文件
curl -X POST http://localhost:8080/api/gallery/batch-upload \
  -F "files=@test.jpg"

# Postman：选择form-data，key为files，value为file类型
```

### Q2: 生产部署时，后端和AList通信用什么地址？

**A**: 
- 同一服务器：使用 `127.0.0.1:5244` 或内网IP
- 不同服务器：使用AList所在服务器的IP或域名
- 优先级：127.0.0.1 > 内网IP > 域名

### Q3: alist_domain 字段何时修改？

**A**: 仅在以下情况修改：
- 服务器迁移，AList域名改变
- 需要灾难恢复、切换域名
- 由系统管理员操作，**禁止前端用户修改**

### Q4: 为什么上传的URL中包含签名参数？

**A**: 签名参数 (`?sign=xxx`) 用于：
- 防盗链验证
- 访问权限控制
- URL过期时间管理
- AList内部安全机制

### Q5: 如何处理大文件上传？

**A**:
```java
// 配置分片上传
@PostMapping("/chunk-upload")
public Result chunkUpload(
    @RequestParam String uploadId,
    @RequestParam int chunkNumber,
    @RequestParam int totalChunks,
    @RequestParam MultipartFile chunk) {
    // 实现分片上传逻辑
}
```

---

**文档版本**：v1.0  
**最后更新**：2026年3月23日  
**维护部门**：后端开发团队
