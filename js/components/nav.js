import { findRequiredElement, safeUpdate } from "../utils/findDom.js";

/**
 * 点击logo时跳转首页
 */
function clickLogo(container) {
  safeUpdate(container, ".nav__logo", clickLogoCallBack);
}

/**
 * 点击logo触发的回调函数
 */
function clickLogoCallBack(el) {
  el.addEventListener("click", (e) => {
    e.preventDefault(); // 如果你不想让链接的默认跳转触发，可以保留这行
    window.location.href = "/"; // 跳转到网站主页
  });
}

/**
 * 为导航栏元素添加 下滑 动画
 * @param {HTMLElement} container - 包含导航栏的容器元素
 */
function addAnimation(container) {
  const navEl = findRequiredElement(container, ".nav");

  // 先清除动画，确保重复调用时动画能重新触发
  navEl.style.animation = "none";
  void navEl.offsetWidth; // 强制回流，重新计算布局

  // 添加动画
  navEl.style.animation = "slide-down 1s ease-in-out";
}

/**
 * 初始化导航栏 hover 效果：
 * 鼠标悬停在某个导航项时，其他导航项会变暗
 * @param {HTMLElement} container - 包含导航栏的容器元素
 */
function initHoverEffect(container) {
  safeUpdate(container, ".nav__links", attachHoverHandlers);
}

/**
 * 事件委托
 * 为导航链接父容器绑定 hover 事件
 * @param {HTMLElement} parent - 导航链接的父元素
 */
function attachHoverHandlers(parent) {
  parent.addEventListener("mouseover", handleHover.bind(0.5)); // 悬停时其他项透明度降低
  parent.addEventListener("mouseout", handleHover.bind(1)); // 移出时恢复透明度
}

/**
 * 事件委托
 * hover 事件处理函数
 * 使用 this 来传递目标透明度（0.5 或 1），因为bind的第一个参数为this
 * @param {MouseEvent} e
 */
function handleHover(e) {
  const target = e.target;

  // guard line
  if (!target.classList.contains("nav__link")) return;

  const links = target.closest(".nav").querySelectorAll(".nav__link");
  links.forEach((link) => {
    if (link !== target) link.style.opacity = this;
  });
}

/**
 * 初始化导航栏（动画 + hover 效果）
 * @param {HTMLElement} container - 包含导航栏的容器元素
 */
function initNav(container) {
  initHoverEffect(container);
  addAnimation(container);
  clickLogo(container);
}

export { initNav };
