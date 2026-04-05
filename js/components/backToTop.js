/**
 * @file BackToTop.js
 * @description
 * “回到顶部”组件逻辑模块。
 * 当页面顶部锚点（topAnchor）离开视口时显示该组件；
 * 当顶部锚点重新进入视口时隐藏该组件。
 *
 * 动画说明：
 * - 入场动画：上滑（slide-up）
 * - 出场动画：下滑（通过 hidden 类控制 display）
 *
 * @module BackToTop
 */

import { topAnchor } from "../dom/commonDom";
import { findRequiredElement } from "../utils/findDom";
import { observeDom } from "../utils/observe";

/**
 * 切换元素的 hidden 状态。
 * 当 operation 为 true 时，添加类名 'hidden'；
 * 当 operation 为 false 时，移除类名 'hidden'。
 *
 * @param {HTMLElement} element - 需要切换状态的元素
 * @param {boolean} [shouldHide=false] - 是否隐藏元素
 */
const toggleHiddenState = (element, shouldHide = false) => {
  if (shouldHide) {
    element.classList.add("hidden");
  } else {
    element.classList.remove("hidden");
  }
};

/**
 * 监听顶部锚点的可见性，控制“回到顶部”组件的显示与隐藏
 * 当顶部锚点离开视口时显示组件，并播放上滑入场动画；
 * 当顶部锚点重新进入视口时隐藏组件
 *
 * @param {HTMLElement} container - 组件所在的容器元素
 */
const observeBackToTopVisibility = (container) => {
  const backToTopButton = findRequiredElement(container, ".back-to-top");

  const handleVisibilityChange = (target, isInView) => {
    if (isInView) {
      // 添加出场动画
      // 先清除动画属性，再强制回流，然后重新设置动画
      backToTopButton.style.animation = "none";
      void backToTopButton.offsetWidth; // 触发强制回流（reflow）
      backToTopButton.style.animation = "slide-down-out 0.5s ease-in-out";
      // 顶部锚点在视口内 => 隐藏回到顶部按钮
      setTimeout(() => {
        toggleHiddenState(backToTopButton, true);
      }, 300);
    } else {
      // 顶部锚点离开视口 => 显示按钮并播放动画
      toggleHiddenState(backToTopButton, false);

      // 为了确保重复触发动画时能正常播放：
      // 先清除动画属性，再强制回流，然后重新设置动画
      backToTopButton.style.animation = "none";
      void backToTopButton.offsetWidth; // 触发强制回流（reflow）
      backToTopButton.style.animation = "slide-up 0.5s ease-in-out";
    }
  };

  observeDom(topAnchor, handleVisibilityChange, {
    root: null,
    threshold: 0,
    rootMargin: "0px",
  });
};

/**
 * 处理点击事件，实现平滑跳转
 *
 * @param {*} container
 */
const handleClick = function (container) {
  const el = findRequiredElement(container, ".back-to-top");
  el.addEventListener("click", (e) => {
    const link = e.target.closest(".back-to-top__link");
    if (link) {
      e.preventDefault(); // 阻止默认跳转
      const id = link.getAttribute("href"); // 获取跳转目标
      document.querySelector(id).scrollIntoView({ behavior: "smooth" }); // 平滑跳转
    }
  });
};

/**
 * 初始化“回到顶部”组件。
 *
 * @param {HTMLElement} container - 组件所在的容器元素
 */
const initBackToTop = (container) => {
  observeBackToTopVisibility(container);
  handleClick(container);
};

export { initBackToTop };
