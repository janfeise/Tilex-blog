package blog.tilex.backend.Tilex_blog_backend.service.impl;

import blog.tilex.backend.Tilex_blog_backend.dao.PostDao;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleQueryDTO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleSearchVO;
import blog.tilex.backend.Tilex_blog_backend.dto.ArticleVO;
import blog.tilex.backend.Tilex_blog_backend.dto.UploadArticleDTO;
import blog.tilex.backend.Tilex_blog_backend.entity.Post;
import blog.tilex.backend.Tilex_blog_backend.service.ArticleService;
import blog.tilex.backend.Tilex_blog_backend.utils.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private PostDao postDao;

    // ==================== 配置参数 ====================

    /** 高亮HTML标签 */
    private static final String HIGHLIGHT_PREFIX = "<span class='highlight'>";
    private static final String HIGHLIGHT_SUFFIX = "</span>";

    /** 每个片段包含的上下文字符数 */
    private static final int SNIPPET_CONTEXT_LENGTH = 60;

    /** 最多返回的片段数量 */
    private static final int MAX_SNIPPETS = 5;

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

    /**
     * 上传文章（创建新文章）
     * 业务逻辑：
     * 1. 参数校验 & 日志记录
     * 2. vo -> Entity转换
     * 3. 设置默认值（status、createAt, updateAt）
     * 4. 调用 DAO 插入数据库
     * 5. 回填 ID 并转换为 VO 返回
     */
    @Override
    public ArticleVO createArticle(UploadArticleDTO uploadArticleDTO) {
        log.info("开始创建文章: {}", uploadArticleDTO);

        // DTO -> Entity 转换
        Post post = new Post();
        BeanUtils.copyProperties(uploadArticleDTO, post);

        // 设置默认值
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(java.time.LocalDateTime.now());
            post.setUpdatedAt(java.time.LocalDateTime.now());
        } else {
            post.setUpdatedAt(post.getCreatedAt());
        }

        // 插入数据到数据库
        postDao.insert(post);

        log.info("文章插入成功，ID： {}", post.getId());

        // 转为 VO 返回给前端
        ArticleVO vo = convertToVO(post);


        return vo;
    }

    // ==================== 公共方法 ====================
    @Override
    public List<ArticleSearchVO> searchArticles(String  keyword) {
        // 1. 参数校验
        if (!StringUtils.hasText(keyword)) {
            log.warn("搜索关键词为空");
            return new ArrayList<>();
        }

//        // 2. 去掉首尾空格
//        keyword = keyword.trim();
//
//        // 3. 转为小写，统一搜索
//        keyword = keyword.toLowerCase();
//        log.info("开始搜索文章， 关键词: {}", keyword);

        // 4. 数据库查询：返回文章的文章对象
        List<Post> posts = postDao.searchArticlesByKeyword(keyword);
        System.out.println(keyword);
        System.out.println(posts.size());
        log.info("数据库查询完成，找到 {} 篇文章", posts.size());

        // 5. VO层处理：提取关键词所在的片段、高亮、封装
        List<ArticleSearchVO> voList = new ArrayList<>();
        for (Post post : posts) {
            ArticleSearchVO vo = convertToSearchVO(post, keyword);
            voList.add(vo);
        }

        log.info("Vo封装完成，返回 {} 条搜索", voList.size());
        return voList;
    }

    // ==================== 私有方法 - VO层核心处理逻辑 ====================

    /**
     * 将 Article 实体转换为 ArticleSearchVO
     * 核心步骤:
     * 1. 基础字段复制
     * 2. 标题高亮处理
     * 3. 从content中提取包含关键词的片段
     * 4. 片段高亮处理
     * 5. 判断匹配位置(标题/内容/都匹配)
     */
    private ArticleSearchVO convertToSearchVO(Post post, String keyword) {
        ArticleSearchVO vo = new ArticleSearchVO();

        // 1. 复制基础字段
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());

        // 2. 高亮片段
        List<String> snippets = extractSnippets(post.getContent(), keyword);
        vo.setSnippets(snippets);

        return  vo;
    }

    /**
     * 从文章内容中提取包含关键词的片段
     *
     * 算法步骤:
     * 1. 使用正则表达式查找所有关键词位置
     * 2. 对每个位置,提取前后各N个字符作为上下文
     * 3. 添加省略号"..."
     * 4. 对关键词进行高亮处理
     * 5. 最多返回M个片段
     *
     * @param content 完整的文章内容
     * @param keyword 搜索关键词
     * @return 高亮后的片段数组
     */
    private List<String> extractSnippets(String content, String keyword) {
        List<String> snippets = new ArrayList<>();

        if (!StringUtils.hasText(content) || !StringUtils.hasText(keyword)) {
            return snippets;
        }

        try {
            // 转义正则特殊字符
            String escapedKeyword = Pattern.quote(keyword);

            // 不区分大小写匹配
            Pattern pattern = Pattern.compile(escapedKeyword, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            int count = 0;
            while (matcher.find() && count < MAX_SNIPPETS) {
                int matchStart = matcher.start();
                int matchEnd = matcher.end();

                // 计算片段起始位置(向前取N个字符)
                int snippetStart = Math.max(0, matchStart - SNIPPET_CONTEXT_LENGTH);

                // 计算片段结束位置(向后取N个字符)
                int snippetEnd = Math.min(content.length(), matchEnd + SNIPPET_CONTEXT_LENGTH);

                // 提取片段
                String snippet = content.substring(snippetStart, snippetEnd);

                // 添加省略号
                StringBuilder sb = new StringBuilder();
                if (snippetStart > 0) {
                    sb.append("...");
                }
                sb.append(snippet);
                if (snippetEnd < content.length()) {
                    sb.append("...");
                }

                // 高亮关键词
                String highlightedSnippet = highlightKeyword(sb.toString(), keyword);

                snippets.add(highlightedSnippet);
                count++;
            }

            // 如果没有找到匹配(理论上不会发生,因为数据库已经过滤),返回文章开头
            if (snippets.isEmpty()) {
                String preview = content.substring(0, Math.min(content.length(), 150));
                if (content.length() > 150) {
                    preview += "...";
                }
                snippets.add(preview);
            }

        } catch (Exception e) {
            log.error("提取片段失败, keyword: {}", keyword, e);
            // 降级处理: 返回文章开头
            String preview = content.substring(0, Math.min(content.length(), 150));
            if (content.length() > 150) {
                preview += "...";
            }
            snippets.add(preview);
        }

        return snippets;
    }

    /**
     * 关键词高亮处理
     * 将关键词包裹在 <span class='highlight'> 标签中
     *
     * @param text 原始文本
     * @param keyword 关键词
     * @return 高亮后的文本
     */
    private String highlightKeyword(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return text;
        }

        try {
            // 转义特殊字符
            String escapedKeyword = Pattern.quote(keyword);

            // 不区分大小写替换
            Pattern pattern = Pattern.compile(escapedKeyword, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                // 保留原始大小写
                String matched = matcher.group();
                matcher.appendReplacement(sb, HIGHLIGHT_PREFIX + matched + HIGHLIGHT_SUFFIX);
            }
            matcher.appendTail(sb);

            return sb.toString();

        } catch (Exception e) {
            log.error("关键词高亮失败, keyword: {}", keyword, e);
            return text;
        }
    }

    /**
     * 判断文本是否包含关键词(不区分大小写)
     */
    private boolean containsIgnoreCase(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        return text.toLowerCase().contains(keyword.toLowerCase());
    }
}
