/**
 * 文章详情页模块
 * 负责处理文章详情页的数据获取、渲染和 SEO 元数据设置
 */

import { safeUpdate } from "../utils/findDom.js";
import marked from "../utils/markdown.js";
import hljs from "highlight.js";

/**
 * 从 URL 查询参数中获取指定参数的值
 * @param {string} name - 查询参数的名称
 * @returns {string|null} 参数值，如果不存在则返回 null
 * @example
 * // URL: https://example.com/article.html?id=123
 * getQueryParam('id') // 返回 '123'
 */
function getQueryParam(name) {
  return new URL(location.href).searchParams.get(name);
}

/**
 * 设置页面的 SEO 元数据（标题和描述）
 * @param {string} title - 文章标题，用于设置页面标题和描述
 */
function setMeta(title) {
  // 设置网页的标题
  document.title = `${title} - TilexBlog`;

  // 查找或创建 meta description 标签
  let meta = document.querySelector('meta[name="description"]');
  if (!meta) {
    meta = document.createElement("meta");
    meta.name = "description";
    document.head.appendChild(meta);
  }

  // 设置页面描述为文章标题（可根据需求改为摘要）
  meta.content = title;
}

/**
 * 渲染文章内容到指定容器
 * @param {HTMLElement} container - 文章容器的 DOM 元素
 * @param {Object} post - 文章数据对象
 * @param {string} post.title - 文章标题
 * @param {string} [post.date] - 文章发布日期
 * @param {string} [post.content] - 文章正文内容
 */
function renderPost(container, post) {
  // 设置页面的 SEO 元数据
  setMeta(post.title);

  // 渲染文章正文内容
  safeUpdate(container, ".post__content", (el) => {
    // 将 Markdown 转换为 HTML
    const html = marked.parse(post.content);

    // 渲染到容器
    el.innerHTML = html;

    // 对所有 code 块应用 hljs
    el.querySelectorAll("pre code").forEach((block) => {
      hljs.highlightElement(block);
    });
  });
}

/**
 * 获取文章数据
 * 从 URL 参数中获取文章 ID，然后获取对应的文章数据
 * TODO: 这里应该从 API 或数据库获取真实数据，目前使用的是模拟数据
 * @returns {Object} 文章数据对象
 */
function getPost() {
  // 从 URL 获取文章 ID
  const id = getQueryParam("id");
  console.log("文章 ID:", id);

  // 模拟文章数据（实际项目中应该通过 API 获取）
  // TODO: 替换为实际的 API 调用，如 fetch(`/api/posts/${id}`)
  const post = {
    id: 2,
    title: "github项目查找指南",
    date: "2025-10-06",
    content: `# github项目查找指南

| 一级主题 | 二级主题      | 三级主题示例                                       |
| -------- | ------------- | -------------------------------------------------- |
| 搜索基础 | 查找对象 \`in\` | \`in:readme\`、\`in:description\` 等                   |
| 筛选条件 | star数筛选    | \`stars:>100\` 只看热门项目                          |
|          | 语言限定      | \`language:java\`、\`language:python\` 等              |
|          | 时间限定      | \`pushed:>2024-01-01\` 查找最近更新的项目            |
| 实用组合 | 毕业设计项目  | \`in:readme 毕业设计 stars:>100 language:java\`      |
|          | 持续维护项目  | \`stars:>50 pushed:>2024-01-01 language:javascript\` |
| 使用技巧 | 排序与过滤    | 配合“排序：star数/更新时间”快速筛选高质量仓库      |
|          | 补充搜索方式  | 关键词 + 条件组合（如 “management system” 等）     |

------

## 1. 基础搜索：\`in:\` 指定查找对象

GitHub 的搜索支持使用 \`in:\` 来限定搜索的范围，包括：

- \`in:readme\`：在 README 文档中搜索
- \`in:description\`：在<u>项目简介</u>中搜索
- \`in:name\`：在项目名称中搜索

**示例：查找毕业设计项目**

\`\`\`bash
in:readme 毕业设计
\`\`\`

✅ 说明：该命令会搜索所有 README 中包含“毕业设计”的项目。

------

## 2.  人气筛选：\`stars:>\`

项目的 Star 数是衡量项目质量和受欢迎程度的重要指标。
你可以通过 \`stars:>\` 来筛选出**受欢迎的项目**。

**示例：查找 Star 超过 100 的项目**

\`\`\`bash
stars:>100
\`\`\`

✅ 说明：只显示 Star 数量大于 100 的项目，快速定位优质开源项目。

你也可以组合使用：

\`\`\`bash
in:readme 毕业设计 stars:>100
\`\`\`

✅ 效果：在 README 中包含“毕业设计”且 Star 数量超过 100 的项目。

------

## 3. 按语言过滤：\`language:\`

如果你只想找特定语言的项目，可以通过 \`language:\` 来限定搜索范围。

**示例：查找 Java 项目**

\`\`\`bash
language:java
\`\`\`

**组合示例：查找 Java 毕业设计项目**

\`\`\`bash
in:readme 毕业设计 stars:>100 language:java
\`\`\`

✅ 效果：只会显示 README 中有“毕业设计”关键词、Star 超过 100、且使用 Java 编写的项目。

------

## 4. 按更新时间筛选：\`pushed:>\`

开源项目如果长时间无人维护，可能无法在新环境中运行。
使用 \`pushed:\` 可以筛选出**最近更新过的项目**。

**示例：查找 2024 年后仍在维护的项目**

\`\`\`bash
pushed:>2024-01-01
\`\`\`

**组合示例：找最新的 Java 毕业设计项目**

\`\`\`bash
in:readme 毕业设计 stars:>100 language:java pushed:>2024-01-01
\`\`\`

✅ 效果：精准找到近期维护、受欢迎、与毕业设计相关的 Java 项目。

------

## 5. 组合搜索实战案例

这里是一些常见需求的搜索语法示例：

| 搜索需求                   | 搜索语句                                                     |
| -------------------------- | ------------------------------------------------------------ |
| 查找热门 Java 毕业设计项目 | \`in:readme 毕业设计 stars:>100 language:java\`                |
| 查找热门 Python 爬虫项目   | \`in:description 爬虫 stars:>200 language:python\`             |
| 查找最近更新的前端管理系统 | \`management system stars:>50 language:javascript pushed:>2024-01-01\` |
| 查找近期活跃的机器学习项目 | \`machine learning stars:>500 pushed:>2024-06-01\`             |

------

## 6. 实用技巧与建议

- **关键词要精准**：使用中英文都可以尝试，如“管理系统”/“management system”。
- **结合排序功能**：GitHub 搜索结果右上角可以按 Star 数、更新时间排序。
- **多语言交叉搜索**：如 \`language:java OR language:python\`，查找多语言项目。
- **关注项目活跃度**：查看 Issues、PR、commit 记录，判断项目是否仍在维护。

------

##  总结

掌握这些搜索技巧后，你可以在几分钟内从海量开源项目中**精准找到适合自己的目标项目**：

- ✅ \`in:\` 精准定位搜索范围
- ⭐ \`stars:\` 快速筛选高质量项目
- 💻 \`language:\` 聚焦技术栈
- 🕒 \`pushed:\` 保证项目活跃度`,
    url: "/pages/article.html?id=2",
  };

  return post;
}

/**
 * 为博客文章元素添加 上滑 动画
 * @param {HTMLElement} container - 包含文章的容器元素
 */
function addSlideUpAnimation(container) {
  safeUpdate(container, ".post__content", (el) => {
    // 先清除动画，确保重复调用时动画能重新触发
    el.style.animation = "none";
    void el.offsetWidth; // 强制回流，重新计算布局

    // 添加动画
    el.style.animation = "slide-up 1s ease-in-out";
  });
}

/**
 * 初始化文章详情页
 * 作为模块的主入口函数，负责协调数据获取和页面渲染
 * @param {HTMLElement} container - 文章容器的 DOM 元素
 * @example
 * // 在页面加载时调用
 * const articleContainer = document.querySelector('.article-container');
 * initArticle(articleContainer);
 */
const initArticle = function (container) {
  // 获取文章数据
  const post = getPost();

  // 渲染文章到 articl.html 页面
  renderPost(container, post);

  // 添加上滑动画
  addSlideUpAnimation(container);
};

export { initArticle };
