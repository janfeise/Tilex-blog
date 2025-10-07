package blog.tilex.backend.Tilex_blog_backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Integer id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
