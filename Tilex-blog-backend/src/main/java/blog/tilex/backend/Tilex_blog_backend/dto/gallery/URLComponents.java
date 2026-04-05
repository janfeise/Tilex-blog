package blog.tilex.backend.Tilex_blog_backend.dto.gallery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * URL 组件 DTO
 * 表示 AList URL 的已解析组件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLComponents {
    /**
     * 域名(例子： oss.tilex.world)
     */
    private String domain;

    /**
     * 路径(例子： /d/blog-images/2026/03/20/filename.png)
     */
    private String path;

    /**
     * URL 是否含有签名参数
     */
    private boolean hasSign;
}
