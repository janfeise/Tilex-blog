package blog.tilex.backend.Tilex_blog_backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章搜索结果VO
 * 用于封装搜索接口的返回数据
 */
@Data
public class ArticleSearchVO {
    /**
     * 文章ID - 用于跳转详情页
     */
    private int id;

    /**
     * 原始标题
     */
    private String title;

    /**
     * 包含关键词的片段数组(用于搜索结果列表展示)
     * 示例: ["...Spring Boot是一个...", "...使用Spring Boot..."]
     */
    private List<String> snippets;

    /**
     * 完整的文章内容(用于详情页展示)
     */
    private String content;
}
