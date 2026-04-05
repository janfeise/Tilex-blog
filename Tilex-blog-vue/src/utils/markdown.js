/**
 * @fileoverview Markdown 渲染工具库
 *
 * 功能：
 * 1. 初始化 markdown-it 和 highlight.js
 * 2. 处理代码块高亮
 * 3. 生成目录
 * 4. 处理 HTML 安全过滤
 */

import MarkdownIt from "markdown-it";
import hljs from "highlight.js";
import "highlight.js/styles/atom-one-dark.css";

// 创建 markdown-it 实例
const markdownInstance = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang, ignoreIllegals: true })
          .highlighted;
      } catch (__) {}
    }
    return ""; // 使用默认的逃逸
  },
});

/**
 * 解析 Markdown 文本为 HTML
 * @param {string} markdown - Markdown 文本
 * @returns {string} 渲染后的 HTML
 */
export function parseMarkdown(markdown) {
  if (!markdown) return "";

  const html = markdownInstance.render(markdown);
  return html;
}

/**
 * 生成文章目录
 * @param {string} markdown - Markdown 文本
 * @returns {Array} 目录数组 [{ id, title, level }]
 */
export function generateTOC(markdown) {
  if (!markdown) return [];

  const headingRegex = /^(#{1,6})\s+(.+)$/gm;
  const toc = [];
  let match;

  while ((match = headingRegex.exec(markdown)) !== null) {
    const level = match[1].length;
    const title = match[2];
    const id = generateIdFromTitle(title);

    toc.push({ id, title, level });
  }

  return toc;
}

/**
 * 根据标题生成 HTML ID
 * @param {string} title - 标题文本
 * @returns {string} 生成的 ID
 */
export function generateIdFromTitle(title) {
  return title
    .toLowerCase()
    .replace(/[^\u4e00-\u9fa5a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

/**
 * 在 HTML 中添加标题锚点
 * @param {string} html - HTML 字符串
 * @returns {string} 添加锚点后的 HTML
 */
export function addHeadingAnchors(html) {
  return html.replace(/<h([1-6])>([^<]+)<\/h\1>/g, (match, level, content) => {
    const id = generateIdFromTitle(content);
    return `<h${level} id="${id}"><a href="#${id}" class="anchor-link">#</a> ${content}</h${level}>`;
  });
}

/**
 * 在代码块中添加行号
 * @param {string} html - HTML 字符串
 * @returns {string} 添加行号后的 HTML
 */
export function addLineNumbers(html) {
  return html.replace(
    /<pre><code([^>]*)>([\s\S]*?)<\/code><\/pre>/g,
    (match, attrs, code) => {
      const lines = code
        .split("\n")
        .filter((line, idx, arr) => idx < arr.length - 1 || line);
      const numberedLines = lines
        .map(
          (line, idx) => `<span class="line-number">${idx + 1}</span>${line}`,
        )
        .join("\n");

      return `<pre><code${attrs}><span class="code-lines">${numberedLines}</span></code></pre>`;
    },
  );
}

/**
 * 在代码块中添加复制按钮
 * @param {string} html - HTML 字符串
 * @returns {string} 添加复制按钮后的 HTML
 */
export function addCodeCopyButton(html) {
  return html.replace(
    /<pre><code([^>]*)>([\s\S]*?)<\/code><\/pre>/g,
    (match, attrs, code) => {
      const language = attrs.match(/language-(\w+)/)?.at(1) || "text";
      const escapedCode = code
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&amp;/g, "&");

      return `
      <div class="code-block" data-language="${language}">
        <div class="code-block-header">
          <span class="code-language">${language}</span>
          <button class="copy-btn" data-code="${encodeURIComponent(escapedCode)}">复制代码</button>
        </div>
        <pre><code${attrs}>${code}</code></pre>
      </div>
    `;
    },
  );
}

/**
 * XSS 防护：清理不安全的 HTML
 * @param {string} html - 原始 HTML
 * @returns {string} 清理后的 HTML
 */
export function sanitizeHtml(html) {
  const unsafePatterns = [
    /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,
    /on\w+\s*=\s*["'][^"']*["']/gi,
    /on\w+\s*=\s*[^\s>]*/gi,
    /<iframe\b[^>]*>/gi,
  ];

  let sanitized = html;
  for (const pattern of unsafePatterns) {
    sanitized = sanitized.replace(pattern, "");
  }

  return sanitized;
}

/**
 * 完整的 Markdown 处理流程
 * @param {string} markdown - Markdown 文本
 * @param {Object} options - 选项
 * @returns {Object} { html, toc }
 */
export function renderMarkdown(markdown, options = {}) {
  const {
    includeTOC = true,
    includeAnchors = true,
    includeLineNumbers = false,
    includeCopyButton = true,
    sanitize = true,
  } = options;

  if (!markdown) return { html: "", toc: [] };

  // 1. 解析 Markdown 为 HTML
  let html = parseMarkdown(markdown);

  // 2. 添加标题锚点
  if (includeAnchors) {
    html = addHeadingAnchors(html);
  }

  // 3. 添加代码块功能
  if (includeLineNumbers) {
    html = addLineNumbers(html);
  }
  if (includeCopyButton) {
    html = addCodeCopyButton(html);
  }

  // 4. XSS 防护
  if (sanitize) {
    html = sanitizeHtml(html);
  }

  // 5. 生成目录
  const toc = includeTOC ? generateTOC(markdown) : [];

  return { html, toc };
}

/**
 * 获取支持的语言列表
 * @returns {Array} 语言列表
 */
export function getSupportedLanguages() {
  return hljs.listLanguages();
}

/**
 * 获取 markdown-it 实例（用于高级定制）
 * @returns {MarkdownIt} markdown-it 实例
 */
export function getMarkdownInstance() {
  return markdownInstance;
}

/**
 * 截取 Markdown 文本到指定字数（用于预览）
 * @param {string} markdown - Markdown 文本
 * @param {number} limit - 字数限制
 * @returns {string} 截取后的 Markdown 文本
 */
export function trimMarkdown(markdown, limit = 400) {
  if (!markdown) return "";

  if (markdown.length <= limit) {
    return markdown;
  }

  // 截取到 limit 个字符
  let trimmed = markdown.substring(0, limit);

  // 避免在中间词处截断，尝试截到最后一个完整的句子（句号、问号、感叹号）或段落
  const lastSentenceMatch = trimmed.lastIndexOf("。");
  const lastQuestionMark = trimmed.lastIndexOf("？");
  const lastExclamation = trimmed.lastIndexOf("！");
  const lastParagraph = trimmed.lastIndexOf("\n\n");

  const positions = [
    lastSentenceMatch,
    lastQuestionMark,
    lastExclamation,
    lastParagraph,
  ].filter((pos) => pos > 0);

  if (positions.length > 0) {
    const lastPos = Math.max(...positions);
    trimmed = trimmed.substring(0, lastPos + 1);
  }

  return trimmed + "\n\n*[阅读全文]*";
}
