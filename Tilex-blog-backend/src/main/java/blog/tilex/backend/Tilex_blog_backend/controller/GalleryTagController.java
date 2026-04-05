package blog.tilex.backend.Tilex_blog_backend.controller;

import blog.tilex.backend.Tilex_blog_backend.dto.common.Result;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.BatchCreateTagRequest;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.TagCreateItem;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryTag;
import blog.tilex.backend.Tilex_blog_backend.service.gallery.GalleryTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 画庶标签 Controller
 * 画庶标签操作的 REST API 端点
 */
@RestController
@RequestMapping("/gallery/tags")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class GalleryTagController {

    private final GalleryTagService galleryTagService;

    public GalleryTagController(GalleryTagService galleryTagService) {
        this.galleryTagService = galleryTagService;
    }

    /**
     * 批量创建标签
     * 
     * @param request 批量创建请求
     * @return 创建结果
     */
    @PostMapping("/batch-create")
    public Result<Map<String, Object>> batchCreateTags(
        @RequestBody BatchCreateTagRequest request) {

        log.info("Starting batch tag creation: {} tags", 
            request.getTags() != null ? request.getTags().size() : 0);

        try {
            Map<String, Object> result = galleryTagService.batchCreateTags(request.getTags());
            return Result.ok(result, "Batch tag creation completed");
        } catch (Exception e) {
            log.error("Error in batch tag creation: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 上司日期创建标签
     * 
     * @param tagItem 要创建的标签
     * @return 创建结果
     */
    @PostMapping
    public Result<GalleryTag> createTag(@RequestBody TagCreateItem tagItem) {
        log.info("Creating tag: {}", tagItem.getTagName());

        try {
            // 验证
            if (tagItem.getTagName() == null || tagItem.getTagName().isEmpty()) {
                return Result.fail("Tag name cannot be empty");
            }

            GalleryTag tag = galleryTagService.createOrGetTag(tagItem.getTagName());

            // 更新其他字段(如果提供的话)
            if (tagItem.getTagDescription() != null) {
                tag.setTagDescription(tagItem.getTagDescription());
            }
            if (tagItem.getColorCode() != null) {
                tag.setColorCode(tagItem.getColorCode());
            }
            if (tagItem.getSortOrder() != null) {
                tag.setSortOrder(tagItem.getSortOrder());
            }

            return Result.ok(tag, "Tag created successfully");
        } catch (Exception e) {
            log.error("Error creating tag: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取所有标签
     * 
     * @return 所有活动标签的列表
     */
    @GetMapping
    public Result<Map<String, Object>> getAllTags() {
        log.info("Fetching all tags");

        try {
            List<GalleryTag> tags = galleryTagService.findAllTags();
            long totalCount = galleryTagService.getTagCount();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tags", tags);
            data.put("totalCount", totalCount);

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error fetching tags: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Get tag by ID
     * 
     * @param tagId Tag ID
     * @return Tag details
     */
    @GetMapping("/{tagId}")
    public Result<GalleryTag> getTag(@PathVariable Long tagId) {
        log.info("Fetching tag: {}", tagId);

        try {
            Optional<GalleryTag> tag = galleryTagService.findTagById(tagId);
            if (tag.isPresent()) {
                return Result.ok(tag.get());
            } else {
                return Result.fail("Tag not found");
            }
        } catch (Exception e) {
            log.error("Error fetching tag: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Search tags by pattern
     * 
     * @param pattern Search pattern
     * @return Search results
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> searchTags(
        @RequestParam(value = "pattern", required = false) String pattern) {

        log.info("Searching tags with pattern: {}", pattern);

        try {
            List<GalleryTag> tags = pattern == null || pattern.isEmpty() ?
                galleryTagService.findAllTags() :
                galleryTagService.searchTags(pattern);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tags", tags);
            data.put("count", tags.size());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error searching tags: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Delete tag (soft delete)
     * 
     * @param tagId Tag ID
     * @return Delete result
     */
    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@PathVariable Long tagId) {
        log.info("Deleting tag: {}", tagId);

        try {
            galleryTagService.deleteTag(tagId);
            return Result.ok(null, "Tag deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting tag: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Get images associated with a tag
     * 
     * @param tagId Tag ID
     * @return List of image IDs
     */
    @GetMapping("/{tagId}/images")
    public Result<Map<String, Object>> getImagesForTag(@PathVariable Long tagId) {
        log.info("Fetching images for tag: {}", tagId);

        try {
            // Verify tag exists
            Optional<GalleryTag> tag = galleryTagService.findTagById(tagId);
            if (tag.isEmpty()) {
                return Result.fail("Tag not found");
            }

            List<Long> imageIds = galleryTagService.getImagesByTag(tagId);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tagId", tagId);
            data.put("imageIds", imageIds);
            data.put("count", imageIds.size());

            return Result.ok(data);
        } catch (Exception e) {
            log.error("Error fetching images for tag: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
