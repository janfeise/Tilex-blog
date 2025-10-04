/**
 * @fileoverview 该文件负责在页面加载完成后，使用 JS 动态加载所有的组件 HTML 内容。
 *
 * 实现思路：
 * 1. 在 DOMContentLoaded 事件触发后调用 `loadComponents`。
 * 2. `loadComponents` 会根据传入的组件名称自动拼接组件的 HTML 文件路径。
 * 3. 使用 `fetch` 异步加载组件 HTML 内容并插入到指定的 DOM 容器中。
 */

import { headerSeasonsContainer } from "../dom/headerDom.js";
import { loadComponents } from "../utils/loadComponent.js";
import { initSeasons } from "../components/seasons.js";

// 页面 DOM 加载完成后执行
window.addEventListener("DOMContentLoaded", () => {
  // 批量加载组件：只需传入容器和组件名(可选：初始化函数)
  loadComponents([
    {
      container: headerSeasonsContainer,
      name: "seasons",
      initFuc: initSeasons,
    },
  ]);
});
