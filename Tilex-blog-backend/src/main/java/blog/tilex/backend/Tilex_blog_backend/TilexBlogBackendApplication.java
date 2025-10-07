package blog.tilex.backend.Tilex_blog_backend;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("blog.tilex.backend.Tilex_blog_backend.dao")
public class TilexBlogBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TilexBlogBackendApplication.class, args);
	}

	@Bean
	public ConfigurationCustomizer mybatisConfigurationCustomizer() {
		return new ConfigurationCustomizer() {
			@Override
			public void customize(Configuration configuration) {
				configuration.setMapUnderscoreToCamelCase(true);
			}
		};
	}
}