package blog.tilex.backend.Tilex_blog_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章响应对象
 */
@Data
public class ArticleVO {
    private int id;
    private String title;
    private String content;
    private Integer status;
    private String statusName;  // 状态名称（草稿/已发布）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
