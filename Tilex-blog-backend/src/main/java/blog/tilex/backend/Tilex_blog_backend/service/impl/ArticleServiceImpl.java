package blog.tilex.backend.Tilex_blog_backend.service.impl;

import blog.tilex.backend.Tilex_blog_backend.dao.PostDao;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleQueryDTO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleVO;
import blog.tilex.backend.Tilex_blog_backend.entity.Post;
import blog.tilex.backend.Tilex_blog_backend.service.ArticleService;
import blog.tilex.backend.Tilex_blog_backend.utils.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private PostDao postDao;

    /**
     * 获取所有文章（不分页）
     * 业务逻辑：1.查询数据库 2.转换为VO 3.处理状态名称
     */
    @Override
    public List<ArticleVO> getAllArticles() {
        log.info("查询所有文章");

        // 1. 查询所有文章
        List<Post> posts = postDao.selectAll();

        // 2. 转换为 VO 列表
        List<ArticleVO> articleVOS = posts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("查询到 {} 篇文章", articleVOS.size());
        return articleVOS;
    }

    /**
     * 分页查询文章列表
     * 业务逻辑：1.构建分页对象 2.条件查询 3.转换结果
     */
    @Override
    public PageResult<ArticleVO> getArticleList(ArticleQueryDTO queryDTO) {
        log.info("分页查询文章，参数: {}", queryDTO);

        // 1. 计算分页参数
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 执行条件查询分页
        List<Post> postList = postDao.selectArticlesByCondition(
                offset,
                pageSize,
                queryDTO.getTitle(),
                queryDTO.getStatus(),
                queryDTO.getSortField(),
                queryDTO.getSortOrder()
        );

        // 3. 查询总记录数
        int total = postDao.countArticlesByCondition(
                queryDTO.getTitle(),
                queryDTO.getStatus()
        );

        // 4. 转换为 VO 列表
        List<ArticleVO> articleVOS = postList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 5. 构建分页结果
        PageResult<ArticleVO> result = new PageResult<>(
                total,
                pageNum,
                pageSize,
                articleVOS
        );

        log.info("查询到 {} 篇文章，共 {} 页", total, result.getTotalPages());
        return result;
    }



    /**
     * 根据ID获取文章详情
     */
    @Override
    public ArticleVO getArticleById(int id) {
        log.info("查询文章详情，ID: {}", id);

        Post post = postDao.selectById(id);
        if (post == null) {
            throw new RuntimeException("文章不存在，ID: " + id);
        }

        return convertToVO(post);
    }

    /**
     * Entity 转换为 VO
     * 私有方法：处理数据转换和额外字段
     */
    private ArticleVO convertToVO(Post post) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(post, vo);

        // 设置状态名称
        vo.setStatusName(getStatusName(post.getStatus()));

        return vo;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            default: return "未知";
        }
    }
}
