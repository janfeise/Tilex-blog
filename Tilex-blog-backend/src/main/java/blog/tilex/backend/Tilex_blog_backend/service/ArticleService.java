package blog.tilex.backend.Tilex_blog_backend.service;

import blog.tilex.backend.Tilex_blog_backend.dto.ArticleQueryDTO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleVO;
import blog.tilex.backend.Tilex_blog_backend.utils.PageResult;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 获取所有文章（不分页）
     */
    List<ArticleVO> getAllArticles();

    /**
     * 获取文章列表（分页 + 条件查询）
     */
    PageResult<ArticleVO> getArticleList(ArticleQueryDTO queryDTO);

    /**
     * 根据ID获取文章详情
     */
    ArticleVO getArticleById(int id);
}