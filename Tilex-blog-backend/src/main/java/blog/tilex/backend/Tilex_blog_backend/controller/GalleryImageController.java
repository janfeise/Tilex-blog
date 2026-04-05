package blog.tilex.backend.Tilex_blog_backend.controller;

import blog.tilex.backend.Tilex_blog_backend.dto.common.Result;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.*;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryImage;
import blog.tilex.backend.Tilex_blog_backend.service.gallery.AlistService;
import blog.tilex.backend.Tilex_blog_backend.service.gallery.GalleryImageService;
import blog.tilex.backend.Tilex_blog_backend.service.gallery.GalleryImageTagService;
import blog.tilex.backend.Tilex_blog_backend.utils.gallery.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 画庶图片 Controller
 * 画庶图片操作的 REST API 端点
 */
@RestController
@RequestMapping("/gallery")
@Slf4j
public class GalleryImageController {

    private final GalleryImageService galleryImageService;
    private final GalleryImageTagService galleryImageTagService;
    private final AlistService alistService;
    private final FileValidator fileValidator;

    public GalleryImageController(GalleryImageService galleryImageService,
                                GalleryImageTagService galleryImageTagService,
                                AlistService alistService,
                                FileValidator fileValidator) {
        this.galleryImageService = galleryImageService;
        this.galleryImageTagService = galleryImageTagService;
        this.alistService = alistService;
        this.fileValidator = fileValidator;
    }

    /**
     * 批量接受图片到 AList
     * 
     * @param files 要上传的多个文件
     * @param titles 每个嚾片的可选标题
     * @param descriptions 每个嚾片的可选描述
     * @param tagsJson 可选 JSON 格式标签
     * @return 上载结果与嚾片 ID
     */
    @PostMapping("/batch-upload")
    public Result<Map<String, Object>> batchUploadImages(
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(value = "titles", required = false) String[] titles,
        @RequestParam(value = "descriptions", required = false) String[] descriptions,
        @RequestParam(value = "tags", required = false) String tagsJson) {

        log.info("Starting batch upload: {} files", files.length);

        // 验证
        if (files == null || files.length == 0) {
            return Result.fail("No files uploaded");
        }

        if (files.length > alistService.getBatchMaxLimit()) {
            return Result.fail("Maximum " + alistService.getBatchMaxLimit() +
                " files per upload");
        }

        // 第一阶段：验证并上传所有文件到AList，不保存到数据库
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            try {
                MultipartFile file = files[i];

                // 验证文件
                fileValidator.validate(file);

                // 上载到 AList
                log.debug("Uploading file {} to AList: {}", i, file.getOriginalFilename());
                String imageUrl = alistService.uploadFile(file);

                // 解析 URL
                URLComponents components = alistService.parseUrl(imageUrl);

                // 保存上传信息到临时List中
                Map<String, Object> uploadInfo = new LinkedHashMap<>();
                uploadInfo.put("imageUrl", imageUrl);
                uploadInfo.put("components", components);
                uploadInfo.put("title", titles != null && i < titles.length ? 
                    titles[i] : file.getOriginalFilename());
                uploadInfo.put("description", descriptions != null && i < descriptions.length ? 
                    descriptions[i] : "");

                uploadedFiles.add(uploadInfo);
                log.debug("File {} uploaded to AList successfully", i);

            } catch (Exception e) {
                String errorMsg = "File " + i + " (" + 
                    (files[i] != null ? files[i].getOriginalFilename() : "unknown") + 
                    "): " + e.getMessage();
                errors.add(errorMsg);
                log.error("Error uploading file {}: {}", i, e.getMessage());
            }
        }

        // 如果有任何文件上传失败，返回错误，避免数据库与AList不一致
        if (!errors.isEmpty() && uploadedFiles.size() != files.length) {
            log.warn("Some files failed to upload to AList, aborting batch operation");
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uploadedCount", uploadedFiles.size());
            data.put("totalCount", files.length);
            data.put("errors", errors);
            data.put("message", "Upload failed, no files were saved to database (consistency maintained)");
            return Result.fail(data, "Batch upload failed due to errors in AList upload");
        }

        // 第二阶段：如果所有文件都成功上传到AList，则保存到数据库
        List<Long> imageIds = new ArrayList<>();
        
        try {
            for (int i = 0; i < uploadedFiles.size(); i++) {
                Map<String, Object> uploadInfo = uploadedFiles.get(i);
                URLComponents components = (URLComponents) uploadInfo.get("components");

                // 创建图库图片实体
                GalleryImage image = new GalleryImage();
                image.setTitle((String) uploadInfo.get("title"));
                image.setDescription((String) uploadInfo.get("description"));
                image.setImageUrl((String) uploadInfo.get("imageUrl"));
                image.setAlistPath(components.getPath());
                image.setAlistDomain(components.getDomain());
                image.setSourceType("alist");
                image.setHasSignParam(components.isHasSign() ? 1 : 0);
                image.setIsDeleted(0);

                // 保存到数据库
                GalleryImage savedImage = galleryImageService.saveImage(image);
                imageIds.add(savedImage.getId());

                log.debug("Image saved to database: {}", savedImage.getId());
            }
        } catch (Exception e) {
            log.error("Error saving images to database: {}", e.getMessage());
            // 注意：此时文件已经上传到AList，但数据库保存失败
            // 这是不可恢复的情况，需要手动清理或重试
            errors.add("Database save failed: " + e.getMessage());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uploadedCount", imageIds.size());
        data.put("totalCount", files.length);
        data.put("imageIds", imageIds);
        data.put("errors", errors);

        String message = imageIds.isEmpty() ? 
            "All uploads failed" : 
            ("Successfully uploaded " + imageIds.size() + "/" + files.length);

        return imageIds.isEmpty() ? Result.fail(data, message) : Result.ok(data, message);
    }

    /**
     * 批量更新嚾片信息
     * 仅藁准修改：title, description, sort_order
     * 
     * @param request 批量更新请求
     * @return 更新结果
     */
    @PutMapping("/batch-update")
    public Result<Map<String, Object>> batchUpdateImages(
        @RequestBody BatchUpdateRequest request) {

        log.info("Starting batch update");

        try {
            Map<String, Object> result = galleryImageService.batchUpdateImages(request);
            return Result.ok(result, "Batch update completed");
        } catch (Exception e) {
            log.error("Error in batch update: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取所有嚾片
     * 
     * @return 所有主动嚾片的列表
     */
    @GetMapping("/images")
    public Result<Map<String, Object>> getAllImages() {
        log.info("Fetching all images");

        try {
            List<GalleryImage> images = galleryImageService.findAllImages();
            long totalCount = galleryImageService.getImageCount();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("images", images);
            data.put("totalCount", totalCount);

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error fetching images: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 根据 ID 获取嚾片
     * 
     * @param imageId 嚾片 ID
     * @return 嚾片详情
     */
    @GetMapping("/images/{imageId}")
    public Result<GalleryImage> getImage(@PathVariable Long imageId) {
        log.info("Fetching image: {}", imageId);

        try {
            Optional<GalleryImage> image = galleryImageService.findImageById(imageId);
            if (image.isPresent()) {
                return Result.ok(image.get());
            } else {
                return Result.fail("Image not found");
            }
        } catch (Exception e) {
            log.error("Error fetching image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 根据标题搜索嚾片
     * 
     * @param keyword 搜索关键字
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> searchImages(
        @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("Searching images with keyword: {}", keyword);

        try {
            List<GalleryImage> images = keyword == null || keyword.isEmpty() ?
                galleryImageService.findAllImages() :
                galleryImageService.searchImagesByTitle(keyword);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("images", images);
            data.put("count", images.size());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error searching images: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Delete image (soft delete)
     * 
     * @param imageId Image ID
     * @return Delete result
     */
    @DeleteMapping("/images/{imageId}")
    public Result<Void> deleteImage(@PathVariable Long imageId) {
        log.info("Deleting image: {}", imageId);

        try {
            galleryImageService.deleteImage(imageId);
            return Result.ok(null, "Image deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Restore deleted image
     * 
     * @param imageId Image ID
     * @return Restore result
     */
    @PostMapping("/images/{imageId}/restore")
    public Result<Void> restoreImage(@PathVariable Long imageId) {
        log.info("Restoring image: {}", imageId);

        try {
            galleryImageService.restoreImage(imageId);
            return Result.ok(null, "Image restored successfully");
        } catch (Exception e) {
            log.error("Error restoring image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Get image statistics
     * 
     * @return Statistics
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        log.info("Fetching gallery statistics");

        try {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalImages", galleryImageService.getImageCount());
            stats.put("maxUploadSizeMB", alistService.getMaxUploadSizeMb());
            stats.put("batchMaxLimit", alistService.getBatchMaxLimit());

            return Result.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching stats: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 诊断 AList 连接和存储配置
     * 用于当出现"storage not found"错误时进行故障排除
     * 
     * @return 诊断报告
     */
    @GetMapping("/diagnostic/alist-connection")
    public Result<String> testAlistConnection() {
        log.info("Running AList connection diagnostic");

        try {
            String report = alistService.testAlistConnection();
            return Result.ok(report, "Diagnostic completed");
        } catch (Exception e) {
            log.error("Diagnostic failed: {}", e.getMessage());
            return Result.fail("Diagnostic failed: " + e.getMessage());
        }
    }

    /**
     * ============ 后台管理接口 ============
     * 以下接口仅用于后台管理员，返回所有图片（包括已删除的）
     */

    /**
     * 后台接口：获取所有图片（包括已删除的）
     * 用于后台管理员查看所有图片
     * 
     * @return 所有图片列表
     */
    @GetMapping("/admin/images")
    public Result<Map<String, Object>> getAllImagesForAdmin() {
        log.info("Fetching all images for admin panel");

        try {
            List<GalleryImage> images = galleryImageService.findAllImagesForAdmin();
            long totalCount = images.size();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("images", images);
            data.put("totalCount", totalCount);

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error fetching all images for admin: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 后台接口：获取指定图片详情（不考虑删除状态）
     * 用于后台管理员编辑任何状态的图片
     * 
     * @param imageId 图片 ID
     * @return 图片详情
     */
    @GetMapping("/admin/images/{imageId}")
    public Result<GalleryImage> getImageForAdmin(@PathVariable Long imageId) {
        log.info("Fetching image for admin: {}", imageId);

        try {
            Optional<GalleryImage> image = galleryImageService.findImageByIdForAdmin(imageId);
            if (image.isPresent()) {
                return Result.ok(image.get());
            } else {
                return Result.fail("Image not found");
            }
        } catch (Exception e) {
            log.error("Error fetching image for admin: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 后台接口：禁用图片
     * 禁用后前台将不显示此图片
     * 
     * @param imageId 图片 ID
     * @return 操作结果
     */
    @PutMapping("/admin/images/{imageId}/disable")
    public Result<Void> disableImage(@PathVariable Long imageId) {
        log.info("Disabling image: {}", imageId);

        try {
            galleryImageService.disableImage(imageId);
            return Result.ok(null, "Image disabled successfully");
        } catch (Exception e) {
            log.error("Error disabling image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 后台接口：启用图片
     * 启用后前台将可以显示此图片
     * 
     * @param imageId 图片 ID
     * @return 操作结果
     */
    @PutMapping("/admin/images/{imageId}/enable")
    public Result<Void> enableImage(@PathVariable Long imageId) {
        log.info("Enabling image: {}", imageId);

        try {
            galleryImageService.enableImage(imageId);
            return Result.ok(null, "Image enabled successfully");
        } catch (Exception e) {
            log.error("Error enabling image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 后台接口：获取统计信息（包含已删除的图片统计）
     * 
     * @return 统计数据
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getAdminStatistics() {
        log.info("Fetching admin statistics");

        try {
            Map<String, Object> stats = galleryImageService.getAdminStatistics();
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching admin statistics: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
