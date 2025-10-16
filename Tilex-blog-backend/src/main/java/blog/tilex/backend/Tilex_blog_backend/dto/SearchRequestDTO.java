package blog.tilex.backend.Tilex_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchRequestDTO {
    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词过长，最多100字符")
    private String keyword;
}
