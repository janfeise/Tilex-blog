/**
 * 日期工具函数集
 * @module utils/date
 */

/**
 * 获取当前日期对象
 * @returns {Date} 当前日期对象
 */
export function getCurrentDate() {
  return new Date();
}

/**
 * 格式化日期为字符串
 * @param {Date|string|number} date - 要格式化的日期，可以是 Date 对象、时间戳或日期字符串
 * @param {string} format - 格式化模板，常用 "YYYY-MM-DD", "YYYY-MM-DD HH:mm:ss"
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = "YYYY-MM-DD") {
  const d = new Date(date);
  const pad = (n) => String(n).padStart(2, "0");

  const year = d.getFullYear();
  const month = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const hours = pad(d.getHours());
  const minutes = pad(d.getMinutes());
  const seconds = pad(d.getSeconds());

  return format
    .replace("YYYY", year)
    .replace("MM", month)
    .replace("DD", day)
    .replace("HH", hours)
    .replace("mm", minutes)
    .replace("ss", seconds);
}
