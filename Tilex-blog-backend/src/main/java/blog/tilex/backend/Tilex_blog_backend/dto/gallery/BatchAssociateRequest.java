package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量把图片与标签关联请求 DTO
 * 支持两种模式：
 * 1. associations 模式：每个项丫点量嚾片及其标签
 * 2. imageIds + tagIds 模式：笛卡尔乘积关联
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchAssociateRequest {
    /**
     * 模式 1: 一对多关联
     * 每个项丫轩包含一个嚾片及其关联的标签
     */
    private List<ImageTagAssociation> associations;

    /**
     * 模式 2: 通过笛卡尔乘积的多对多
     * 所有嚾片都与所有标签关联
     */
    private List<Long> imageIds;

    /**
     * 模式 2 的标签 ID
     */
    private List<Long> tagIds;
}
