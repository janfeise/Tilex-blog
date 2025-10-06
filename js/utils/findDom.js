/**
 * DOM 查找的操作工具函数
 *
 * 💡 使用建议
 *      必需元素：使用 findRequiredElement（找不到会抛错）
 *      可选元素：使用 safeUpdate（找不到不会报错）
 *      批量查找：使用 findRequiredElements 或 findOptionalElements
 */

/**
 * 查找必需的 DOM 元素（找不到会抛出错误）
 * @param {HTMLElement} container - 容器元素
 * @param {string} selector - CSS 选择器
 * @param {string} [errorMsg] - 自定义错误信息
 * @returns {HTMLElement} 找到的元素
 * @throws {Error} 当元素不存在时抛出错误
 */
export function findRequiredElement(container, selector, errorMsg) {
  if (!container || !(container instanceof HTMLElement)) {
    throw new Error("findRequiredElement: 无效的容器元素");
  }

  const element = container.querySelector(selector);

  if (!element) {
    const message = errorMsg || `未找到必需元素: ${selector}`;
    console.error(message);
    throw new Error(message);
  }

  return element;
}

/**
 * 查找可选的 DOM 元素（找不到会警告但不报错）
 * @param {HTMLElement} container - 容器元素
 * @param {string} selector - CSS 选择器
 * @param {string} [warnMsg] - 自定义警告信息
 * @returns {HTMLElement|null} 找到的元素或 null
 */
export function findOptionalElement(container, selector, warnMsg) {
  if (!container || !(container instanceof HTMLElement)) {
    console.warn("findOptionalElement: 无效的容器元素");
    return null;
  }

  const element = container.querySelector(selector);

  if (!element) {
    const message = warnMsg || `未找到可选元素: ${selector}`;
    console.warn(message);
  }

  return element;
}

/**
 * 批量查找多个特定选择器对应的元素，但每个选择器 只返回第一个匹配的元素
 * @param {HTMLElement} container - 容器元素
 * @param {Object} selectors - 选择器映射表 { key: selector }
 * @returns {Object} 元素映射表 { key: element }
 * @throws {Error} 当任何元素不存在时抛出错误
 *
 * @example
 * const elements = findRequiredElements(container, {
 *   title: '.seasons__title',
 *   content: '.seasons__content',
 *   date: '.seasons__date'
 * });
 * // 使用方法: elements.title, elements.content, elements.date
 */
export function findRequiredElements(container, selectors) {
  const elements = {};

  for (const [key, selector] of Object.entries(selectors)) {
    elements[key] = findRequiredElement(container, selector);
  }

  return elements;
}

/**
 * 批量查找可选元素
 * @param {HTMLElement} container - 容器元素
 * @param {Object} selectors - 选择器映射表 { key: selector }
 * @returns {Object} 元素映射表 { key: element | null }
 */
export function findOptionalElements(container, selectors) {
  const elements = {};

  for (const [key, selector] of Object.entries(selectors)) {
    elements[key] = findOptionalElement(container, selector);
  }

  return elements;
}

/**
 * 安全地操作 DOM 元素（如果元素存在才执行回调）
 * @param {HTMLElement} container - 容器元素
 * @param {string} selector - CSS 选择器
 * @param {Function} callback - 操作元素的回调函数
 * @returns {boolean} 是否成功执行操作
 *
 * @example
 * safeUpdate(container, '.seasons__title', (el) => {
 *   el.textContent = '春天';
 * });
 */
export function safeUpdate(container, selector, callback) {
  const element = findOptionalElement(container, selector);

  if (element && typeof callback === "function") {
    callback(element);
    return true;
  }

  return false;
}

/**
 * 批量查找所有匹配元素（返回 NodeList 或 Array）
 * @param {HTMLElement} container - 容器元素
 * @param {Object} selectors - 选择器映射表 { key: selector }
 * @returns {Object} 元素列表映射表 { key: NodeList | [] }
 */
export function findAllElements(container, selectors) {
  const elements = {};

  for (const [key, selector] of Object.entries(selectors)) {
    elements[key] = Array.from(container.querySelectorAll(selector));
  }

  return elements;
}
