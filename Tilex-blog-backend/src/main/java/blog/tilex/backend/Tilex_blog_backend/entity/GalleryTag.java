package blog.tilex.backend.Tilex_blog_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 画廊标签 Entity
 * 用于分类和标签图片
 */
@Entity
@Table(name = "gallery_tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "tag_name", unique = true),
    @Index(name = "idx_is_deleted", columnList = "is_deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GalleryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标签名称(唯一)
     */
    @Column(nullable = false, length = 100, unique = true)
    private String tagName;

    /**
     * 标签描述
     */
    @Column(columnDefinition = "TEXT")
    private String tagDescription;

    /**
     * UI 展示的颜色代码(十六进制)
     * 例子：#FF6B6B
     */
    @Column(length = 7)
    private String colorCode;

    /**
     * 排序须
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /**
     * 创建时间戳
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间戳
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 软删除标记: 0 = 未删除, 1 = 已删除
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (isDeleted == null) {
            isDeleted = 0;
        }
        if (colorCode == null) {
            colorCode = "#999999";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
