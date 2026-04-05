package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建标签请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateTagRequest {
    /**
     * 要创建的标签列表
     */
    private List<TagCreateItem> tags;
}
