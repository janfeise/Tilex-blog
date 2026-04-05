package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片-标签关联 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageTagAssociation {
    /**
     * 嚾片 ID
     */
    private Long imageId;

    /**
     * 与这个图片关联的标签 ID 列表
     */
    private List<Long> tagIds;
}
