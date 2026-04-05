# 画廊业务详细文档

## 一、业务概述

画廊业务是一个交互式图片展示系统，支持标签过滤、图片关联、无限滚动加载等功能。用户可以通过点击、双击操作与图片交互，实现图片详情查看、标签过滤等功能。

---

## 二、核心功能需求

### 2.1 无限滚动加载
- **描述**：用户向下滚动时，自动加载更多图片
- **实现逻辑**：监听滚动事件，当用户滚动到底部时触发加载
- **分页策略**：使用分页或游标分页方式加载
- **加载状态**：显示加载提示，避免重复请求

### 2.2 双击进入图片详情
- **触发条件**：用户在图片上双击（dblclick）
- **行为**：跳转到图片详情页面或打开图片详情弹窗
- **详情信息展示**：
  - 图片及其完整描述
  - 标签列表与标签描述
  - 创建时间和最近更新时间
  - 图片来源域名信息（download）

### 2.3 点击点亮图片（单标签）
- **触发条件**：用户点击（click）某张图片
- **行为**：
  - 该图片进入"已点亮"状态（高亮显示、边框发光等）
  - 其他未点亮的图片置于半透明或灰显状态
  - 显示该图片的标签及标签描述

### 2.4 关联图片高亮与连线显示（多标签关联）
- **触发条件**：两张图片有相同标签时，点击其中任意一张
- **行为**：
  - 两张图片都进入"已点亮"状态
  - 在两张图片之间绘制可视化连线（表示标签关联）
  - 其他无关图片置于半透明状态
  - 在连线上显示关联的标签名称
  - 若有多个共同标签，可在连线上显示多个标签名称或标签计数

### 2.5 不同标签图片点亮时的状态切换
- **触发条件**：用户点击具有不同标签的图片（与当前已点亮图片无共同标签）
- **行为**：
  - 取消之前所有图片的点亮状态
  - 清除之前的连线
  - 仅点亮新点击的图片
  - 更新为新图片的标签及标签描述显示

### 2.6 标签过滤与图片隐藏
- **触发条件**：某个标签的所有关联图片都被点亮
- **行为**：
  - 隐藏所有不属于该标签的图片
  - 仅显示该标签下的所有图片
  - 在页面显示该标签的描述和相关UI-指示（如"已过滤：标签名"）
  - 显示回到全部图片的操作按钮或交互提示
  - 提供清除过滤的功能（点击图片区域空白处、点击"×"按钮等）

---

## 三、数据结构设计

### 3.1 数据表结构

#### 表1：图片表（Gallery_Images）
用于存储所有画廊图片的基本信息。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|--------|------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 图片唯一标识 |
| `title` | VARCHAR(255) | NOT NULL | 图片标题 |
| `description` | TEXT | - | 图片描述（详细说明该图片的内容） |
| `image_url` | VARCHAR(512) | NOT NULL, UNIQUE | 图片URL（来自AList存储或其他CDN） |
| `domain` | VARCHAR(255) | - | 图片所属域名（为后续域名迁移准备，允许存储源域名或多个域名） |
| `source_type` | VARCHAR(50) | - | 图片来源类型（如：alist, local, cdn等） |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 最后更新时间 |
| `display_time` | TIMESTAMP | - | 前台展示时间（通常与updated_at相同，但允许自定义） |
| `is_deleted` | TINYINT | DEFAULT 0 | 软删除标识（0=正常, 1=已删除） |
| `sort_order` | INT | - | 排序字段（用于自定义排序） |

#### 表2：标签表（Gallery_Tags）
用于存储业务中的所有标签及其描述。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|--------|------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 标签唯一标识 |
| `tag_name` | VARCHAR(100) | NOT NULL, UNIQUE | 标签名称（如：风景、人物、建筑等） |
| `tag_description` | TEXT | - | 标签描述（对该标签类别的详细说明） |
| `color_code` | VARCHAR(7) | DEFAULT '#999999' | 标签颜色代码（用于UI展示，如连线颜色） |
| `sort_order` | INT | - | 标签排序 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| `is_deleted` | TINYINT | DEFAULT 0 | 软删除标识 |

**标签删除影响分析**：
- 若直接删除标签，会导致关联表中的数据孤立
- **解决方案**：采用软删除方式（is_deleted=1），查询时过滤已删除标签
- 历史数据保留，不会丢失图片与标签的关联信息

#### 表3：图片标签关联表（Gallery_Image_Tags）
用于存储图片与标签的多对多关系。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|--------|------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 关联记录唯一标识 |
| `image_id` | BIGINT | FOREIGN KEY, NOT NULL | 图片ID（外键关联Gallery_Images.id） |
| `tag_id` | BIGINT | FOREIGN KEY, NOT NULL | 标签ID（外键关联Gallery_Tags.id） |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**联合唯一约束**：
```sql
UNIQUE KEY `uk_image_tag` (`image_id`, `tag_id`)
```
- 同一张图片不会被添加重复标签

**外键约束**：
```sql
FOREIGN KEY (`image_id`) REFERENCES `Gallery_Images`(`id`) ON DELETE CASCADE
FOREIGN KEY (`tag_id`) REFERENCES `Gallery_Tags`(`id`) ON DELETE RESTRICT
```
- 图片删除时，关联关系自动删除
- 标签删除受限制，防止数据孤立（配合软删除使用）

---

## 四、交互流程详述

### 4.1 初始加载流程
```
用户打开画廊页面
  ↓
加载首批图片（假设20张）
  ↓
渲染图片列表（未点亮状态）
  ↓
等待用户交互
```

### 4.2 无限滚动流程
```
用户滚动到底部（距底部<100px）
  ↓
检查是否已加载全部图片
  ↓
若未加载全部，发起加载请求（分页号+1）
  ↓
后台返回新图片
  ↓
追加到DOM，继续展示
  ↓
若已加载全部，显示"已加载全部"提示
```

### 4.3 单击点亮流程
```
用户单击图片A
  ↓
获取图片A的所有标签（tags_A）
  ↓
查询具有共同标签的其他图片
  ↓
情况1：tags_A 与 其他图片无共同标签
  ├─ 取消之前图片的点亮
  ├─ 清除之前的连线
  ├─ 点亮图片A
  └─ 展示图片A的标签描述

情况2：tags_A 与 其他图片有共同标签
  ├─ 找出所有具有共同标签的图片
  ├─ 点亮图片A及所有相关图片
  ├─ 绘制关联连线（不同共同标签用不同颜色）
  ├─ 在连线上标注共同标签
  └─ 展示相关标签的描述
```

### 4.4 清除点亮流程
```
用户点击空白区域 或 点击"清除"按钮 或 满足过滤隐藏条件后手动操作
  ↓
取消所有图片的点亮状态
  ↓
清除所有连线
  ↓
恢复所有图片的完全显示
  ↓
清空标签描述显示区
```

### 4.5 标签过滤与隐藏流程
```
用户多次单击，将某标签的所有图片都点亮
  ↓
系统检测到该标签下所有图片已点亮
  ↓
隐藏所有其他标签的图片
  ↓
仅显示该标签下的图片
  ↓
页面顶部显示过滤状态："正在查看：[标签名] 分类"
  ↓
显示该标签的描述
  ↓
提供"清除过滤"按钮
```

### 4.6 双击打开详情流程
```
用户双击某张图片
  ↓
获取该图片的完整信息：
  ├─ 图片URL
  ├─ 标题和描述
  ├─ 所有标签及标签描述
  ├─ 创建时间和更新时间
  └─ 源域名
  ↓
打开详情页面/弹窗
  ↓
展示上述所有信息
  ↓
提供关闭按钮或返回功能
```

---

## 五、UI/UX设计考虑

### 5.1 图片状态分类
1. **未点亮状态**：
   - 不透明度：100%
   - 边框：无或浅色
   - 光标：pointer
   
2. **点亮状态**：
   - 不透明度：100%
   - 边框：彩色发光边框（主题色）
   - 阴影：外发光效果
   - 缩放：可选微微放大
   
3. **未关联状态**（点亮其他标签时）：
   - 不透明度：40-50%
   - 边框：无
   - 光标：default或不可点击提示
   
4. **关联点亮状态**：
   - 不透明度：100%
   - 边框：彩色发光边框（与共同标签对应颜色）
   - 光标：pointer
   - 优先级：默认主色，多标签关联时分别显示

### 5.2 连线设计
- **颜色**：根据标签的color_code动态设置，多标签关联时可使用渐变色或多条线
- **粗度**：2-3px
- **样式**：可以是直线、贝塞尔曲线或其他装饰性曲线
- **标签标注**：在连线中部显示共同标签名称，背景色与标签颜色相同
- **动画**：可选闪烁或渐现效果

### 5.3 标签描述显示区
- **位置**：页面底部、侧边栏或弹窗形式
- **内容**：
  - 标签名称
  - 标签描述
  - 当前点亮的图片所属的标签数量
  - 该标签下总共的图片数量
- **动画**：平滑过渡，避免闪烁

### 5.4 过滤状态指示
- **位置**：页面顶部导航栏下方
- **内容**：
  - "正在查看：[标签名] - X张图片" 
  - "清除过滤"按钮
  - 标签描述
- **样式**：鲜明且易于关闭

### 5.5 加载状态指示
- **无限滚动加载提示**：在底部显示加载动画或"加载中..."
- **全部加载完成提示**："已加载全部图片"
- **加载失败提示**：显示重试按钮

---

## 六、前后端API设计

### 6.1 获取图片列表API
```
请求：GET /api/gallery/images
参数：
  - page (可选，默认1)：页码
  - limit (可选，默认20)：每页数量
  - tag_ids (可选)：按标签ID过滤

响应：
{
  "code": 0,
  "data": {
    "total": 1000,
    "current_page": 1,
    "per_page": 20,
    "images": [
      {
        "id": 1,
        "title": "图片标题",
        "description": "图片描述",
        "image_url": "https://...",
        "domain": "example.com",
        "display_time": "2025-03-20T10:30:00Z",
        "tags": [
          {
            "id": 1,
            "tag_name": "风景",
            "tag_description": "自然风景类图片",
            "color_code": "#FF6B6B"
          }
        ]
      }
    ]
  }
}
```

### 6.2 获取图片详情API
```
请求：GET /api/gallery/images/:id
响应：
{
  "code": 0,
  "data": {
    "id": 1,
    "title": "详细标题",
    "description": "详细描述...",
    "image_url": "https://...",
    "domain": "example.com",
    "source_type": "alist",
    "created_at": "2025-01-15T08:00:00Z",
    "updated_at": "2025-03-20T10:30:00Z",
    "display_time": "2025-03-20T10:30:00Z",
    "tags": [
      {
        "id": 1,
        "tag_name": "风景",
        "tag_description": "自然风景类图片，包括山水、日出日落等",
        "color_code": "#FF6B6B"
      },
      {
        "id": 3,
        "tag_name": "建筑",
        "tag_description": "建筑和人工结构",
        "color_code": "#4ECDC4"
      }
    ]
  }
}
```

### 6.3 获取标签列表API
```
请求：GET /api/gallery/tags
响应：
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "tag_name": "风景",
      "tag_description": "自然风景及美景",
      "color_code": "#FF6B6B",
      "image_count": 45
    }
  ]
}
```

### 6.4 查询共同标签的关联图片API
```
请求：GET /api/gallery/related-images
参数：
  - image_id：当前图片ID
  
响应：
{
  "code": 0,
  "data": {
    "current_image": {
      "id": 1,
      "tags": [1, 3, 5]
    },
    "related_images": [
      {
        "id": 5,
        "title": "相关图片",
        "tags": [3],
        "common_tags": [3],
        "tag_info": [
          {
            "id": 3,
            "tag_name": "建筑",
            "color_code": "#4ECDC4"
          }
        ]
      }
    ]
  }
}
```

---

## 七、前台实现技术指南

### 7.1 点击点亮实现逻辑
```javascript
// 伪代码
onImageClick(imageId) {
  const currentImage = getImage(imageId);
  const currentTags = currentImage.tags; // 获取标签数组
  
  // 查询与当前图片有共同标签的图片
  const relatedImages = queryRelatedImages(imageId);
  
  // 清除之前的点亮
  clearPreviousHighlight();
  
  // 点亮当前图片和相关图片
  highlightImages([currentImage, ...relatedImages]);
  
  // 如果有关联图片，绘制连线
  if (relatedImages.length > 0) {
    drawConnectingLines(currentImage, relatedImages);
  }
  
  // 显示标签描述
  showTagDescriptions(currentTags);
}
```

### 7.2 连线绘制实现
- **使用Canvas或SVG**：推荐SVG便于交互和样式设置
- **坐标计算**：获取图片中心坐标，计算两点间的最优路径
- **贝塞尔曲线**：使用二阶或三阶贝塞尔曲线平滑连接
- **动态更新**：窗口resize时重新计算坐标和重绘

### 7.3 过滤隐藏实现
```javascript
// 伪代码
checkIfAllImagesOfTagHighlighted(tagId) {
  const imagesWithTag = getImagesByTag(tagId);
  const highlightedImages = getHighlightedImages();
  
  return imagesWithTag.every(img => highlightedImages.includes(img.id));
}

if (checkIfAllImagesOfTagHighlighted(tagId)) {
  // 隐藏其他图片
  hideImagesNotInTag(tagId);
  showFilterStatus(tagId);
}
```

### 7.4 无限滚动实现
```javascript
// 伪代码
onScroll() {
  if (isNearBottom() && !isLoading && !isAllLoaded) {
    isLoading = true;
    currentPage++;
    
    fetchImages(currentPage).then(newImages => {
      appending(newImages);
      isLoading = false;
    });
  }
}
```

---

## 八、性能优化建议

### 8.1 前台优化
- **图片懒加载**：使用IntersectionObserver进行图片预加载
- **连线缓存**：缓存已绘制的连线SVG，避免重复计算
- **防抖处理**：debounce滚动事件，防止频繁触发加载
- **虚拟滚动**：对于超大数据量，考虑使用虚拟滚动库

### 8.2 后台优化
- **数据库索引**：在`image_id`、`tag_id`、`is_deleted`字段建立索引
- **查询优化**：使用JOIN查询替代多次查询
- **缓存策略**：使用Redis缓存热门标签和图片列表
- **分页优化**：使用游标分页提升大数据量查询性能

### 8.3 存储优化
- **CDN加速**：图片使用CDN加速，支持多域名回源
- **图片压缩**：不同尺寸、不同清晰度的图片版本
- **AList配置**：确保AList返回的URL稳定可靠

---

## 九、常见问题与解决方案

### 问题1：删除标签如何处理历史关联？
**解决方案**：
- 采用软删除方式（is_deleted=1）
- 前台查询时自动过滤已删除标签
- 历史数据保留，便于数据审计和恢复

### 问题2：多个图片有多个共同标签时如何显示？
**解决方案**：
- 在连线上用不同颜色标注不同标签
- 或在连线旁显示所有共同标签的列表
- 在标签描述区显示所有相关标签的详细信息

### 问题3：大数据量下连线绘制性能问题？
**解决方案**：
- 使用Canvas而非SVG（Canvas性能更好，但交互性差）
- 或使用WebGL加速
- 限制同时显示的连线数量
- 考虑异步渲染和增量更新

### 问题4：域名迁移时如何保证URL有效？
**解决方案**：
- 在`domain`字段记录源域名和目标域名
- 提供域名映射服务
- 支持URL重定向或反向代理
- 定期检测URL有效性，失效时更新

### 问题5：如何防止标签被误删，造成数据孤立？
**解决方案**：
- 后台约束：设置`ON DELETE RESTRICT`，禁止删除关联的标签
- 前台提示：删除标签时提示"该标签已被X张图片使用，确认删除？"
- 使用软删除，逻辑删除而非物理删除
- 提供删除恢复功能

---

## 十、数据库初始化SQL

```sql
-- 创建图片表
CREATE TABLE `Gallery_Images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `image_url` VARCHAR(512) NOT NULL UNIQUE,
  `domain` VARCHAR(255),
  `source_type` VARCHAR(50),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `display_time` TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  `sort_order` INT,
  INDEX `idx_is_deleted` (`is_deleted`),
  INDEX `idx_created_at` (`created_at`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建标签表
CREATE TABLE `Gallery_Tags` (
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

-- 创建图片标签关联表
CREATE TABLE `Gallery_Image_Tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `image_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_image_tag` (`image_id`, `tag_id`),
  FOREIGN KEY (`image_id`) REFERENCES `Gallery_Images`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `Gallery_Tags`(`id`) ON DELETE RESTRICT,
  INDEX `idx_tag_id` (`tag_id`)
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 十一、总结与后续建议

### 11.1 MVP（最小可行产品）优先级
1. **第一阶段**：基础展示 + 无限滚动
2. **第二阶段**：标签关联 + 点亮逻辑
3. **第三阶段**：连线可视化 + 过滤隐藏
4. **第四阶段**：详情页面 + 性能优化

### 11.2 后续扩展功能
- 内容推荐：根据标签为用户推荐相似图片
- 用户收藏：用户收藏喜欢的图片
- 评论交互：图片评论与讨论
- 搜索功能：按标签、描述、时间进行搜索
- 分享功能：分享图片和标签组合
- 统计分析：热门标签、热门图片统计

### 11.3 技术栈建议
- **前台**：Vue 3 / React + Canvas/SVG库 + Intersection Observer
- **后台**：Spring Boot + JPA + MySQL
- **存储**：AList + CDN + 对象存储（如MinIO/AWS S3）
- **缓存**：Redis缓存热数据
- **监控**：ELK日志系统 + Prometheus指标监控

---

**文档版本**：v1.0  
**最后更新**：2026年3月23日  
**维护者**：画廊业务团队
