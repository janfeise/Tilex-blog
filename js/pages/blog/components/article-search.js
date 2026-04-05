/**
 * @fileoverview 博客页面的文章搜索组件的js逻辑实现
 */
import { debounce } from "../../../utils/debounce.js";
import { safeUpdate } from "../../../utils/findDom.js";
import { searchArticles } from "../../../utils/api.js";
import { BASE_URL } from "../blog.js";

/**
 * 为该组件添加入场动画：左侧滑入
 */
const addSlideUpAnimation = function (container) {
  safeUpdate(container, "#article-search__input", (el) => {
    // 先清除动画，确保重复调用时动画能重新触发
    el.style.animation = "none";
    void el.offsetWidth; // 强制回流，重新计算布局

    // 添加动画
    el.style.animation = "slide-up 1s ease-in-out";
  });
};

/**
 * 文章搜索
 */
const articleSearch = async function (container) {
  // 搜索实现
  safeUpdate(container, "#article-search__input", (el) => {
    el.addEventListener(
      "input",
      debounce(async (e) => {
        const params = { keyword: e.target.value };
        const res = await searchArticles(params);

        displySearchResults(container, res.data);
      }, 300)
    );
  });
};

/**
 * 显示搜索结果
 * @param {HTMLElement} displayDom - 搜索结果要插入的父级 DOM 节点，即搜索结果显示在哪？
 * @param {Array} searchResults - 搜索结果数据数组，每个元素包含 { id, title, snippets }
 */
let isFirstSearch = true; // 标记是否为第一次搜索，用于控制入场动画
const displySearchResults = function (displayDom, searchResults) {
  // 使用 safeUpdate 确保操作的 DOM 安全存在
  safeUpdate(displayDom, ".article-search__results--content", (el) => {
    // 清空旧的搜索结果
    el.innerHTML = "";

    // 获取外层容器，移除 hidden 类名
    const resultsContainer = el.closest(".article-search__results--container");
    resultsContainer.classList.remove("hidden");

    // 第一次搜索时添加上滑动画
    if (isFirstSearch) {
      // 先清除动画，确保重复调用时动画能重新触发
      resultsContainer.style.animation = "none";
      void resultsContainer.offsetWidth; // 强制回流，重新计算布局

      // 添加动画
      resultsContainer.style.animation = "fade-in .5s ease-in-out";

      isFirstSearch = false;
    }

    // 处理无搜索结果的情况
    if (!searchResults || searchResults.length === 0) {
      const noResult = document.createElement("div");
      noResult.className = "article-search__no-results";
      noResult.innerHTML = `
      <p>😕 没有找到相关结果，请尝试其他关键词。</p>
    `;
      el.appendChild(noResult);
      return; // 直接退出函数，不执行后面的 forEach
    }

    // 遍历搜索结果，生成列表项
    searchResults.forEach((item) => {
      // 创建 li
      const li = document.createElement("li");
      li.setAttribute("data-articleId", item.id);

      // 创建标题 h3
      const h3 = document.createElement("h3");
      h3.className = "article-search__results--title";
      h3.innerHTML = item.title; // innerHTML 可包含 HTML 标签
      li.appendChild(h3);

      // 创建多个 snippets p
      item.snippets.forEach((snippet) => {
        const p = document.createElement("p");
        p.className = "article-search__results--snippets";
        p.innerHTML = snippet; // 可以保留 HTML 标签
        li.appendChild(p);
      });

      // 添加 click 事件监听：跳转对应的文章
      li.addEventListener("click", (e) => {
        const el = e.target.closest("li");

        // guard line
        if (!el) return;

        window.location.href = `${BASE_URL}?id=${el.dataset.articleid}`;
      });

      // 添加 li 到容器
      el.appendChild(li);
    });
  });
};

const initArticleSearch = function (container) {
  addSlideUpAnimation(container);
  articleSearch(container);
};

export { initArticleSearch };
