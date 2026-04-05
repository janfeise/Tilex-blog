package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签创建项 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateItem {
    /**
     * 标签名(必填，不能为空)
     */
    @NotBlank(message = "Tag name cannot be empty")
    private String tagName;

    /**
     * 标签描述(可选)
     */
    private String tagDescription;

    /**
     * 16进制颜色代码(可选)
     * 格式：#RRGGBB
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color code format is incorrect, must be #RRGGBB")
    private String colorCode;

    /**
     * 排序(可选)
     */
    private Integer sortOrder;
}
