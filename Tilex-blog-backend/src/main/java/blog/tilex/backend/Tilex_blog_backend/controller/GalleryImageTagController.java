package blog.tilex.backend.Tilex_blog_backend.controller;

import blog.tilex.backend.Tilex_blog_backend.dto.common.Result;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.BatchAssociateRequest;
import blog.tilex.backend.Tilex_blog_backend.service.gallery.GalleryImageTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画庶图片-标签关联 Controller
 * 图片-标签关联操作的 REST API 端点
 */
@RestController
@RequestMapping("/gallery/image-tags")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class GalleryImageTagController {

    private final GalleryImageTagService galleryImageTagService;

    public GalleryImageTagController(GalleryImageTagService galleryImageTagService) {
        this.galleryImageTagService = galleryImageTagService;
    }

    /**
     * 批量把图片与标签关联
     * 支持两种模式：
     * 1. associations 模式：每个项丫指定嚾片及其标签
     * 2. imageIds + tagIds 模式：笛卡尔乘积关联
     * 
     * @param request 批量关联请求
     * @return 关联结果
     */
    @PostMapping("/batch-associate")
    public Result<Map<String, Object>> batchAssociateImageTags(
        @RequestBody BatchAssociateRequest request) {

        log.info("Starting batch image-tag association");

        try {
            Map<String, Object> result = galleryImageTagService.batchAssociateImageTags(request);
            return Result.ok(result, "Batch association completed");
        } catch (Exception e) {
            log.error("Error in batch association: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 把单个嚾片与标签关联
     * 
     * @param imageId 嚾片 ID
     * @param tagIds 标签 ID 列表
     * @return 关联结果
     */
    @PostMapping("/images/{imageId}/tags")
    public Result<Void> associateImageWithTags(
        @PathVariable Long imageId,
        @RequestBody List<Long> tagIds) {

        log.info("Associating image {} with {} tags", imageId, tagIds.size());

        try {
            galleryImageTagService.associateImageWithTags(imageId, tagIds);
            return Result.ok(null, "Association created successfully");
        } catch (Exception e) {
            log.error("Error associating image with tags: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取嚾片的所有标签
     * 
     * @param imageId 嚾片 ID
     * @return 标签 ID 列表
     */
    @GetMapping("/images/{imageId}/tags")
    public Result<Map<String, Object>> getTagsForImage(@PathVariable Long imageId) {
        log.info("Fetching tags for image: {}", imageId);

        try {
            List<Long> tagIds = galleryImageTagService.getTagsForImage(imageId);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("imageId", imageId);
            data.put("tagIds", tagIds);
            data.put("count", tagIds.size());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error fetching tags for image: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Disassociate image from tags
     * 
     * @param imageId Image ID
     * @param tagIds List of tag IDs to disassociate
     * @return Disassociation result
     */
    @DeleteMapping("/images/{imageId}/tags")
    public Result<Void> disassociateImageFromTags(
        @PathVariable Long imageId,
        @RequestBody List<Long> tagIds) {

        log.info("Disassociating image {} from {} tags", imageId, tagIds.size());

        try {
            galleryImageTagService.disassociateImageFromTags(imageId, tagIds);
            return Result.ok(null, "Disassociation completed successfully");
        } catch (Exception e) {
            log.error("Error disassociating image from tags: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Batch disassociate images from tags
     * 
     * @param request Batch disassociate request (uses associations mode)
     * @return Disassociation result
     */
    @PostMapping("/batch-disassociate")
    public Result<Map<String, Object>> batchDisassociateImageFromTags(
        @RequestBody BatchAssociateRequest request) {

        log.info("Starting batch image-tag disassociation");

        try {
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            if (request.getAssociations() != null) {
                for (var assoc : request.getAssociations()) {
                    try {
                        galleryImageTagService.disassociateImageFromTags(
                            assoc.getImageId(), assoc.getTagIds());
                        successCount += assoc.getTagIds().size();
                    } catch (Exception e) {
                        errors.add("Image:" + assoc.getImageId() + " - " + e.getMessage());
                    }
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("successCount", successCount);
            result.put("errors", errors);

            return Result.ok(result, "Batch disassociation completed");
        } catch (Exception e) {
            log.error("Error in batch disassociation: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Check if image-tag association exists
     * 
     * @param imageId Image ID
     * @param tagId Tag ID
     * @return Association status
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkAssociation(
        @RequestParam Long imageId,
        @RequestParam Long tagId) {

        log.info("Checking association between image {} and tag {}", imageId, tagId);

        try {
            boolean exists = galleryImageTagService.associationExists(imageId, tagId);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("imageId", imageId);
            data.put("tagId", tagId);
            data.put("exists", exists);

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error checking association: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
