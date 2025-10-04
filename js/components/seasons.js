import { formatDate } from "../utils/date.js";

/**
 * 格式化seasons的日期
 */
function initSeasonsDate(container) {
  const el = container.querySelector(".seasons__date");
  if (el) {
    el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  } else {
    console.warn(".seasons__date 元素未找到");
  }
}

export { initSeasonsDate };
