package blog.tilex.backend.Tilex_blog_backend.controller;

import blog.tilex.backend.Tilex_blog_backend.dto.ArticleQueryDTO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleVO;
import blog.tilex.backend.Tilex_blog_backend.service.ArticleService;
import blog.tilex.backend.Tilex_blog_backend.utils.PageResult;
import blog.tilex.backend.Tilex_blog_backend.utils.ResultMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
