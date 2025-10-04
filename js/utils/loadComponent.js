/**
 * @fileoverview 组件加载工具函数模块。
 *
 * 提供两种功能：
 * 1. `loadComponent`：加载单个组件 HTML 并插入到指定容器中。
 * 2. `loadComponents`：批量加载多个组件（传入组件名数组）。
 */

const BASE_PATH = "../../components/";
// 所有组件 HTML 文件的基础路径。
// 调用时只需传入组件名，代码自动拼接路径。

/**
 * 异步加载单个组件 HTML 并插入到页面中。
 *
 * @async
 * @param {HTMLElement} container - 要插入组件 HTML 的 DOM 容器。
 * @param {string} url - 组件 HTML 文件的完整路径。
 * @returns {Promise<void>} 加载完成后返回 Promise。
 */
const _loadComponent = async (container, url) => {
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`加载组件失败：${url}`);
    const html = await res.text();
    container.innerHTML = html;
  } catch (err) {
    console.error(err);
    container.innerHTML = `<p style="color:red;">组件加载失败，请检查路径：${url}</p>`;
  }
};

/**
 * 批量加载多个组件。
 *
 * 每个组件可选地指定 `onMounted` 回调函数，
 * 当该组件的 HTML 模板加载并插入 DOM 后，将自动执行回调。
 *
 * @async
 * @param {Array<{container: HTMLElement, name: string, initFuc?: Function}>} components - 组件配置数组。
 * @returns {Promise<void>} 所有组件加载完成后返回 Promise。
 *
 * @example
 * loadComponents([
 *   { container: headerContainer, name: "header", initFuc: initHeader },
 *   { container: footerContainer, name: "footer" }
 * ]);
 */
const loadComponents = async (components = []) => {
  if (!Array.isArray(components) || components.length === 0) {
    console.warn("⚠️ 未传入任何组件配置，loadComponents 未执行。");
    return;
  }

  for (const { container, name, initFuc } of components) {
    await _loadComponent(container, `${BASE_PATH}${name}.html`);
    if (typeof initFuc === "function") {
      try {
        initFuc(container); // 组件加载完再执行
      } catch (err) {
        console.error(`组件 ${name} 的 initFuc 执行失败:`, err);
      }
    }
  }
};

export { loadComponents };
