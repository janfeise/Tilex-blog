import { getArticles } from "../utils/api.js";
import { findAllElements } from "./../utils/findDom.js";

const BASE_URL = "/pages/article.html";

/**
 * 渲染博客文章列表
 * @param {HTMLElement} container - 博客文章容器
 */
function renderBlogPosts(container) {
  // TODO: 正式使用时取消注释,启用真实API
  // const posts = getArticles({ page: 0, size: 15 });

  // 临时测试数据
  const posts = [
    {
      id: 1,
      title: "文章标题 1",
      date: "2025-10-05",
      content: "这是第一篇文章的内容。",
      url: "/pages/article.html?1",
    },
    {
      id: 2,
      title: "文章标题 2",
      date: "2025-10-06",
      content: "这是第二篇文章的内容。",
      url: "/article/2",
    },
    {
      id: 3,
      title: "文章标题 3",
      date: "2025-10-07",
      content: "这是第三篇文章的内容,展示更多文章样式。",
      url: "/article/3",
    },
    {
      id: 1,
      title: "文章标题 1",
      date: "2025-10-05",
      content: "这是第一篇文章的内容。",
      url: "/article/1",
    },
    {
      id: 2,
      title: "文章标题 2",
      date: "2025-10-06",
      content: "这是第二篇文章的内容。",
      url: "/article/2",
    },
    {
      id: 3,
      title: "文章标题 3",
      date: "2025-10-07",
      content: "这是第三篇文章的内容,展示更多文章样式。",
      url: "/article/3",
    },
    {
      id: 1,
      title: "文章标题 1",
      date: "2025-10-05",
      content: "这是第一篇文章的内容。",
      url: "/article/1",
    },
    {
      id: 2,
      title: "文章标题 2",
      date: "2025-10-06",
      content: "这是第二篇文章的内容。",
      url: "/article/2",
    },
    {
      id: 3,
      title: "文章标题 3",
      date: "2025-10-07",
      content: "这是第三篇文章的内容,展示更多文章样式。",
      url: "/article/3",
    },
    {
      id: 1,
      title: "文章标题 1",
      date: "2025-10-05",
      content: "这是第一篇文章的内容。",
      url: "/article/1",
    },
    {
      id: 2,
      title: "文章标题 2",
      date: "2025-10-06",
      content: "这是第二篇文章的内容。",
      url: "/article/2",
    },
    {
      id: 3,
      title: "文章标题 3",
      date: "2025-10-07",
      content: "这是第三篇文章的内容,展示更多文章样式。",
      url: "/article/3",
    },
  ];

  const template = document.getElementById("blog-template");

  if (!template) {
    console.error("未找到 #blog-template 模板元素");
    container.innerHTML = "<p>模板加载失败</p>";
    return;
  }

  // 清空容器,避免重复渲染
  container.innerHTML = "";

  // 如果没有文章数据
  if (!posts || posts.length === 0) {
    container.innerHTML = "<p class='blog__empty'>暂无文章</p>";
    return;
  }

  posts.forEach((post) => {
    // 克隆模板内容
    const clone = template.content.cloneNode(true);

    // 动态填充数据
    clone.querySelector(".blog__title").textContent = post.title;
    clone.querySelector(".blog__date").textContent = post.date;
    clone.querySelector(".blog__text").textContent = post.content;

    // 给按钮加上跳转事件
    const btnGoto = clone.querySelector(".blog__goto");
    if (btnGoto) {
      btnGoto.addEventListener("click", () => {
        window.location.href = `${BASE_URL}?id=${post.id}`;
      });
    }

    // 插入页面
    container.appendChild(clone);
  });
}

/**
 * 为博客文章元素添加 上滑 动画
 * @param {HTMLElement} container - 包含导航栏的容器元素
 */
function addSlideUpAnimation(container) {
  const articlesEl = findAllElements(container, {
    wrap: ".blog__wrap",
  });

  articlesEl.wrap.map((article) => {
    // 先清除动画，确保重复调用时动画能重新触发
    article.style.animation = "none";
    void article.offsetWidth; // 强制回流，重新计算布局

    // 添加动画
    article.style.animation = "slide-up 1s ease-in-out";
  });
}

/**
 * 初始化博客页（文章渲染 + slide-up 效果）
 * @param {HTMLElement} container - 包含导航栏的容器元素
 */
const initBlog = function (container) {
  renderBlogPosts(container);
  addSlideUpAnimation(container);
};

export { initBlog };
