package blog.tilex.backend.Tilex_blog_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 画廊图片 Entity
 * 存储已上传图片的信息
 * 
 * 字段权限矩阵：
 * - id: 系统生成，不可修改
 * - title: 用户可输入改上修改
 * - description: 用户可输入改上修改
 * - image_url: 事件推改物篏可憵は AList 需要，不可修改
 * - alist_path: 后端解析，不可修改
 * - alist_domain: 系统管理，可控制䝪修改
 * - source_type: 系统生成，不可修改
 * - has_sign_param: 自动检测，一既乆可修改
 * - sort_order: 用户可修改
 * - created_at: 系统生成，不可修改
 * - updated_at: 自动更新，不可改上修改
 * - is_deleted: 软去会仂旗，用户可修改
 */
@Entity
@Table(name = "gallery_images", indexes = {
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_is_deleted", columnList = "is_deleted"),
    @Index(name = "idx_sort_order", columnList = "sort_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图片标题(用户可修改)
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 图片描述(用户可修改)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 完整图片 URL（来自 AList）
     * 不可修改，系统生成
     * 格式：http://oss.tilex.world/d/blog-images/2026/03/20/filename.png?sign=xxx
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * 从 URL 解析出来的相对路径(后端解析)
     * 不可修改，系统生成
     * 格式：/d/blog-images/2026/03/20/filename.png
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String alistPath;

    /**
     * AList 域名
     * 有限修改，通常由琳自动竟管理
     * 例：oss.tilex.world
     */
    @Column(nullable = false, length = 255)
    private String alistDomain;

    /**
     * 来源类型：alist, local, cdn
     * 系统生成，不可修改
     */
    @Column(nullable = false, length = 50)
    private String sourceType;

    /**
     * URL 是否含有 sign 参数
     * 0 = 不含, 1 = 含有
     * 自动检测，不可修改
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer hasSignParam;

    /**
     * 排序(用户可修改)
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /**
     * 创建时间戳(不可修改)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间戳(不可手动修改)
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 软删除标记: 0 = 未删除, 1 = 已删除
     * 用户可修改(遮下犬)
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
        if (hasSignParam == null) {
            hasSignParam = 0;
        }
        if (isDeleted == null) {
            isDeleted = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
