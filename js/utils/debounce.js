/**
 * 防抖函数（Debounce）
 *
 * 在事件被触发后，只有当指定延迟时间内没有再次触发时，才会执行回调函数。
 * 适用于输入框输入、按钮点击、窗口 resize 等场景，避免函数被频繁调用造成性能问题。
 *
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟执行的时间（毫秒）
 * @returns {Function} 返回一个新的防抖函数
 *
 * 说明：
 * fn.apply(context, args) 中：
 * - context 为调用防抖函数时的 this，保证回调函数的上下文不丢失
 * - args 为收集的参数数组，会被展开传给原函数
 * - 普通函数，this谁调用指向谁，但由于 setTimeout 的嵌套导致内层函数的 this 指向丢失，需使用 apply 重新绑定this
 *
 * 示例：
 * const button = document.querySelector('#btn');
 * button.addEventListener('click', debounce(function() {
 *   console.log('this:', this); // 指向按钮 <button id="btn">
 * }, 300));
 */
function debounce(fn, delay = 300) {
  let timer = null;
  return function (...args) {
    const context = this; // 保存调用防抖函数时的 this
    clearTimeout(timer); // 清除上一次的定时器
    timer = setTimeout(() => fn.apply(context, args), delay); // 延迟执行函数
  };
}

export { debounce };
