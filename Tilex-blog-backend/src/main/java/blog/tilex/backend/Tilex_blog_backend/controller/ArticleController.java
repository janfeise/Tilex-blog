package blog.tilex.backend.Tilex_blog_backend.controller;

import blog.tilex.backend.Tilex_blog_backend.dto.*;
import blog.tilex.backend.Tilex_blog_backend.service.ArticleService;
import blog.tilex.backend.Tilex_blog_backend.utils.PageResult;
import blog.tilex.backend.Tilex_blog_backend.utils.ResultMsg;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.SignStyle;
import java.util.List;

/**
 * 文章控制器
 * RESTful API 规范
 */
@RestController
@RequestMapping("/articles")
@Slf4j
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 获取所有文章（不分页）
     * GET /articles?all=true
     *
     * 前端调用：getArticles({ all: true })
     */
    @GetMapping
    public ResultMsg<Object> getArticles(
            @RequestParam(required = false) Boolean all,
            @Validated ArticleQueryDTO queryDTO) {

        log.info("接收到获取文章请求，all={}, queryDTO={}", all, queryDTO);

        // 如果 all=true，返回所有文章（不分页）
        if (Boolean.TRUE.equals(all)) {
            List<ArticleVO> articles = articleService.getAllArticles();
            return ResultMsg.success(articles);
        }

        // 否则返回分页数据
        PageResult<ArticleVO> pageResult = articleService.getArticleList(queryDTO);
        return ResultMsg.success(pageResult);
    }

    /**
     * 获取文章详情
     * GET /articles/{id}
     *
     * 前端调用：request("/articles/1", "GET")
     */
    @GetMapping("/{id}")
    public ResultMsg<ArticleVO> getArticleById(@PathVariable int id) {
        log.info("获取文章详情，ID: {}", id);

        ArticleVO article = articleService.getArticleById(id);
        return ResultMsg.success(article);
    }

    /**
     * 上传文章
     * POST/articles
     *
     * 前端调用：
     * POST/articles
     * Body(JSON)
     * {
     *     "title": "我的第一篇文章",
     *     "content": "这是一篇用来测试上传的文章内容",
     *     "createdAt": "2025-10-08"
     * }
     */
    @PostMapping
    public ResultMsg<ArticleVO> createArticle(@Valid @RequestBody UploadArticleDTO uploadArticleDTO) {
        log.info("收到创建文章请求: {}", uploadArticleDTO);

        ArticleVO savedArticle = articleService.createArticle(uploadArticleDTO);

        return ResultMsg.success(savedArticle);
    }

    /**
     * 文章控制器
     * 提供搜索和详情查询接口
     */
    @PostMapping("/search")
    public ResultMsg<List<ArticleSearchVO>> searchArticles(
            @Valid @RequestBody SearchRequestDTO searchRequestDTO) {
        String keyword = searchRequestDTO.getKeyword();

        try {
            // service层进行搜索
            List<ArticleSearchVO> results = articleService.searchArticles(keyword);

            log.info("搜索成功，关键词： {}， 匹配数目: {}", keyword, results.size());
            return ResultMsg.success(results);
        } catch (Exception e) {
            return ResultMsg.<List<ArticleSearchVO>>error("搜索失败：" + e.getMessage());
        }
    }
}
