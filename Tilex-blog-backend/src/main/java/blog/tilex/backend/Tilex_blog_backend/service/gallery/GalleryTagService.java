package blog.tilex.backend.Tilex_blog_backend.service.gallery;

import blog.tilex.backend.Tilex_blog_backend.config.GalleryConfig;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryImageTagRepository;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryTagRepository;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.TagCreateItem;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 画庶标签服务
 * 画庶标签操作的业务逻辑
 */
@Service
@Slf4j
public class GalleryTagService {

    private final GalleryTagRepository galleryTagRepository;
    private final GalleryImageTagRepository galleryImageTagRepository;
    private final GalleryConfig galleryConfig;

    public GalleryTagService(GalleryTagRepository galleryTagRepository,
                            GalleryImageTagRepository galleryImageTagRepository,
                            GalleryConfig galleryConfig) {
        this.galleryTagRepository = galleryTagRepository;
        this.galleryImageTagRepository = galleryImageTagRepository;
        this.galleryConfig = galleryConfig;
    }

    /**
     * 根据 ID 查找标签(仅活动的标签)
     */
    public Optional<GalleryTag> findTagById(Long tagId) {
        return galleryTagRepository.findByIdAndIsDeleted(tagId, 0);
    }

    /**
     * 根据名称查找标签(仅活务的标签)
     */
    public Optional<GalleryTag> findTagByName(String tagName) {
        return galleryTagRepository.findByTagNameAndIsDeleted(tagName, 0);
    }

    /**
     * 找到所有活动的标签
     */
    public List<GalleryTag> findAllTags() {
        return galleryTagRepository.findByIsDeletedOrderBySortOrderAsc(0);
    }

    /**
     * 根据名称模式云查标签
     */
    public List<GalleryTag> searchTags(String pattern) {
        return galleryTagRepository.findByTagNameContainingAndIsDeleted(pattern, 0);
    }

    /**
     * 上司日期创建标签(如果没有的话)
     */
    @Transactional
    public GalleryTag createOrGetTag(String tagName) {
        Optional<GalleryTag> existingTag = findTagByName(tagName);
        if (existingTag.isPresent()) {
            log.debug("Tag already exists: {}", tagName);
            return existingTag.get();
        }

        GalleryTag tag = new GalleryTag();
        tag.setTagName(tagName);
        tag.setColorCode("#999999");
        tag.setIsDeleted(0);

        GalleryTag savedTag = galleryTagRepository.save(tag);
        log.info("Tag created: {}", tagName);
        return savedTag;
    }

    /**
     * 根据名称获取或创建一个标签
     */
    @Transactional
    public List<Long> getOrCreateTagIds(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        return tagNames.stream()
            .map(this::createOrGetTag)
            .map(GalleryTag::getId)
            .collect(Collectors.toList());
    }

    /**
     * 批量创建标签
     */
    @Transactional
    public Map<String, Object> batchCreateTags(List<TagCreateItem> tagItems) {
        if (tagItems == null || tagItems.isEmpty()) {
            throw new IllegalArgumentException("Tag list cannot be empty");
        }

        if (tagItems.size() > galleryConfig.getBatchMaxLimit()) {
            throw new IllegalArgumentException(
                "Cannot create more than " + galleryConfig.getBatchMaxLimit() + " tags at once");
        }

        List<GalleryTag> createdTags = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int skipCount = 0;

        for (TagCreateItem item : tagItems) {
            try {
                // Check if tag already exists
                if (galleryTagRepository.existsByTagNameAndIsDeleted(item.getTagName(), 0)) {
                    skipCount++;
                    log.debug("Tag already exists, skipping: {}", item.getTagName());
                    continue;
                }

                GalleryTag tag = new GalleryTag();
                tag.setTagName(item.getTagName());
                tag.setTagDescription(item.getTagDescription());
                tag.setColorCode(item.getColorCode() != null ? item.getColorCode() : "#999999");
                tag.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
                tag.setIsDeleted(0);

                GalleryTag savedTag = galleryTagRepository.save(tag);
                createdTags.add(savedTag);
                log.debug("Tag created: {}", item.getTagName());

            } catch (Exception e) {
                errors.add(item.getTagName() + ": " + e.getMessage());
                log.error("Error creating tag {}: {}", item.getTagName(), e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("createdCount", createdTags.size());
        result.put("skippedCount", skipCount);
        result.put("totalCount", tagItems.size());
        result.put("createdTags", createdTags);
        result.put("errors", errors);

        log.info("Batch tag creation completed: {} created, {} skipped, {} errors",
            createdTags.size(), skipCount, errors.size());
        return result;
    }

    /**
     * 刪除标签(软删除)
     */
    @Transactional
    public void deleteTag(Long tagId) {
        GalleryTag tag = galleryTagRepository.findByIdAndIsDeleted(tagId, 0)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        tag.setIsDeleted(1);
        galleryTagRepository.save(tag);

        // Also delete all associations
        galleryImageTagRepository.deleteByTagId(tagId);

        log.info("Tag deleted (soft): {}", tagId);
    }

    /**
     * 获取活动标签数
     */
    public long getTagCount() {
        List<GalleryTag> tags = galleryTagRepository.findByIsDeletedOrderBySortOrderAsc(0);
        return tags.size();
    }

    /**
     * 获取与标签关联的图片
     */
    public List<Long> getImagesByTag(Long tagId) {
        return galleryImageTagRepository.findImageIdsByTagId(tagId);
    }

    /**
     * 获取图片的标签
     */
    public List<Long> getTagsByImage(Long imageId) {
        return galleryImageTagRepository.findTagIdsByImageId(imageId);
    }
}
