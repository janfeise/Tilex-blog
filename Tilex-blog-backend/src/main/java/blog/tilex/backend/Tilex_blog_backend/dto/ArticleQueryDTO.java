package blog.tilex.backend.Tilex_blog_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;


/**
 * 文章查询请求参数
 */
@Data
public class ArticleQueryDTO {
    @Min(value = 1, message = "页码至少为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量至少为1")
    @Max(value = 100, message = "每页数量最多100条")
    private Integer pageSize = 10;

    private String title;      // 标题模糊查询
    private Integer status;    // 状态筛选（0-草稿 1-已发布）
    private String sortField;  // 排序字段（createdAt/updatedAt）
    private String sortOrder;  // 排序方式（asc/desc）
}
