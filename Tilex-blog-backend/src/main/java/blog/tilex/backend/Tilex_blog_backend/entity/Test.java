package blog.tilex.backend.Tilex_blog_backend.entity;

import java.util.Date;

public class Test {
    private Long id;           // 对应数据库的 id
    private String name;       // 对应数据库的 name
    private String email;      // 对应数据库的 email
    private Date createAt;     // 对应数据库的 create_at (驼峰命名)

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    // 添加 toString 方便查看结果
    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}