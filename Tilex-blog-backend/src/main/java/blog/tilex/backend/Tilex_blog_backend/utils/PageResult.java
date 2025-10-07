package blog.tilex.backend.Tilex_blog_backend.utils;

import lombok.Data;

import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {
    private Long total;        // 总记录数
    private Integer pageNum;   // 当前页
    private Integer pageSize;  // 每页大小
    private Integer totalPages; // 总页数
    private List<T> records;   // 数据列表

    public PageResult(long total, int pageNum, int pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    public long getTotalPages() {
        return (total + pageSize - 1) / pageSize;
    }
}