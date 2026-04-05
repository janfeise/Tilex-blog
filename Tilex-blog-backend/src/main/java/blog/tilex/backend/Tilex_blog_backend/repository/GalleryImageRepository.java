package blog.tilex.backend.Tilex_blog_backend.repository;

import blog.tilex.backend.Tilex_blog_backend.entity.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 画廊图片存储源
 */
@Repository
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    /**
     * 根据 ID 找到未删除的图片
     */
    Optional<GalleryImage> findByIdAndIsDeleted(Long id, Integer isDeleted);

    /**
     * 找到所有未删除的图片
     */
    List<GalleryImage> findByIsDeletedOrderBySortOrderAsc(Integer isDeleted);

    /**
     * 检查 ID 的未删除图片是否存在
     */
    boolean existsByIdAndIsDeleted(Long id, Integer isDeleted);

    /**
     * 根据标题搜索图片(模糊查询)
     */
    @Query("SELECT g FROM GalleryImage g WHERE g.title LIKE concat('%', :title, '%') AND g.isDeleted = 0")
    List<GalleryImage> searchByTitle(@Param("title") String title);

    /**
     * 分页查询未删除的图片
     */
    @Query(value = "SELECT * FROM gallery_images WHERE is_deleted = 0 ORDER BY sort_order ASC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<GalleryImage> findPagedNotDeleted(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计未删除的图片总数
     */
    long countByIsDeleted(Integer isDeleted);

    /**
     * 获取所有图片（包括已删除的图片，用于管理后台）
     */
    List<GalleryImage> findAllByOrderBySortOrderAsc();

    /**
     * 按 ID 获取任何状态的图片（管理员使用）
     */
    Optional<GalleryImage> findById(Long id);
}
