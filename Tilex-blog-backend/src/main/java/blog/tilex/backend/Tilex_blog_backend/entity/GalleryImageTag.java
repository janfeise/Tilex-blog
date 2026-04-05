package blog.tilex.backend.Tilex_blog_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 画廊图片-标签关联 Entity
 * 表示 GalleryImage 和 GalleryTag 之间的多对多关系
 */
@Entity
@Table(name = "gallery_image_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"image_id", "tag_id"})
}, indexes = {
    @Index(name = "idx_image_id", columnList = "image_id"),
    @Index(name = "idx_tag_id", columnList = "tag_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImageTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图片 ID(外锯)
     */
    @Column(nullable = false)
    private Long imageId;

    /**
     * 标签 ID(外锯)
     */
    @Column(nullable = false)
    private Long tagId;

    /**
     * 快速创建的构造函数
     */
    public GalleryImageTag(Long imageId, Long tagId) {
        this.imageId = imageId;
        this.tagId = tagId;
    }
}
