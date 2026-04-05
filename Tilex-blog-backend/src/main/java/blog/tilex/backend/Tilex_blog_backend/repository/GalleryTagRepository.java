package blog.tilex.backend.Tilex_blog_backend.repository;

import blog.tilex.backend.Tilex_blog_backend.entity.GalleryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 画廊标签存储源
 */
@Repository
public interface GalleryTagRepository extends JpaRepository<GalleryTag, Long> {

    /**
     * 根据标签名称找到未删除的标签
     */
    Optional<GalleryTag> findByTagNameAndIsDeleted(String tagName, Integer isDeleted);

    /**
     * 根据 ID 找到未删除的标签
     */
    Optional<GalleryTag> findByIdAndIsDeleted(Long id, Integer isDeleted);

    /**
     * 找到所有未删除的标签
     */
    List<GalleryTag> findByIsDeletedOrderBySortOrderAsc(Integer isDeleted);

    /**
     * 检查 ID 的未删除标签是否存在
     */
    boolean existsByIdAndIsDeleted(Long id, Integer isDeleted);

    /**
     * 检查标签名称的未删除标签是否存在
     */
    boolean existsByTagNameAndIsDeleted(String tagName, Integer isDeleted);

    /**
     * 根据标签名称模式找标签(模糊查询)
     */
    List<GalleryTag> findByTagNameContainingAndIsDeleted(String tagName, Integer isDeleted);
}
