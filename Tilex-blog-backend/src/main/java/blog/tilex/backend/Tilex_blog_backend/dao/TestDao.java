package blog.tilex.backend.Tilex_blog_backend.dao;

import org.apache.ibatis.annotations.Param;

public interface TestDao {
    /**
     * 通过id查询数据
     */
    blog.tilex.backend.Tilex_blog_backend.entity.Test getDocById(@Param("docId") long docId) throws Exception;
}