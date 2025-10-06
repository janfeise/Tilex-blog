/**
 * @fileoverview 路由配置文件，定义 不同页面 需要加载的组件
 *
 * 实现思路：
 * 1. 为每个页面路径定义需要加载的组件配置
 * 2. 使用现有的 loadComponents 工具函数来加载组件
 * 3. 自动识别当前页面路径并加载对应组件
 */
import { articleContainer } from "./dom/articlDom.js";
import { blogWrapper } from "./dom/blogDom.js";
import { headerSeasonsContainer, headerNavContainer } from "./dom/headerDom.js";
import { initSeasons } from "./components/seasons.js";
import { initNav } from "./components/nav.js";
import { initBlog } from "./pages/blog.js";
import { loadComponents } from "./utils/loadComponent.js";
import { initArticle } from "./pages/article.js";

/**
 * 公共组件配置（所有页面都需要的组件）
 */
const commonComponents = [
  {
    container: headerNavContainer,
    name: "nav",
    initFuc: initNav,
  },
];

/**
 * 页面特定组件配置
 */
const pageSpecificComponents = {
  "/": [
    {
      container: headerSeasonsContainer,
      name: "seasons",
      initFuc: initSeasons,
    },
  ],

  "/index.html": [],

  "/pages/blog.html": [
    {
      container: blogWrapper,
      name: "blog-article",
      initFuc: initBlog,
    },
  ],

  "/pages/article.html": [
    {
      container: articleContainer,
      name: "article-detail",
      initFuc: initArticle,
    },
  ],
};

/**
 * 合并公共组件和页面特定组件
 * @param {string} path - 页面路径
 * @returns {Array} 完整的组件配置数组
 */
function getComponentsForPath(path) {
  const specific = pageSpecificComponents[path] || [];
  return [...commonComponents, ...specific];
}

/**
 * 获取当前页面路径（标准化处理）
 */
function getCurrentPath() {
  let path = window.location.pathname;

  // 处理根路径
  if (path === "/" || path === "/index.html") {
    return "/";
  }

  return path;
}

/**
 * 根据当前路径加载对应的组件
 */
export function loadPageComponents() {
  const currentPath = getCurrentPath();
  let componentConfigs = getComponentsForPath(currentPath);

  // 如果没有精确匹配，使用默认配置
  if (!pageSpecificComponents[currentPath] && currentPath !== "/") {
    console.warn(
      `No specific configuration for path: ${currentPath}, using common components only`
    );
    componentConfigs = commonComponents;
  }

  // console.log(`Loading components for path: ${currentPath}`);

  // 过滤掉容器不存在的组件配置
  const validConfigs = componentConfigs.filter((config) => {
    if (!config.container) {
      console.warn(`Container not found for component: ${config.name}`);
      return false;
    }
    return true;
  });

  if (validConfigs.length > 0) {
    loadComponents(validConfigs);
  }
}

/**
 * 动态添加页面特定组件
 * @param {string} path - 页面路径
 * @param {Array} configs - 组件配置数组（会自动添加公共组件）
 */
export function addRoute(path, configs) {
  pageSpecificComponents[path] = configs;
}

/**
 * 获取某个路径的完整组件配置
 * @param {string} path - 页面路径
 * @returns {Array} 组件配置数组
 */
export function getRouteConfig(path) {
  return getComponentsForPath(path);
}
