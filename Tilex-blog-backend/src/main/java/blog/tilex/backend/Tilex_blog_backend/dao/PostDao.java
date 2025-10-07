package blog.tilex.backend.Tilex_blog_backend.dao;

import blog.tilex.backend.Tilex_blog_backend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 文章数据访问层
 */
@Mapper
public interface PostDao {
    // 查询所有文章
    List<Post> selectAll();

    /**
     * 条件查询文章（分页）
     *
     * @param offset    起始行号
     * @param size      每页条数
     * @param title     标题关键字（模糊查询）
     * @param status    状态
     * @param sortField 排序字段
     * @param sortOrder 排序方式（ASC/DESC）
     * @return 当前页文章列表
     */
    List<Post> selectArticlesByCondition(
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("title") String title,
            @Param("status") Integer status,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );


    // 根据ID查询文章
    Post selectById(@Param("id") Integer id);

    // 根据状态查询文章
    List<Post> selectByStatus(@Param("status") Integer status);

    // 插入文章
    int insert(Post post);

    // 更新文章
    int update(Post post);

    // 删除文章
    int deleteById(@Param("id") Integer id);

    // 统计文章总数
    int count();

    /**
     * 查询满足条件的总记录数
     *
     * @param title  标题关键字（模糊查询）
     * @param status 状态
     * @return 总记录数
     */
    int countArticlesByCondition(
            @Param("title") String title,
            @Param("status") Integer status
    );
}
