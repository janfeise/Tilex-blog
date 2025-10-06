import { formatDate } from "../utils/date.js";
import { findRequiredElement, safeUpdate } from "../utils/findDom.js";

/**
 * 季节配置映射表
 */
const SEASON_CONFIG = {
  春: { className: "seasons__spring", label: "春天" },
  夏: { className: "seasons__summer", label: "夏天" },
  秋: { className: "seasons__autumn", label: "秋天" },
  冬: { className: "seasons__winter", label: "冬天" },
};

/**
 * 根据月份判断季节
 * @param {number|string} month - 月份（1 ~ 12 或 "01" ~ "12"）
 * @returns {string} 季节字符串（"春"、"夏"、"秋"、"冬"）
 */
function getSeasonByMonth(month) {
  const monthNum = typeof month === "string" ? parseInt(month, 10) : month;

  if (monthNum < 1 || monthNum > 12 || isNaN(monthNum)) {
    throw new Error(`无效的月份: ${month}，月份必须是 1 到 12 之间的数字`);
  }

  if (monthNum >= 3 && monthNum <= 5) return "春";
  if (monthNum >= 6 && monthNum <= 8) return "夏";
  if (monthNum >= 9 && monthNum <= 11) return "秋";
  return "冬";
}

/**
 * 获取当前季节
 * @returns {string} 当前季节
 */
function getCurrentSeason() {
  const month = formatDate(new Date(), "MM");
  return getSeasonByMonth(month);
}

/**
 * 更新季节样式类名
 * @param {HTMLElement} container - 容器元素
 */
const updateSeasonClassName = function (container) {
  // 0. 获取当前所在的季节和对应的映射表数据
  const currentSeason = getCurrentSeason();
  // const currentSeason = "春";
  const config = SEASON_CONFIG[currentSeason];

  if (!config) {
    console.error(`未知的季节: ${currentSeason}`);
    return;
  }

  // 1. 找到目标元素
  const el = findRequiredElement(container, ".seasons__container");

  // 2. 移除所有的季节类名
  Object.values(SEASON_CONFIG).forEach(({ className }) => {
    el.classList.remove(className);
  });

  // 3. 添加对应季节的类名
  el.classList.add(config.className);

  // 4.更新标题
  safeUpdate(container, ".seasons__title", (titleEl) => {
    titleEl.textContent = currentSeason;
  });

  // 5. 添加淡入动画
  safeUpdate(container, ".seasons__content", (contentEl) => {
    contentEl.style.animation = "fade-in 1.5s ease-in-out";
  });
};

/**
 * 更新季节日期显示
 * @param {HTMLElement} container - 容器元素
 */
function updateSeasonDate(container) {
  const el = findRequiredElement(container, ".seasons__date");

  el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  el.style.animation = "slide-up 1s ease-in-out forwards";
}

/**
 * 初始化季节组件
 * @param {HTMLElement} container - 容器元素
 */
function initSeasons(container) {
  if (!container || !(container instanceof HTMLElement)) {
    console.error("initSeasons: 无效的容器元素");
    return;
  }

  updateSeasonClassName(container);
  updateSeasonDate(container);
}

export { initSeasons, getCurrentSeason, getSeasonByMonth };
