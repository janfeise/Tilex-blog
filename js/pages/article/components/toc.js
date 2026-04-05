/**
 * @fileoverview 渲染markdown文章的 目录
 */
import marked from "../../../utils/markdown.js";

/**
 * 生成博客文章的目录
 *
 * @param {string} markdownContent
 * @returns {{html: string, toc: Array}}
 */
function generateTOC(markdownContent) {
  // 移除可能的 \r（Windows 换行符）
  const lines = markdownContent.replace(/\r/g, "").split("\n");
  const toc = [];

  const headingRegex = /^(#{1,6})\s+(.*)$/; // 匹配 #、##、### 等标题
  /**
   * 由于在代码块中可能存在注释：#，而代码块中的注释 # 并不是标题，
   * 通过 inCodeBlock 作为标识避免将代码块中的注释识别为标题
   * 而代码块的特点：以```开始， 以```结束
   */
  let inCodeBlock = false; // 标识是否在多行代码块中

  for (const line of lines) {
    const trimmed = line.trim();

    // 检测多行代码块（``` 或 ~~~）
    if (/^(```|~~~)/.test(trimmed)) {
      inCodeBlock = !inCodeBlock;
      continue;
    }

    // 跳过多行代码块内部内容
    if (inCodeBlock) continue;

    // 跳过缩进代码块（以4个空格或制表符开头）
    if (/^( {4,}|\t)/.test(line)) continue;

    const match = line.match(headingRegex);
    if (match) {
      const level = match[1].length; // # 的数量
      const rawText = match[2].trim();

      // 去除行尾可能存在的 Markdown 符号，如 `#` 或多余空格
      const text = rawText.replace(/#+$/, "").trim();

      // 生成 id（常用于跳转锚点）
      const id = text
        .toLowerCase()
        .replace(/[`~!@#$%^&*()+=<>?,./:;"'|{}[\]\\]/g, "") // 移除符号
        .replace(/\s+/g, "-"); // 空格换成 -

      toc.push({ level, text, id });
    }
  }

  return toc;
}

/**
 * 渲染文章目录
 * @param {HTMLElement} tocDom - 目录容器 DOM
 * @param {Array} toc - 目录数组，每个元素包含 { text, level, id }
 */
function renderTOC(tocDom, toc) {
  if (!tocDom || !Array.isArray(toc)) return;

  // 清空容器
  tocDom.innerHTML = "";

  toc.forEach((item) => {
    const li = document.createElement("li");
    li.style.paddingLeft = (item.level - 1) * 10 + "px"; // 缩进显示层级

    const a = document.createElement("a");
    a.href = `#${item.id}`;
    a.textContent = item.text;

    li.appendChild(a);
    tocDom.appendChild(li);
  });
}

/**
 * 为博客的目录添加 上滑 动画
 * @param {HTMLElement} container - 包含文章的容器元素
 */
function addSlideUpAnimationOfTOC(container) {
  // 移出 hidden 类名
  container.classList.remove("hidden");

  // 先清除动画，确保重复调用时动画能重新触发
  container.style.animation = "none";
  void container.offsetWidth; // 强制回流，重新计算布局

  // 添加动画
  container.style.animation = "slide-up 1s ease-in-out";
}

export { generateTOC, renderTOC, addSlideUpAnimationOfTOC };
