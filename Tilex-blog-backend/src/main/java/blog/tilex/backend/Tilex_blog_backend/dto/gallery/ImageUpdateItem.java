package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片更新项 DTO
 * 用于批量更新请求
 * 仅藁准修改特定字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUpdateItem {
    /**
     * 图片 ID(必填)
     */
    private Long id;

    /**
     * 嚾片标题(可修改)
     */
    private String title;

    /**
     * 嚾片描述(可修改)
     */
    private String description;

    /**
     * 排序(可修改)
     */
    private Integer sortOrder;

    // 以下字段为只读，无法修改
    // 即使在请求中退回，它们也会丢了

    @JsonIgnore
    private String imageUrl;

    @JsonIgnore
    private String alistPath;

    @JsonIgnore
    private String alistDomain;

    @JsonIgnore
    private String sourceType;

    @JsonIgnore
    private Integer hasSignParam;

    @JsonIgnore
    private Long createdAt;

    @JsonIgnore
    private Long updatedAt;
}
