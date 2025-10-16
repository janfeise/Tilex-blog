package blog.tilex.backend.Tilex_blog_backend.service;

import blog.tilex.backend.Tilex_blog_backend.dto.ArticleQueryDTO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleSearchVO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleVO;
import blog.tilex.backend.Tilex_blog_backend.dto.UploadArticleDTO;
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

    /**
     * 上传文章
     */
    ArticleVO createArticle(UploadArticleDTO uploadArticleDTO);

    /**
     * 搜索文章
     * 核心流程:
     * 1. 从数据库查询包含关键词的文章(完整content)
     * 2. 在 Service 层提取包含关键词的片段
     * 3. 对关键词进行高亮处理
     * 4. 封装成 VO 对象返回
     *
     * @param keyword 搜索关键词
     * @return 搜索结果列表(包含snippets数组和完整content)
     */
    List<ArticleSearchVO> searchArticles(String keyword);
}