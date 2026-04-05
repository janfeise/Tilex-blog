<!-- 
  MarkdownRenderer.vue
  
  Markdown 文章渲染组件
  
  特性：
  - 统一的 Markdown 解析入口
  - 自动生成目录
  - 代码块高亮和复制功能
  - 标题锚点跳转
  - XSS 防护
  - 懒加载支持
-->

<script setup>
import { defineProps, computed, ref, onMounted } from "vue";
import { renderMarkdown } from "@/utils/markdown";
import MarkdownCode from "./MarkdownCode.vue";

const props = defineProps({
  // 必需：Markdown 文本内容
  content: {
    type: String,
    required: true,
  },

  // 可选：文章标题
  title: {
    type: String,
    default: "",
  },

  // 可选：是否显示目录（仅控制显示，不影响 TOC 数据生成）
  showTOC: {
    type: Boolean,
    default: false,
  },

  // 可选：是否显示阅读时间
  showReadingTime: {
    type: Boolean,
    default: true,
  },

  // 可选：强制指定字数（用于部分内容展示场景）
  // 若指定，则以此为准计算阅读时间，否则使用实际内容长度
  wordCount: {
    type: Number,
    default: null,
  },

  // 可选：是否启用图片懒加载
  lazyLoad: {
    type: Boolean,
    default: true,
  },

  // 可选：代码块配置
  codeBlockOptions: {
    type: Object,
    default: () => ({
      lineNumbers: true,
      copyButton: true,
      language: true,
    }),
  },

  // 可选：渲染选项
  renderOptions: {
    type: Object,
    default: () => ({
      includeTOC: true,
      includeAnchors: true,
      includeLineNumbers: false,
      includeCopyButton: true,
      sanitize: true,
    }),
  },
});

const emit = defineEmits([
  "toc-change", // 目录更新时触发
  "ready", // 组件加载完成时触发
]);

// 渲染后的 HTML 内容
const renderedHTML = ref("");

// 目录数据
const tableOfContents = ref([]);

// 阅读时间（分钟）
const readingTime = computed(() => {
  // 优先使用指定的字数，否则使用实际内容长度
  const words =
    props.wordCount !== null ? props.wordCount : props.content.length;
  return Math.ceil(words / 200); // 按每分钟 200 字算
});

// 计算 HTML 内容
const processedHTML = computed(() => {
  if (!props.content) return "";

  const result = renderMarkdown(props.content, props.renderOptions);
  tableOfContents.value = result.toc;

  return result.html;
});

// 初始化组件
onMounted(() => {
  renderedHTML.value = processedHTML.value;
  emit("toc-change", tableOfContents.value);
  emit("ready");

  // 设置图片懒加载
  if (props.lazyLoad) {
    setupLazyLoad();
  }

  // 为代码块绑定事件
  bindCodeBlockEvents();
});

// 图片懒加载设置
const setupLazyLoad = () => {
  if ("IntersectionObserver" in window) {
    const images = document.querySelectorAll(".markdown-content img");
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target;
          if (img.dataset.src) {
            img.src = img.dataset.src;
            img.removeAttribute("data-src");
          }
          observer.unobserve(img);
        }
      });
    });

    images.forEach((img) => observer.observe(img));
  }
};

// 为代码块的复制按钮绑定事件
const bindCodeBlockEvents = () => {
  const copyButtons = document.querySelectorAll(".code-block [data-code]");

  copyButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const code = decodeURIComponent(btn.dataset.code);
      navigator.clipboard.writeText(code).then(() => {
        const originalText = btn.textContent;
        btn.textContent = "已复制";
        setTimeout(() => {
          btn.textContent = originalText;
        }, 2000);
      });
    });
  });
};
</script>

<template>
  <div class="markdown-renderer">
    <!-- 文章头部 -->
    <div v-if="title || showReadingTime" class="markdown-header">
      <h1 v-if="title" class="markdown-title">{{ title }}</h1>
      <div v-if="showReadingTime" class="reading-info">
        阅读时间约 {{ readingTime }} 分钟 · 字数
        {{ wordCount !== null ? wordCount : content.length }}
      </div>
    </div>

    <!-- 主容器 -->
    <div class="markdown-container">
      <!-- 内容区域 -->
      <div class="markdown-content-wrapper">
        <div class="markdown-content" v-html="processedHTML"></div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.markdown-renderer {
  width: 100%;
}

.markdown-header {
  margin-bottom: 3rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid a.$color-grey-light;

  .markdown-title {
    margin-top: -2rem;
    font-size: a.$font-size-xxl;
    font-weight: bold;
    margin-bottom: 1rem;
    line-height: 1.3;
  }

  .reading-info {
    color: a.$color-grey-dark;
    font-size: a.$font-size-small-sm;
  }
}

.markdown-container {
  @media (max-width: a.$breakpoint-tablet) {
    grid-template-columns: 1fr;
  }
}

.markdown-content-wrapper {
  min-height: 300px;
}

.markdown-content {
  line-height: 1.8;
  color: a.$color-black;

  // 标题样式
  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    font-weight: bold;
    margin-top: 1.5rem;
    margin-bottom: 0.5rem;
    line-height: 1.3;
    scroll-margin-top: 2rem;

    // 锚点链接
    .anchor-link {
      color: a.$color-primary;
      text-decoration: none;
      margin-right: 0.5rem;
      opacity: 0;
      transition: opacity 0.2s;

      &:hover {
        opacity: 1;
      }
    }

    &:hover .anchor-link {
      opacity: 1;
    }
  }

  h1 {
    font-size: 2rem;
  }

  h2 {
    font-size: 1.5rem;
    padding-bottom: 0.5rem;
    border-bottom: 2px solid a.$color-primary;
  }

  h3 {
    font-size: 1.2rem;
  }

  h4,
  h5,
  h6 {
    font-size: 1rem;
  }

  // 段落和文本
  p {
    margin-bottom: 1rem;
    text-align: justify;
  }

  strong {
    font-weight: bold;
    color: a.$color-primary;
  }

  em {
    font-style: italic;
    color: a.$color-grey;
  }

  // 列表
  ul,
  ol {
    margin-bottom: 1rem;
    margin-left: 2rem;

    li {
      margin-bottom: 0.5rem;
    }
  }

  // 链接
  a {
    color: a.$color-primary;
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: border-color 0.2s;

    &:hover {
      border-bottom-color: a.$color-primary;
    }
  }

  // 代码（仅限于行内代码，不包括代码块内的code）
  code:not(.hljs) {
    padding: 0.2rem 0.4rem;
    border-radius: 3px;
    font-family: "Courier New", monospace;
    font-size: a.$font-size-small;
    color: #e83e8c;
    background-color: #f0f0f0;
  }

  // 代码块（由 MarkdownCode 组件处理）
  pre {
    margin-bottom: 1.5rem;
    border-radius: 8px;
    overflow-x: auto;
    background: none !important;

    code {
      background: none !important;
      padding: 0;
      color: inherit;
    }
  }

  // 块引用
  blockquote {
    margin-left: 0;
    padding-left: 1rem;
    border-left: 3px solid a.$color-primary;
    color: a.$color-grey;
    background-color: rgba(0, 0, 0, 0.02);
    padding: 0.5rem 1rem;
    margin-bottom: 1rem;
  }

  // 表格
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 1.5rem;

    th,
    td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid a.$color-grey-light;
    }

    th {
      background-color: a.$color-bg;
      font-weight: bold;
    }

    tr:hover {
      background-color: rgba(0, 0, 0, 0.02);
    }
  }

  // 图片
  img {
    max-width: 100%;
    height: auto;
    margin: 1.5rem 0;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  // 分割线
  hr {
    margin: 2rem 0;
    border: none;
    border-top: 2px solid a.$color-grey-light;
  }
}

:deep(.code-block) {
  margin-bottom: 1.5rem;
  border-radius: 8px;
  overflow: hidden;
  background-color: #0d1117;

  .code-block-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 1rem;
    background-color: #010409;
    color: #c9d1d9;
    font-size: a.$font-size-small;
  }

  .code-language {
    font-weight: bold;
    color: #79c0ff;
  }

  .copy-btn {
    background: none;
    border: none;
    color: a.$color-primary;
    cursor: pointer;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    transition: background-color 0.2s;

    &:hover {
      background-color: rgba(64, 158, 255, 0.1);
    }
  }

  pre {
    margin: 0 !important;
    border-radius: 0 !important;
    background-color: #0d1117 !important;
    color: #c9d1d9 !important;
    font-family: "Courier New", "Monaco", monospace !important;
    font-size: a.$font-size-large !important;
    line-height: 1.6 !important;
  }

  code {
    background-color: transparent !important;
    color: #c9d1d9 !important;
    padding: 0 !important;
    border-radius: 0 !important;
  }
}
</style>
