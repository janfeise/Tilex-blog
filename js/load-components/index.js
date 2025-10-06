/**
 * @fileoverview 该文件负责在页面加载完成后，使用 JS 动态加载所有的组件 HTML 内容。
 *
 * 实现思路：
 * 1. 在 DOMContentLoaded 事件触发后调用路由加载函数
 * 2. 根据 当前页面路径 自动加载对应的组件配置
 * 3. 使用现有的 loadComponents 工具函数完成实际加载
 */

import { loadPageComponents } from "../router.js";

// 页面 DOM 加载完成后执行
window.addEventListener("DOMContentLoaded", () => {
  // 使用路由配置自动加载当前页面的组件
  loadPageComponents();
});

// 导出路由相关函数，以便其他模块使用
export { loadPageComponents, addRoute, getRouteConfig } from "../router.js";
