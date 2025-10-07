/**
 * 文章详情页模块
 * 负责处理文章详情页的数据获取、渲染和 SEO 元数据设置
 */

import { getArticleById } from "../utils/api.js";
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
async function initArticle(container) {
  // 获取文章数据
  const id = getQueryParam("id");
  let post;
  try {
    const res = await getArticleById(id);
    post = res.data;
  } catch (error) {
    console.error("获取文章失败：", error);
    container.innerHTML = "<p class='blog__empty'>文章加载失败</p>";
    return;
  }

  // 渲染文章到 articl.html 页面
  renderPost(container, post);

  // 添加上滑动画
  addSlideUpAnimation(container);
}

export { initArticle };
