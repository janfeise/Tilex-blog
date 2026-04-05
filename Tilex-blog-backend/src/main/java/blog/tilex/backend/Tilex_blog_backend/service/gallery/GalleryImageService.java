package blog.tilex.backend.Tilex_blog_backend.service.gallery;

import blog.tilex.backend.Tilex_blog_backend.repository.GalleryImageRepository;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryImageTagRepository;
import blog.tilex.backend.Tilex_blog_backend.repository.GalleryTagRepository;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.BatchUpdateRequest;
import blog.tilex.backend.Tilex_blog_backend.dto.gallery.ImageUpdateItem;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryImage;
import blog.tilex.backend.Tilex_blog_backend.entity.GalleryTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 画庶图片服务
 * 画庶图片操作的业务逻辑
 */
@Service
@Slf4j
public class GalleryImageService {

    private final GalleryImageRepository galleryImageRepository;
    private final GalleryTagRepository galleryTagRepository;
    private final GalleryImageTagRepository galleryImageTagRepository;
    private final AlistService alistService;

    public GalleryImageService(GalleryImageRepository galleryImageRepository,
                              GalleryTagRepository galleryTagRepository,
                              GalleryImageTagRepository galleryImageTagRepository,
                              AlistService alistService) {
        this.galleryImageRepository = galleryImageRepository;
        this.galleryTagRepository = galleryTagRepository;
        this.galleryImageTagRepository = galleryImageTagRepository;
        this.alistService = alistService;
    }

    /**
     * 保存图片元数据
     * 
     * @param image 画庶图片实体
     * @return 已保存的图片
     */
    @Transactional
    public GalleryImage saveImage(GalleryImage image) {
        log.info("Saving image: {}", image.getTitle());
        return galleryImageRepository.save(image);
    }

    /**
     * 根据 ID 查找图片(仅活动图片)
     */
    public Optional<GalleryImage> findImageById(Long imageId) {
        return galleryImageRepository.findByIdAndIsDeleted(imageId, 0);
    }

    /**
     * 查找所有仃动的图片
     */
    public List<GalleryImage> findAllImages() {
        return galleryImageRepository.findByIsDeletedOrderBySortOrderAsc(0);
    }

    /**
     * 根据标题搜索图片
     */
    public List<GalleryImage> searchImagesByTitle(String title) {
        return galleryImageRepository.searchByTitle(title);
    }

    /**
     * 批量更新图片
     * 仅藁准修改特定字段
     */
    @Transactional
    public Map<String, Object> batchUpdateImages(BatchUpdateRequest request) {
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            throw new IllegalArgumentException("Update list cannot be empty");
        }

        if (request.getUpdates().size() > alistService.getBatchMaxLimit()) {
            throw new IllegalArgumentException(
                "Cannot update more than " + alistService.getBatchMaxLimit() + " images at once");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (ImageUpdateItem item : request.getUpdates()) {
            try {
                GalleryImage image = galleryImageRepository.findByIdAndIsDeleted(item.getId(), 0)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + item.getId()));

                // 仅藁准修改这些字段
                if (item.getTitle() != null) {
                    image.setTitle(item.getTitle());
                }
                if (item.getDescription() != null) {
                    image.setDescription(item.getDescription());
                }
                if (item.getSortOrder() != null) {
                    image.setSortOrder(item.getSortOrder());
                }

                galleryImageRepository.save(image);
                successCount++;

            } catch (Exception e) {
                errors.add("ID " + item.getId() + ": " + e.getMessage());
                log.error("Error updating image {}: {}", item.getId(), e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("totalCount", request.getUpdates().size());
        result.put("errors", errors);

        log.info("Batch update completed: {} success, {} errors", successCount, errors.size());
        return result;
    }

    /**
     * 刪除图片(软删除)
     */
    @Transactional
    public void deleteImage(Long imageId) {
        GalleryImage image = galleryImageRepository.findByIdAndIsDeleted(imageId, 0)
            .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        image.setIsDeleted(1);
        galleryImageRepository.save(image);

        // 也删除所有个签关联
        galleryImageTagRepository.deleteByImageId(imageId);

        log.info("Image deleted (soft): {}", imageId);
    }

    /**
     * 恢复已刪除的17香蒙冶罗梅
     */
    @Transactional
    public void restoreImage(Long imageId) {
        GalleryImage image = galleryImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (image.getIsDeleted() == 0) {
            throw new IllegalArgumentException("Image is not deleted");
        }

        image.setIsDeleted(0);
        galleryImageRepository.save(image);

        log.info("Image restored: {}", imageId);
    }

    /**
     * 获取图片数量（仅统计未删除的）
     */
    public long getImageCount() {
        return galleryImageRepository.countByIsDeleted(0);
    }

    /**
     * 后台接口：获取所有图片（包括已删除的）
     * 用于管理员查看所有图片状态（已发布/已删除）
     */
    public List<GalleryImage> findAllImagesForAdmin() {
        log.info("Fetching all images for admin");
        return galleryImageRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 后台接口：获取指定图片（不考虑删除状态）
     * 用于管理员编辑任何状态的图片
     */
    public Optional<GalleryImage> findImageByIdForAdmin(Long imageId) {
        return galleryImageRepository.findById(imageId);
    }

    /**
     * 后台接口：禁用图片（设置 is_deleted = 1）
     * 禁用后前台将不显示此图片
     */
    @Transactional
    public void disableImage(Long imageId) {
        GalleryImage image = galleryImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        if (image.getIsDeleted() == 1) {
            throw new IllegalArgumentException("Image is already disabled");
        }

        image.setIsDeleted(1);
        galleryImageRepository.save(image);

        // 也删除所有标签关联
        galleryImageTagRepository.deleteByImageId(imageId);

        log.info("Image disabled: {}", imageId);
    }

    /**
     * 后台接口：启用图片（设置 is_deleted = 0）
     * 启用后前台将可以显示此图片
     */
    @Transactional
    public void enableImage(Long imageId) {
        GalleryImage image = galleryImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        if (image.getIsDeleted() == 0) {
            throw new IllegalArgumentException("Image is already enabled");
        }

        image.setIsDeleted(0);
        galleryImageRepository.save(image);

        log.info("Image enabled: {}", imageId);
    }

    /**
     * 后台接口：获取所有图片的统计信息
     */
    public Map<String, Object> getAdminStatistics() {
        long totalCount = galleryImageRepository.count();
        long activeCount = galleryImageRepository.countByIsDeleted(0);
        long disabledCount = galleryImageRepository.countByIsDeleted(1);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("activeCount", activeCount);
        stats.put("disabledCount", disabledCount);

        log.info("Admin statistics: total={}, active={}, disabled={}", 
            totalCount, activeCount, disabledCount);
        return stats;
    }
}
