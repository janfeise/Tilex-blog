package blog.tilex.backend.Tilex_blog_backend.repository;

import blog.tilex.backend.Tilex_blog_backend.entity.GalleryImageTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 画廊图片-标签关联存储源
 */
@Repository
public interface GalleryImageTagRepository extends JpaRepository<GalleryImageTag, Long> {

    /**
     * 找到特定图片的所有标签
     */
    List<GalleryImageTag> findByImageId(Long imageId);

    /**
     * 找到特定标签的所有图片
     */
    List<GalleryImageTag> findByTagId(Long tagId);

    /**
     * 找到图片和标签之间的关联
     */
    Optional<GalleryImageTag> findByImageIdAndTagId(Long imageId, Long tagId);

    /**
     * 检查关联是否存在
     */
    boolean existsByImageIdAndTagId(Long imageId, Long tagId);

    /**
     * 删除图片和标签之间的关联
     */
    void deleteByImageIdAndTagId(Long imageId, Long tagId);

    /**
     * 删除常力的所有关联
     */
    void deleteByImageId(Long imageId);

    /**
     * 删除标签的所有关联
     */
    void deleteByTagId(Long tagId);

    /**
     * 统计图片的关联数
     */
    long countByImageId(Long imageId);

    /**
     * 统计标签的关联数
     */
    long countByTagId(Long tagId);

    /**
     * 根据标签 ID 找根据图片 ID
     */
    @Query("SELECT gait.imageId FROM GalleryImageTag gait WHERE gait.tagId = :tagId")
    List<Long> findImageIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 根据图片 ID 找根据标签 ID
     */
    @Query("SELECT gait.tagId FROM GalleryImageTag gait WHERE gait.imageId = :imageId")
    List<Long> findTagIdsByImageId(@Param("imageId") Long imageId);
}
