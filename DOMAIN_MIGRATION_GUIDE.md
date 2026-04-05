# 🌍 域名迁移操作指南

**文档版本**：v1.0  
**创建日期**：2026-03-20  
**目标**：当 OSS 域名改变时，如何快速处理所有文章中的图片链接

---

## 📋 当前现状回顾

### 为什么需要这份文档？

**现状**：使用 Typora → PicList → Alist 的自动化图床方案

```
Typora (编辑)
  ↓ 
PicList (上传)
  ↓ WebDAV
Alist (存储)
  ↓ 返回完整 URL
文章内容：![](http://oss.tilex.world/d/blog-images/2026/03/20/image-xxx.png?sign=...)
                    ↑ 硬编码在 Markdown 内容中
```

**问题**：URL 硬编码在文章 Markdown 内容中 🔴

```
如果域名从 http://oss.tilex.world 改为 https://oss.newdomain.com
  ├─ 所有文章的图片链接都失效 ❌
  ├─ 需要批量修改数据库 ⚠️
  └─ 本文档就是来解决这个问题的
```

---

## 🎯 快速操作清单（5 分钟搞定）

### 假设场景

```
旧域名：http://oss.tilex.world
新域名：https://oss.newdomain.com
```

### 操作步骤

#### 1️⃣ SSH 连接服务器

```bash
ssh root@your-server
```

#### 2️⃣ 备份数据库（关键！）

```bash
# 进入 MySQL 容器
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<你的密码> << EOF
-- 备份 articles 表
CREATE TABLE articles_backup_20260320 AS SELECT * FROM articles;

-- 验证备份
SELECT COUNT(*) FROM articles_backup_20260320;
EOF
```

#### 3️⃣ 执行域名替换

```bash
# 替换所有文章中的旧域名
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<你的密码> << EOF
USE tilex_blog;

-- 关键 SQL：替换文章内容中的 URL
UPDATE articles 
SET content = REPLACE(
    content,
    'http://oss.tilex.world',
    'https://oss.newdomain.com'
)
WHERE content LIKE '%oss.tilex.world%';

-- 验证修改
SELECT COUNT(*) as modified_count FROM articles WHERE content LIKE '%oss.newdomain.com%';
EOF
```

#### 4️⃣ 验证修改

```bash
# 查看修改前后的对比（从 bash 查询也可以）
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<你的密码> << EOF
USE tilex_blog;

-- 验证：应该没有任何文章还含有旧域名
SELECT COUNT(*) as still_using_old_domain FROM articles WHERE content LIKE '%oss.tilex.world%';
-- 结果应该是 0

-- 验证：应该有 N 篇文章使用了新域名
SELECT COUNT(*) as using_new_domain FROM articles WHERE content LIKE '%oss.newdomain.com%';
EOF
```

#### 5️⃣ 重启应用（让改动生效）

```bash
# 重启 Spring Boot 应用（如有缓存机制）
docker restart tilex-blog-springboot

# 验证是否启动成功
docker logs tilex-blog-springboot
```

#### 6️⃣ 验证最终效果

```bash
# 访问博客，查看文章中的图片是否能正常加载
# 在浏览器打开：https://你的博客域名/articles/1
# 检查图片是否显示正常
```

---

## 📊 操作细节深度解析

### 为什么要备份？

```sql
-- 备份的作用：出问题时可以快速回滚
CREATE TABLE articles_backup_20260320 AS SELECT * FROM articles;

-- 回滚命令（如需要）：
RESTORE TABLE articles FROM articles_backup_20260320;
-- 或
UPDATE articles SET content = (SELECT content FROM articles_backup_20260320 WHERE id = articles.id);
```

**备份时机**：替换前必须做！

---

### SQL 替换命令详解

```sql
UPDATE articles 
SET content = REPLACE(
    content,                           -- 要修改的字段
    'http://oss.tilex.world',         -- 旧值（查找）
    'https://oss.newdomain.com'       -- 新值（替换为）
)
WHERE content LIKE '%oss.tilex.world%';  -- 条件：只修改包含旧域名的记录
```

**关键点**：
- `content LIKE '%oss.tilex.world%'` 可以提高查询效率
- `REPLACE()` 会替换所有出现的位置（包括多个图片的 URL）
- 如果有多篇文章包含该域名，会全部修改 ✅

---

### 检验修改是否成功

```sql
-- 方法1：查看是否还有旧域名
SELECT id, title, content 
FROM articles 
WHERE content LIKE '%oss.tilex.world%' 
LIMIT 5;
-- 结果应该为空 ✅

-- 方法2：查看新域名的使用数量
SELECT id, title 
FROM articles 
WHERE content LIKE '%oss.newdomain.com%' 
LIMIT 5;
-- 应该看到你所有的文章 ✅

-- 方法3：数一下修改了多少篇
SELECT COUNT(*) as total_modified 
FROM articles 
WHERE content LIKE '%oss.newdomain.com%';
```

---

## ⚠️ 常见问题与解决

### 问题 1：MySQL 密码忘了怎么办？

**症状**：执行 SQL 时提示 `Access denied`

```bash
# 1. 查看 1Panel 中 MySQL 的密码设置
# 打开 1Panel 后台 → 应用 → MySQL → 查看密码

# 或从容器环境变量读取
docker inspect 1Panel-mysql-wrVt | grep MYSQL_ROOT_PASSWORD

# 2. 如果都不行，可以进入 MySQL 容器重置
docker exec -it 1Panel-mysql-wrVt bash
mysql -uroot -p  # 输入密码
# 然后执行 SQL 命令
```

---

### 问题 2：REPLACE 会不会替换错东西？

**例如**：如果某篇文章的内容里恰好提到了"oss.tilex.world"文字

**解决**：限制替换范围，只替换 URL 格式

```sql
-- 更安全的做法：只替换 URL 结构
UPDATE articles 
SET content = REGEXP_REPLACE(
    content,
    'http://oss\\.tilex\\.world/d/',
    'https://oss.newdomain.com/d/'
)
WHERE content REGEXP 'http://oss\\.tilex\\.world/d/';
```

---

### 问题 3：如何验证图片是否真的可以访问？

**方法 1：数据库查询**

```bash
# 从 MySQL 获取一个图片 URL
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<密码> << EOF
USE tilex_blog;
SELECT id, content FROM articles LIMIT 1 \G
EOF

# 复制 URL 到浏览器访问
```

**方法 2：检查 Nginx 日志**

```bash
# 查看 Nginx 反向代理是否正常转发
docker logs 1Panel-openresty-P3eV | tail -20

# 查找包含新域名的请求
docker logs 1Panel-openresty-P3eV | grep "oss.newdomain.com"
```

**方法 3：测试 curl**

```bash
# 直接请求验证 URL 是否可访问
curl -I "https://oss.newdomain.com/d/blog-images/2026/03/20/image-xxx.png"

# 返回 200 OK 说明正常
# 返回 404 说明文件不存在
# 返回 其他 说明需要排查
```

---

## 🔄 完整操作流程（图解）

```
┌─────────────────────────────────────────┐
│ 步骤 1：SSH 连接服务器                   │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 步骤 2：备份数据库                       │
│ CREATE TABLE articles_backup_20260320   │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 步骤 3：执行 REPLACE 替换                │
│ UPDATE articles SET content = REPLACE() │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 步骤 4：验证修改                         │
│ SELECT COUNT(*) WHERE content LIKE ...  │
│ 旧域名数量应为 0 ✅                     │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 步骤 5：重启应用                         │
│ docker restart tilex-blog-springboot    │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 步骤 6：浏览器验证                       │
│ 打开博客，检查图片是否加载               │
│                                         │
│ ✅ 都正常 → 完成！                      │
│ ❌ 有问题 → 看回滚方案                  │
└─────────────────────────────────────────┘
```

---

## 🔙 回滚方案（如出错）

### 情况 1：修改后图片都失效了

```bash
# 立即回滚到备份
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<密码> << EOF
USE tilex_blog;

-- 恢复备份
UPDATE articles 
SET content = (
    SELECT content FROM articles_backup_20260320 
    WHERE articles_backup_20260320.id = articles.id
);

-- 验证回滚成功
SELECT COUNT(*) FROM articles WHERE content LIKE '%oss.tilex.world%';
-- 结果应该 > 0，表示恢复了旧域名
EOF

# 重启应用
docker restart tilex-blog-springboot
```

### 情况 2：只有某些文章出错

```bash
# 只回滚有问题的文章
docker exec -i 1Panel-mysql-wrVt mysql -uroot -p<密码> << EOF
USE tilex_blog;

-- 查看哪些文章有问题
SELECT id, title FROM articles WHERE content LIKE '%404%';

-- 只恢复这些文章
UPDATE articles 
SET content = (SELECT content FROM articles_backup_20260320 
               WHERE articles_backup_20260320.id = articles.id)
WHERE id IN (1, 2, 3);  -- 替换为有问题的文章 ID
EOF
```

---

## 📋 完整的一键脚本

如果觉得上面太复杂，可以直接用这个脚本：

### 创建脚本文件

```bash
# SSH 到服务器后
cat > /tmp/migrate-domain.sh << 'EOF'
#!/bin/bash

# 域名迁移脚本
OLD_DOMAIN="http://oss.tilex.world"
NEW_DOMAIN="https://oss.newdomain.com"
DB_PASSWORD="your_mysql_password"  # ← 改成你的密码
MYSQL_CONTAINER="1Panel-mysql-wrVt"
DB_NAME="tilex_blog"

echo "🚀 开始域名迁移"
echo "旧域名: $OLD_DOMAIN"
echo "新域名: $NEW_DOMAIN"
echo ""

# 步骤 1：备份
echo "1️⃣ 备份数据库..."
docker exec -i $MYSQL_CONTAINER mysql -uroot -p$DB_PASSWORD << SQL
CREATE TABLE ${DB_NAME}.articles_backup_$(date +%Y%m%d_%H%M%S) AS SELECT * FROM ${DB_NAME}.articles;
SQL
echo "✅ 备份完成"
echo ""

# 步骤 2：替换
echo "2️⃣ 替换域名..."
docker exec -i $MYSQL_CONTAINER mysql -uroot -p$DB_PASSWORD << SQL
USE $DB_NAME;
UPDATE articles SET content = REPLACE(content, '$OLD_DOMAIN', '$NEW_DOMAIN');
SQL
echo "✅ 替换完成"
echo ""

# 步骤 3：验证
echo "3️⃣ 验证修改..."
OLD_COUNT=$(docker exec -i $MYSQL_CONTAINER mysql -uroot -p$DB_PASSWORD << SQL
USE $DB_NAME;
SELECT COUNT(*) FROM articles WHERE content LIKE '%$OLD_DOMAIN%';
SQL
)
echo "还在使用旧域名的文章数: $OLD_COUNT"

NEW_COUNT=$(docker exec -i $MYSQL_CONTAINER mysql -uroot -p$DB_PASSWORD << SQL
USE $DB_NAME;
SELECT COUNT(*) FROM articles WHERE content LIKE '%$NEW_DOMAIN%';
SQL
)
echo "已更换新域名的文章数: $NEW_COUNT"
echo ""

# 步骤 4：重启应用
echo "4️⃣ 重启应用..."
docker restart tilex-blog-springboot
sleep 5
echo "✅ 应用已重启"
echo ""

echo "🎉 域名迁移完成！"
EOF

chmod +x /tmp/migrate-domain.sh
```

### 运行脚本

```bash
/tmp/migrate-domain.sh
```

---

## 🎯 检查清单

迁移完成后，按照这个清单验证：

```
迁移前检查：
  ├─ [ ] 已备份数据库
  ├─ [ ] 确认旧域名: http://oss.tilex.world
  └─ [ ] 确认新域名: https://oss.newdomain.com

迁移中检查：
  ├─ [ ] SQL 替换命令已执行
  ├─ [ ] 验证：旧域名数量为 0
  └─ [ ] 验证：新域名数量 > 0

迁移后检查：
  ├─ [ ] 应用已重启
  ├─ [ ] 在浏览器打开博客
  ├─ [ ] 检查第一篇文章的图片是否显示
  ├─ [ ] 检查 Nginx 日志无 4xx 错误
  ├─ [ ] 用 curl 验证 URL 可访问（返回 200）
  └─ [ ] 删除备份表（可选）

故障排查（如需要）：
  ├─ [ ] 确认 Nginx 反向代理配置无误
  ├─ [ ] 确认 Alist 正常运行
  ├─ [ ] 查看 Alist 是否需要重新生成签名
  └─ [ ] 如一切失败，执行回滚脚本
```

---

## 💡 预防措施（现在就做）

### 措施 1：创建配置文件

在你的项目中添加一个配置文件，便于未来快速修改：

```properties
# config/oss.properties
oss.domain=http://oss.tilex.world
oss.alist.path=/d/blog-images

# 以后改域名时，只需改这一处
# oss.domain=https://oss.newdomain.com
```

### 措施 2：定期备份

```bash
# 每周自动备份
0 2 * * 0  backupdb.sh  # 每周日 凌晨 2 点执行
```

### 措施 3：记录 URL 格式规范

```markdown
## URL 格式规范

所有图片 URL 格式：
{domain}/d/blog-images/{year}/{month}/{day}/{filename}

示例：
http://oss.tilex.world/d/blog-images/2026/03/20/image-xxx.png

域名前缀：http://oss.tilex.world
存储路径：/d/blog-images/2026/03/20/
文件名：   image-xxx.png
```

---

## 🆘 如何联系技术支持

如果遇到问题，请提供以下信息：

1. **错误现象**：图片加载不了？还是其他?
2. **错误日志**：（见下方获取日志的方法）
3. **修改内容**：从什么域名改到什么域名

### 获取相关日志

```bash
# MySQL 日志
docker logs 1Panel-mysql-wrVt | tail -50

# Nginx 日志
docker logs 1Panel-openresty-P3eV | tail -50

# Alist 日志
docker logs alist | tail -50

# Spring Boot 日志
docker logs tilex-blog-springboot | tail -50
```

---

## 📌 总结

| 步骤 | 操作 | 耗时 |
|-----|------|------|
| 1 | SSH 连接 | 2 分钟 |
| 2 | 备份数据库 | 1 分钟 |
| 3 | 执行 SQL 替换 | 1 分钟 |
| 4 | 验证修改 | 1 分钟 |
| 5 | 重启应用 | 1 分钟 |
| 6 | 浏览器测试 | 2 分钟 |
| **总计** | | **8 分钟** |

---

## 📚 相关文档

- [MEDIA_SYSTEM_SOLUTION.md](MEDIA_SYSTEM_SOLUTION.md) - 完整的媒体系统方案
- [应用部署指南](./docs/deployment.md) - 应用部署和配置

---

**最后更新**：2026-03-20  
**下次审视**：当真的要改域名时 📅
