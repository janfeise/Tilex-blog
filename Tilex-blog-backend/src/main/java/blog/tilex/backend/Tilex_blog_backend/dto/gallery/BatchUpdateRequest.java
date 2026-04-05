package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量更新请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateRequest {
    /**
     * 要更新的项丫表
     */
    private List<ImageUpdateItem> updates;
}
