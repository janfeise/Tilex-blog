package blog.tilex.backend.Tilex_blog_backend.service.gallery;

import blog.tilex.backend.Tilex_blog_backend.config.GalleryConfig;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryImageRepository;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryImageTagRepository;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryTagRepository;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.BatchAssociateRequest;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.ImageTagAssociation;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryImageTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 画庶图片-标签关联服务
 * 处理图片和标签之间的多对多关系
 */
@Service
@Slf4j
public class GalleryImageTagService {

    private final GalleryImageRepository galleryImageRepository;
    private final GalleryTagRepository galleryTagRepository;
    private final GalleryImageTagRepository galleryImageTagRepository;
    private final GalleryConfig galleryConfig;

    public GalleryImageTagService(GalleryImageRepository galleryImageRepository,
                                 GalleryTagRepository galleryTagRepository,
                                 GalleryImageTagRepository galleryImageTagRepository,
                                 GalleryConfig galleryConfig) {
        this.galleryImageRepository = galleryImageRepository;
        this.galleryTagRepository = galleryTagRepository;
        this.galleryImageTagRepository = galleryImageTagRepository;
        this.galleryConfig = galleryConfig;
    }

    /**
     * 把图片与特定标签关联
     */
    @Transactional
    public void associateImageWithTags(Long imageId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // Verify image exists and is not deleted
        if (!galleryImageRepository.existsByIdAndIsDeleted(imageId, 0)) {
            throw new IllegalArgumentException("Image not found: " + imageId);
        }

        for (Long tagId : tagIds) {
            // Verify tag exists and is not deleted
            if (!galleryTagRepository.existsByIdAndIsDeleted(tagId, 0)) {
                throw new IllegalArgumentException("Tag not found: " + tagId);
            }

            // Check if association already exists
            if (!galleryImageTagRepository.existsByImageIdAndTagId(imageId, tagId)) {
                GalleryImageTag association = new GalleryImageTag(imageId, tagId);
                galleryImageTagRepository.save(association);
                log.debug("Image-tag association created: imageId={}, tagId={}", imageId, tagId);
            }
        }
    }

    /**
     * 批量把图片与标签关联
     * 支持两种模式：
     * 1. associations 模式：每个关联指定嚾片及其标签
     * 2. imageIds + tagIds 模式：笛卡尔乘积
     */
    @Transactional
    public Map<String, Object> batchAssociateImageTags(BatchAssociateRequest request) {
        List<ImageTagAssociation> associations = new ArrayList<>();

        // 将请求转换为关联列表
        if (request.getAssociations() != null && !request.getAssociations().isEmpty()) {
            // 模式 1：关联
            associations = request.getAssociations();
        } else if (request.getImageIds() != null && !request.getImageIds().isEmpty() &&
                   request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // 模式 2： imageIds + tagIds 笛卡尔乘积
            for (Long imageId : request.getImageIds()) {
                ImageTagAssociation assoc = new ImageTagAssociation();
                assoc.setImageId(imageId);
                assoc.setTagIds(request.getTagIds());
                associations.add(assoc);
            }
        } else {
            throw new IllegalArgumentException(
                "Must provide either associations or both imageIds and tagIds");
        }

        // 检查计数限制
        long totalAssociations = associations.stream()
            .mapToLong(a -> (long) (a.getTagIds() != null ? a.getTagIds().size() : 0))
            .sum();

        if (totalAssociations > galleryConfig.getBatchMaxLimit()) {
            throw new IllegalArgumentException(
                "Total associations exceed limit of " + galleryConfig.getBatchMaxLimit());
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (ImageTagAssociation assoc : associations) {
            try {
                // Verify image exists and is not deleted
                if (!galleryImageRepository.existsByIdAndIsDeleted(assoc.getImageId(), 0)) {
                    throw new IllegalArgumentException("Image not found: " + assoc.getImageId());
                }

                for (Long tagId : assoc.getTagIds()) {
                    try {
                        // Verify tag exists and is not deleted
                        if (!galleryTagRepository.existsByIdAndIsDeleted(tagId, 0)) {
                            throw new IllegalArgumentException("Tag not found: " + tagId);
                        }

                        // Check if association already exists
                        if (!galleryImageTagRepository.existsByImageIdAndTagId(
                            assoc.getImageId(), tagId)) {
                            GalleryImageTag imageTag = new GalleryImageTag(assoc.getImageId(), tagId);
                            galleryImageTagRepository.save(imageTag);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errors.add("Image:" + assoc.getImageId() + " Tag:" + tagId +
                            " - " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                errors.add("Association error - " + e.getMessage());
                log.error("Error processing association: {}", e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("totalCount", associations.stream()
            .mapToLong(a -> (long) (a.getTagIds() != null ? a.getTagIds().size() : 0))
            .sum());
        result.put("errors", errors);

        log.info("Batch association completed: {} success, {} errors", successCount, errors.size());
        return result;
    }

    /**
     * 把图片与批定标签基美
     */
    @Transactional
    public void disassociateImageFromTags(Long imageId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        for (Long tagId : tagIds) {
            galleryImageTagRepository.deleteByImageIdAndTagId(imageId, tagId);
            log.debug("Image-tag association deleted: imageId={}, tagId={}", imageId, tagId);
        }
    }

    /**
     * 获取嚾片的所有标签
     */
    public List<Long> getTagsForImage(Long imageId) {
        return galleryImageTagRepository.findTagIdsByImageId(imageId);
    }

    /**
     * 获取标签的所有图片
     */
    public List<Long> getImagesForTag(Long tagId) {
        return galleryImageTagRepository.findImageIdsByTagId(tagId);
    }

    /**
     * 检查图片-标签关联是否存在
     */
    public boolean associationExists(Long imageId, Long tagId) {
        return galleryImageTagRepository.existsByImageIdAndTagId(imageId, tagId);
    }
}
