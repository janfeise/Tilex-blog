/**
 * 首页：截取文章预览内容
 * - 去除一级标题（# 开头的行）
 * - 限制最大长度
 *
 * @param {string} content 原始 Markdown 内容
 * @param {number} maxLength 最大字符数，默认 200
 * @returns {string} 预览内容
 */
export function getPreviewContent(content, maxLength = 200) {
  if (!content) return "";

  // 1. 按行拆分，去掉一级标题
  const lines = content.split("\n").filter((line) => !line.startsWith("# "));

  // 2. 合并为纯文本
  let plainText = lines.join(" ");

  // 4. 限制长度
  if (plainText.length > maxLength) {
    plainText = plainText.slice(0, maxLength) + "...";
  }

  return plainText;
}
