package blog.tilex.backend.Tilex_blog_backend;

import blog.tilex.backend.Tilex_blog_backend.dao.PostDao;
import blog.tilex.backend.Tilex_blog_backend.dao.TestDao;
import blog.tilex.backend.Tilex_blog_backend.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TilexBlogBackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private TestDao testDao;

	@Autowired
	private PostDao postDao;

	/**
	 * 测试类
	 * */
	@Test
	public void testGetDocById() throws Exception {
		try {
			long id = 1;
			System.out.println(testDao.getDocById(id));
		}  catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("查询失败", e);
		}
	}

	/**
	 * 通过id查询文章
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetPostById() throws Exception {
		try {
			int id = 1;
			System.out.println(postDao.selectById(id));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("查询失败", e);
		}
	}

	/**
	 * 查询所有文章
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectAll() throws Exception {
		System.out.println(postDao.selectAll());
	}

	/**
	 * 插入文章
	 */
	@Test
	public void testInsert() throws Exception {
		Post post = new Post();
		post.setTitle("插入文章");
		post.setContent("测试：插入文章是否成功");

		postDao.insert(post);

		System.out.println(post.getId());
	}

	/**
	 * 根据关键词搜索文章
	 */
	@Test
	public void testSearchArticlesByKeyword() throws Exception {
		String keyword = "Android";

		System.out.println(postDao.searchArticlesByKeyword(keyword).size());
	}
}
