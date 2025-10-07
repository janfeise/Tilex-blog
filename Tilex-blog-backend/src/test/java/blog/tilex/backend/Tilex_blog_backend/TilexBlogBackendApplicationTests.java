package blog.tilex.backend.Tilex_blog_backend;

import blog.tilex.backend.Tilex_blog_backend.dao.PostDao;
import blog.tilex.backend.Tilex_blog_backend.dao.TestDao;
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

	@Test
	public void testSelectAll() throws Exception {
		System.out.println(postDao.selectAll());
	}
}
