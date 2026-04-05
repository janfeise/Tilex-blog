# 画廊数据库操作说明文档

## 一、数据库基本信息

### 1.1 数据库信息
- **数据库名**：`gallery_db`（建议名称，可自定义）
- **字符集**：`utf8mb4` （支持表情符号和多国语言）
- **排序方式**：`utf8mb4_unicode_ci` （Unicode不区分大小写排序）
- **后端框架**：Spring Boot JPA / MyBatis
- **ORM方案**：推荐使用Spring Data JPA或MyBatis Plus

### 1.2 表关系图
```
gallery_images (图片表)
    ↓
    ├── 1对多 ← gallery_image_tags (关联表)
    │                ↓
    └────────────────┘
                ↓
         gallery_tags (标签表)
```

---

## 二、数据表详细说明

### 2.1 gallery_images（图片表）

#### 表结构
```sql
CREATE TABLE `gallery_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片唯一标识',
  `title` VARCHAR(255) COMMENT '图片标题',
  `description` TEXT COMMENT '图片描述',
  `image_url` VARCHAR(512) NOT NULL UNIQUE COMMENT '完整图片URL',
  `alist_path` VARCHAR(512) COMMENT 'AList相对路径如:/d/blog-images/2026/03/20/xxx.png',
  `alist_domain` VARCHAR(255) COMMENT 'AList域名,如:oss.tilex.world',
  `source_type` VARCHAR(50) COMMENT '图片来源类型：alist/local/cdn',
  `has_sign_param` TINYINT DEFAULT 1 COMMENT '是否包含签名参数：1=是,0=否',
  `status` TINYINT DEFAULT 0 COMMENT '图片状态：0=启用,1=禁用',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标识：0=正常,1=已删除',
  `sort_order` INT COMMENT '排序字段',
  INDEX `idx_is_deleted` (`is_deleted`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_sort_order` (`sort_order`),
  INDEX `idx_status` (`status`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='画廊图片表';
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | BIGINT | ✓ | 自增主键 |
| `title` | VARCHAR(255) | ✗ | 图片标题，长度不超过255，可为空 |
| `description` | TEXT | ✗ | 图片详细描述，支持长文本 |
| `image_url` | VARCHAR(512) | ✓ | 完整URL，具有唯一约束防重复 |
| `alist_path` | VARCHAR(512) | ✗ | AList相对路径，用于迁移 |
| `alist_domain` | VARCHAR(255) | ✗ | 当前AList域名，便于后续更换 |
| `source_type` | VARCHAR(50) | ✗ | 来源类型标记，便于分类处理 |
| `has_sign_param` | TINYINT | ✗ | 是否含签名参数，默认1 |
| `status` | TINYINT | ✓ | 图片状态，0=启用 1=禁用，默认0 |
| `created_at` | TIMESTAMP | ✓ | 自动记录创建时间 |
| `updated_at` | TIMESTAMP | ✓ | 自动记录更新时间 |
| `is_deleted` | TINYINT | ✓ | 软删除标识，默认0 |
| `sort_order` | INT | ✗ | 排序序号，用于自定义展示顺序 |

---

### 2.2 gallery_tags（标签表）

#### 表结构
```sql
CREATE TABLE `gallery_tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签唯一标识',
  `tag_name` VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名称',
  `tag_description` TEXT COMMENT '标签描述',
  `color_code` VARCHAR(7) DEFAULT '#999999' COMMENT '标签颜色代码,如:#FF6B6B',
  `sort_order` INT COMMENT '标签排序',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标识：0=正常,1=已删除',
  INDEX `idx_tag_name` (`tag_name`),
  INDEX `idx_is_deleted` (`is_deleted`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='画廊标签表';
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | BIGINT | ✓ | 自增主键 |
| `tag_name` | VARCHAR(100) | ✓ | 标签名称，唯一约束防重复 |
| `tag_description` | TEXT | ✗ | 标签详细描述，支持Markdown |
| `color_code` | VARCHAR(7) | ✓ | 十六进制颜色码，用于UI渲染 |
| `sort_order` | INT | ✗ | 标签展示顺序 |
| `created_at` | TIMESTAMP | ✓ | 自动记录创建时间 |
| `updated_at` | TIMESTAMP | ✓ | 自动记录更新时间 |
| `is_deleted` | TINYINT | ✓ | 软删除标识，默认0 |

---

### 2.3 gallery_image_tags（关联表）

#### 表结构
```sql
CREATE TABLE `gallery_image_tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录标识',
  `image_id` BIGINT NOT NULL COMMENT '图片ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_image_tag` (`image_id`, `tag_id`) COMMENT '同一图片不重复添加标签',
  FOREIGN KEY (`image_id`) REFERENCES `gallery_images`(`id`) ON DELETE CASCADE COMMENT '删除图片时级联删除关联',
  FOREIGN KEY (`tag_id`) REFERENCES `gallery_tags`(`id`) ON DELETE RESTRICT COMMENT '删除标签时限制（防删除）',
  INDEX `idx_tag_id` (`tag_id`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片-标签多对多关联表';
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | BIGINT | ✓ | 自增主键 |
| `image_id` | BIGINT | ✓ | 关联的图片ID，外键 |
| `tag_id` | BIGINT | ✓ | 关联的标签ID，外键 |
| `created_at` | TIMESTAMP | ✓ | 自动记录创建时间 |
| `updated_at` | TIMESTAMP | ✓ | 自动记录更新时间 |

#### 约束说明
- **唯一约束**：`(image_id, tag_id)` 组合唯一，防止同一图片被添加重复标签
- **级联删除**：删除图片时自动删除所有关联的标签关系
- **限制删除**：删除标签时，若有图片关联则拒绝删除（需使用软删除）

---

## 三、数据库初始化

### 3.1 一键初始化脚本
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `gallery_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `gallery_db`;

-- 创建图片表
CREATE TABLE `gallery_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(255),
  `description` TEXT,
  `image_url` VARCHAR(512) NOT NULL UNIQUE,
  `alist_path` VARCHAR(512),
  `alist_domain` VARCHAR(255),
  `source_type` VARCHAR(50),
  `has_sign_param` TINYINT DEFAULT 1,
  `status` TINYINT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  `sort_order` INT,
  INDEX `idx_is_deleted` (`is_deleted`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_sort_order` (`sort_order`),
  INDEX `idx_status` (`status`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建标签表
CREATE TABLE `gallery_tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tag_name` VARCHAR(100) NOT NULL UNIQUE,
  `tag_description` TEXT,
  `color_code` VARCHAR(7) DEFAULT '#999999',
  `sort_order` INT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  INDEX `idx_tag_name` (`tag_name`),
  INDEX `idx_is_deleted` (`is_deleted`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建关联表
CREATE TABLE `gallery_image_tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `image_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_image_tag` (`image_id`, `tag_id`),
  FOREIGN KEY (`image_id`) REFERENCES `gallery_images`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `gallery_tags`(`id`) ON DELETE RESTRICT,
  INDEX `idx_tag_id` (`tag_id`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引优化查询
CREATE INDEX `idx_image_created` ON `gallery_images`(`created_at`, `is_deleted`);
CREATE INDEX `idx_image_status_deleted` ON `gallery_images`(`status`, `is_deleted`);
CREATE INDEX `idx_tag_sort` ON `gallery_tags`(`sort_order`, `is_deleted`);
```

---

## 三、数据库升级迁移（仅对已有数据库）

如果您已经存在 `gallery_images` 表但不含 `status` 字段，请执行以下升级脚本：

```sql
-- 添加 status 字段
ALTER TABLE `gallery_images` 
ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '图片状态：0=启用,1=禁用' 
AFTER `has_sign_param`;

-- 添加 status 字段索引
CREATE INDEX `idx_status` ON `gallery_images`(`status`);

-- 创建组合索引优化查询性能
CREATE INDEX `idx_image_status_deleted` ON `gallery_images`(`status`, `is_deleted`);

-- 验证迁移结果
SELECT COUNT(*) as total,
       SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as enabled_count,
       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as disabled_count
FROM gallery_images;
```

**迁移说明**：
- 执行上述脚本后，所有现存图片的 status 默认为 0（启用）
- 不需要更新现有数据，所有图片将继续正常显示
- 新增的索引会优化后续的查询性能

---

## 四、CRUD 操作指南

### 4.1 CREATE（创建数据）

#### 添加单张图片
```sql
INSERT INTO gallery_images (
  title, 
  description, 
  image_url, 
  alist_path,
  alist_domain,
  source_type,
  has_sign_param,
  sort_order
) VALUES (
  '山水风景图',
  '这是一张美丽的山水风景图片',
  'http://oss.tilex.world/d/blog-images/2026/03/20/image-20260320152531281.png?sign=xxx',
  '/d/blog-images/2026/03/20/image-20260320152531281.png',
  'oss.tilex.world',
  'alist',
  1,
  1
);
```

#### 批量添加图片
```sql
INSERT INTO gallery_images (title, image_url, alist_path, alist_domain, source_type, sort_order)
VALUES 
  ('图片1', 'http://oss.tilex.world/d/images/pic1.png', '/d/images/pic1.png', 'oss.tilex.world', 'alist', 1),
  ('图片2', 'http://oss.tilex.world/d/images/pic2.png', '/d/images/pic2.png', 'oss.tilex.world', 'alist', 2),
  ('图片3', 'http://oss.tilex.world/d/images/pic3.png', '/d/images/pic3.png', 'oss.tilex.world', 'alist', 3);
```

#### 添加标签
```sql
INSERT INTO gallery_tags (tag_name, tag_description, color_code, sort_order)
VALUES 
  ('风景', '自然风景及美感风光', '#FF6B6B', 1),
  ('建筑', '建筑与人工结构摄影', '#4ECDC4', 2),
  ('人物', '人物肖像创容', '#FFE66D', 3);
```

#### 为图片添加标签
```sql
-- 为图片ID=1添加标签ID=1和标签ID=2
INSERT INTO gallery_image_tags (image_id, tag_id)
VALUES 
  (1, 1),  -- 添加"风景"标签
  (1, 2);  -- 添加"建筑"标签

-- 如果标签已存在，忽略重复错误
INSERT IGNORE INTO gallery_image_tags (image_id, tag_id)
VALUES (1, 1);
```

---

### 4.2 READ（读取数据）

#### 获取所有图片（分页）
```sql
-- 获取第1页，每页20条（只返回启用的图片）
SELECT 
  id, title, description, image_url, 
  created_at, updated_at
FROM gallery_images
WHERE is_deleted = 0 AND status = 0
ORDER BY sort_order ASC, created_at DESC
LIMIT 0, 20;
```

#### 获取单张图片完整信息
```sql
SELECT 
  gi.id, gi.title, gi.description, 
  gi.image_url, gi.alist_path, gi.alist_domain,
  gi.status, gi.created_at, gi.updated_at,
  GROUP_CONCAT(gt.id) as tag_ids,
  GROUP_CONCAT(gt.tag_name) as tag_names,
  GROUP_CONCAT(gt.tag_description) as tag_descriptions,
  GROUP_CONCAT(gt.color_code) as color_codes
FROM gallery_images gi
LEFT JOIN gallery_image_tags git ON gi.id = git.image_id
LEFT JOIN gallery_tags gt ON git.tag_id = gt.id AND gt.is_deleted = 0
WHERE gi.id = 1 AND gi.is_deleted = 0
GROUP BY gi.id;
```

#### 查询具有相同标签的关联图片
```sql
-- 查询与图片ID=1有共同标签的其他启用图片
SELECT DISTINCT
  gi2.id, gi2.title, gi2.image_url,
  gt.id as tag_id, gt.tag_name, gt.color_code
FROM gallery_images gi1
INNER JOIN gallery_image_tags git1 ON gi1.id = git1.image_id
INNER JOIN gallery_tags gt ON git1.tag_id = gt.id AND gt.is_deleted = 0
INNER JOIN gallery_image_tags git2 ON gt.id = git2.tag_id
INNER JOIN gallery_images gi2 ON git2.image_id = gi2.id 
  AND gi2.id != gi1.id AND gi2.is_deleted = 0 AND gi2.status = 0
WHERE gi1.id = 1 AND gi1.is_deleted = 0
ORDER BY git2.created_at DESC;
```

#### 获取标签下的所有图片
```sql
-- 查询标签"风景"下的所有启用图片
SELECT 
  gi.id, gi.title, gi.image_url, gi.created_at
FROM gallery_images gi
INNER JOIN gallery_image_tags git ON gi.id = git.image_id
INNER JOIN gallery_tags gt ON git.tag_id = gt.id
WHERE gt.tag_name = '风景' 
  AND gi.is_deleted = 0 
  AND gi.status = 0
  AND gt.is_deleted = 0
ORDER BY gi.sort_order ASC;
```

#### 获取所有标签（带启用图片计数）
```sql
SELECT 
  gt.id, gt.tag_name, gt.tag_description, 
  gt.color_code, gt.sort_order,
  COUNT(gi.id) as image_count
FROM gallery_tags gt
LEFT JOIN gallery_image_tags git ON gt.id = git.tag_id
LEFT JOIN gallery_images gi ON git.image_id = gi.id AND gi.is_deleted = 0 AND gi.status = 0
WHERE gt.is_deleted = 0
GROUP BY gt.id
ORDER BY gt.sort_order ASC;
```

#### 搜索图片（按标题或描述）
```sql
SELECT 
  id, title, description, image_url, created_at
FROM gallery_images
WHERE is_deleted = 0 
  AND status = 0
  AND (title LIKE '%风景%' OR description LIKE '%风景%')
ORDER BY created_at DESC
LIMIT 0, 20;
```

---

### 4.3 UPDATE（更新数据）

#### 更新图片基本信息
```sql
UPDATE gallery_images
SET 
  title = '新标题',
  description = '新描述'
WHERE id = 1;
```

#### 禁用/启用图片
```sql
-- 禁用图片（禁用后，后端返回画廊图片时不会返回此图片）
UPDATE gallery_images
SET status = 1
WHERE id = 1;

-- 启用图片
UPDATE gallery_images
SET status = 0
WHERE id = 1;

-- 批量禁用查询结果中的某些图片
UPDATE gallery_images
SET status = 1
WHERE id IN (SELECT image_id FROM gallery_image_tags WHERE tag_id = 10);
```

#### 更新图片URL（迁移用）
```sql
-- 更换AList域名，但保留路径
UPDATE gallery_images
SET 
  alist_domain = 'gallery.example.com'
WHERE alist_domain = 'oss.tilex.world';
```

#### 批量更新排序
```sql
UPDATE gallery_images
SET sort_order = 1
WHERE id = 5;

UPDATE gallery_images
SET sort_order = 2
WHERE id = 3;
```

#### 更新标签信息
```sql
UPDATE gallery_tags
SET 
  tag_description = '更新的标签描述',
  color_code = '#FF0000'
WHERE tag_name = '风景';
```

#### 为图片替换标签
```sql
-- 步骤1：删除图片原有的标签
DELETE FROM gallery_image_tags
WHERE image_id = 1;

-- 步骤2：添加新标签
INSERT INTO gallery_image_tags (image_id, tag_id)
VALUES 
  (1, 2),
  (1, 3);
```

---

### 4.4 DELETE（删除数据）- 使用软删除

#### 软删除图片
```sql
-- 推荐方式：软删除（逻辑删除）
UPDATE gallery_images
SET is_deleted = 1
WHERE id = 1;

-- 恢复已删除的图片
UPDATE gallery_images
SET is_deleted = 0
WHERE id = 1;
```

#### 软删除标签
```sql
-- 软删除标签，关联关系保留
UPDATE gallery_tags
SET is_deleted = 1
WHERE id = 1;
```

#### 彻底删除图片（谨慎使用）
```sql
-- 删除前备份数据！
-- 步骤1：删除关联关系（会自动级联删除，无需手动）
-- 步骤2：删除图片记录
DELETE FROM gallery_images WHERE id = 1;
```

#### 删除图片标签关联
```sql
-- 移除图片ID=1的标签ID=1
DELETE FROM gallery_image_tags
WHERE image_id = 1 AND tag_id = 1;

-- 移除图片ID=1的所有标签
DELETE FROM gallery_image_tags
WHERE image_id = 1;
```

---

## 五、常用业务查询

### 5.1 获取无限滚动数据
```sql
-- 前台无限滚动，仅返回启用的图片，按最新更新时间排序
SELECT 
  gi.id, gi.title, gi.image_url, 
  gi.created_at, gi.updated_at,
  GROUP_CONCAT(gt.tag_name SEPARATOR ',') as tags
FROM gallery_images gi
LEFT JOIN gallery_image_tags git ON gi.id = git.image_id
LEFT JOIN gallery_tags gt ON git.tag_id = gt.id AND gt.is_deleted = 0
WHERE gi.is_deleted = 0 AND gi.status = 0
GROUP BY gi.id
ORDER BY gi.updated_at DESC, gi.sort_order ASC
LIMIT ?, 20;  -- 第一个?为分页偏移量 如0,20,40...
```

### 5.2 点击点亮逻辑需要的数据
```sql
-- 查询图片1的所有标签
SELECT DISTINCT 
  gt.id, gt.tag_name, gt.color_code
FROM gallery_image_tags git
INNER JOIN gallery_tags gt ON git.tag_id = gt.id
WHERE git.image_id = 1 AND gt.is_deleted = 0;

-- 结合上面结果，查询具有相同标签的其他启用图片和连线信息
SELECT 
  gi2.id, gi2.title, gi2.image_url,
  gt.tag_name, gt.color_code
FROM (
  SELECT DISTINCT tag_id FROM gallery_image_tags WHERE image_id = 1
) tags
INNER JOIN gallery_image_tags git2 ON tags.tag_id = git2.tag_id
INNER JOIN gallery_images gi2 ON git2.image_id = gi2.id 
  AND gi2.id != 1 AND gi2.is_deleted = 0 AND gi2.status = 0
INNER JOIN Gallery_Tags gt ON tags.tag_id = gt.id
ORDER BY gt.id;
```

### 5.3 标签过滤（隐藏其他图片）
```sql
-- 查询某标签下的所有启用图片
SELECT 
  gi.id, gi.title, gi.image_url
FROM gallery_tags gt
INNER JOIN gallery_image_tags git ON gt.id = git.tag_id
INNER JOIN gallery_images gi ON git.image_id = gi.id
WHERE gt.tag_name = '风景' 
  AND gi.is_deleted = 0 
  AND gi.status = 0
  AND gt.is_deleted = 0
ORDER BY gi.sort_order ASC;
```

### 5.4 获取热门标签
```sql
-- 统计每个标签下的启用图片数量，排序
SELECT 
  gt.id, gt.tag_name, gt.tag_description, 
  COUNT(gi.id) as image_count
FROM gallery_tags gt
LEFT JOIN gallery_image_tags git ON gt.id = git.tag_id
LEFT JOIN gallery_images gi ON git.image_id = gi.id AND gi.is_deleted = 0 AND gi.status = 0
WHERE gt.is_deleted = 0
GROUP BY gt.id
ORDER BY image_count DESC, gt.sort_order ASC
LIMIT 10;
```

### 5.5 获取最近更新的图片
```sql
SELECT 
  id, title, image_url, updated_at
FROM gallery_images
WHERE is_deleted = 0 AND status = 0
ORDER BY updated_at DESC
LIMIT 5;
```

---

## 六、性能优化建议

### 6.1 索引策略
```sql
-- 已创建的基础索引
-- 追加性能优化索引

-- 组合索引：常用的分页查询
CREATE INDEX `idx_deleted_updated` ON gallery_images(`is_deleted`, `updated_at` DESC);

-- 组合索引：标签过滤查询
CREATE INDEX `idx_tag_deleted_sort` ON gallery_tags(`is_deleted`, `sort_order`);

-- 关联表查询优化
CREATE INDEX `idx_git_image_tag` ON gallery_image_tags(`image_id`, `tag_id`);
CREATE INDEX `idx_git_tag_image` ON gallery_image_tags(`tag_id`, `image_id`);
```

### 6.2 查询优化建议
- ✅ 使用分页查询，不要一次加载所有数据
- ✅ 善用LEFT JOIN而非子查询，提升连接效率
- ✅ 为软删除字段建立索引，加快活跃数据查询
- ✅ 使用GROUP_CONCAT合并结果，减少应用层处理
- ✅ 定期分析慢查询日志，持续优化

### 6.3 缓存策略
```java
// Spring Cache 伪代码示例
@Cacheable(value = "tags", unless = "#result == null")
public List<GalleryTag> getAllTags() {
    return galleryTagRepository.findAll();
}

@CacheEvict(value = "tags", allEntries = true)
public void invalidateTagsCache() {
    // 标签变化时清除缓存
}
```

---

## 七、常见问题处理

### 问题1：如何安全地删除标签？

**错误做法**：直接DELETE，会违反外键约束
```sql
-- ❌ 错误
DELETE FROM gallery_tags WHERE id = 1;
-- 报错：Cannot delete or update a parent row: a foreign key constraint fails
```

**正确做法**：使用软删除
```sql
-- ✓ 正确
UPDATE gallery_tags SET is_deleted = 1 WHERE id = 1;

-- 查询时自动过滤
SELECT * FROM gallery_tags WHERE is_deleted = 0;
```

### 问题2：如何查询某张图片所有的标签？

```sql
SELECT 
  gt.id, gt.tag_name, gt.color_code, gt.tag_description
FROM gallery_image_tags git
INNER JOIN gallery_tags gt ON git.tag_id = gt.id
WHERE git.image_id = ? AND gt.is_deleted = 0;
```

### 问题3：如何检查是否有重复图片URL？

```sql
SELECT image_url, COUNT(*) as count
FROM gallery_images
WHERE is_deleted = 0
GROUP BY image_url
HAVING count > 1;
```

### 问题4：如何迁移AList域名？

```sql
-- 步骤1：查看所有旧域名的图片
SELECT DISTINCT alist_domain FROM gallery_images WHERE is_deleted = 0;

-- 步骤2：更新域名（后端配置方式更推荐）
UPDATE gallery_images
SET alist_domain = 'gallery.example.com'
WHERE alist_domain = 'oss.tilex.world';

-- 步骤3：验证
SELECT COUNT(*) FROM gallery_images 
WHERE alist_domain = 'gallery.example.com' AND is_deleted = 0;
```

### 问题5：如何处理软删除后的并发修改？

```sql
-- 更新时保险的做法：检查is_deleted状态
UPDATE gallery_images
SET title = '新标题'
WHERE id = 1 AND is_deleted = 0;

-- 检查是否真的更新成功
SELECT ROW_COUNT();  -- MySQL特有，返回受影响的行数
```

### 问题6：如何禁用/启用图片？

**概述**：`status` 字段用于后台管理员控制前台是否显示某张图片，禁用的图片后端不会返回给前台。

**禁用单张图片**
```sql
UPDATE gallery_images
SET status = 1
WHERE id = 1;
```

**启用单张图片**
```sql
UPDATE gallery_images
SET status = 0
WHERE id = 1;
```

**批量禁用某标签下的所有图片**
```sql
UPDATE gallery_images
SET status = 1
WHERE id IN (
  SELECT DISTINCT gi.id 
  FROM gallery_images gi
  INNER JOIN gallery_image_tags git ON gi.id = git.image_id
  WHERE git.tag_id = 5  -- 标签ID
);
```

**查询已禁用的图片**
```sql
SELECT id, title, image_url, updated_at
FROM gallery_images
WHERE status = 1 AND is_deleted = 0
ORDER BY updated_at DESC;
```

---

## 八、数据备份与恢复

### 8.1 全量备份
```bash
# 使用mysqldump备份整个数据库
mysqldump -u root -p gallery_db > gallery_db_backup_2026-03-23.sql

# 指定备份主从或二进制日志信息（用于恢复到某个时间点）
mysqldump -u root -p --master-data=2 gallery_db > gallery_db_backup.sql
```

### 8.2 恢复备份
```bash
# 恢复整个数据库
mysql -u root -p gallery_db < gallery_db_backup_2026-03-23.sql

# 如果数据库不存在，先创建
mysql -u root -p < gallery_db_backup_2026-03-23.sql
```

### 8.3 表级别备份
```bash
# 只备份特定表
mysqldump -u root -p gallery_db Gallery_Images Gallery_Tags > tables_backup.sql

# 恢复特定表
mysql -u root -p gallery_db < tables_backup.sql
```

### 8.4 数据导出为CSV
```sql
SELECT 
  id, title, description, image_url, 
  created_at, updated_at
FROM gallery_images
WHERE is_deleted = 0
INTO OUTFILE '/tmp/gallery_images.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
```

---

## 九、监控及维护

### 9.1 定期检查数据完整性
```sql
-- 检查孤立的标签关联记录
SELECT git.* FROM gallery_image_tags git
LEFT JOIN gallery_images gi ON git.image_id = gi.id
WHERE gi.id IS NULL;

-- 检查是否存在重复的图片URL
SELECT image_url, COUNT(*) 
FROM gallery_images
WHERE is_deleted = 0
GROUP BY image_url
HAVING COUNT(*) > 1;

-- 检查是否存在空标签关联
SELECT COUNT(*) 
FROM gallery_image_tags 
WHERE image_id IS NULL OR tag_id IS NULL;
```

### 9.2 定期清理软删除数据
```sql
-- 查看软删除数据量
SELECT COUNT(*) as deleted_images FROM gallery_images WHERE is_deleted = 1;
SELECT COUNT(*) as deleted_tags FROM gallery_tags WHERE is_deleted = 1;

-- 备份后永久删除（谨慎使用）
DELETE FROM gallery_image_tags 
WHERE image_id IN (SELECT id FROM gallery_images WHERE is_deleted = 1);

DELETE FROM gallery_images WHERE is_deleted = 1 AND updated_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### 9.3 表统计信息更新
```sql
-- 重建表统计信息，优化查询性能
ANALYZE TABLE gallery_images;
ANALYZE TABLE gallery_tags;
ANALYZE TABLE gallery_image_tags;

-- 检查表
CHECK TABLE gallery_images;
CHECK TABLE gallery_tags;
CHECK TABLE gallery_image_tags;
```

---

## 十、Spring Boot JPA 示例代码

### 10.1 Entity定义
```java
@Entity
@Table(name = "gallery_images")
public class GalleryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 512, nullable = false, unique = true)
    private String imageUrl;
    
    @Column(length = 512)
    private String alistPath;
    
    @Column(length = 255)
    private String alistDomain;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0", comment = "图片状态：0=启用,1=禁用")
    private Integer status = 0;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "gallery_image_tags",
        joinColumns = @JoinColumn(name = "image_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<GalleryTag> tags = new ArrayList<>();
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted = 0;
    
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 10.2 Repository
```java
@Repository
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {
    
    // 分页查询未删除且启用的图片
    Page<GalleryImage> findByIsDeletedAndStatusOrderByUpdatedAtDesc(
        Integer isDeleted,
        Integer status,
        Pageable pageable
    );
    
    // 按标签查询启用的图片
    @Query(value = 
        "SELECT gi.* FROM gallery_images gi " +
        "INNER JOIN gallery_image_tags git ON gi.id = git.image_id " +
        "WHERE git.tag_id = :tagId AND gi.is_deleted = 0 AND gi.status = 0",
        nativeQuery = true
    )
    List<GalleryImage> findByTagId(@Param("tagId") Long tagId);
    
    // 查询关联图片（仅返回启用的图片）
    @Query(value = 
        "SELECT gi2.* FROM gallery_images gi1 " +
        "INNER JOIN gallery_image_tags git1 ON gi1.id = git1.image_id " +
        "INNER JOIN gallery_image_tags git2 ON git1.tag_id = git2.tag_id " +
        "INNER JOIN gallery_images gi2 ON git2.image_id = gi2.id " +
        "WHERE gi1.id = :imageId AND gi2.id != :imageId AND gi2.is_deleted = 0 AND gi2.status = 0",
        nativeQuery = true
    )
    List<GalleryImage> findRelatedImages(@Param("imageId") Long imageId);
    
    // 查询所有启用的图片
    List<GalleryImage> findByIsDeletedAndStatus(Integer isDeleted, Integer status);
    
    // 批量修改图片状态
    @Modifying
    @Query("UPDATE GalleryImage SET status = :status WHERE is_deleted = :isDeleted")
    int updateStatusByIsDeleted(@Param("status") Integer status, @Param("isDeleted") Integer isDeleted);
}
```

---

## 十一、总结检查清单

- [ ] 数据库已创建，字符集设置为 utf8mb4
- [ ] 三张表已正确创建，外键约束完整
- [ ] 所有必要的索引已建立（包括 idx_status 索引）
- [ ] 软删除机制已理解并应用
- [ ] 状态控制机制已理解（status 字段用于禁用/启用图片）
- [ ] 后端查询已添加 status = 0 条件（仅返回启用的图片）
- [ ] 后台管理接口已支持禁用/启用图片的操作
- [ ] 备份策略已制定
- [ ] 缓存策略已规划
- [ ] 应用层代码已处理 is_deleted=0 和 status=0 的过滤
- [ ] 定期维护任务已排期

---

**文档版本**：v2.0（已添加 status 字段支持）  
**最后更新**：2026年3月24日  
**维护部门**：数据库管理团队
