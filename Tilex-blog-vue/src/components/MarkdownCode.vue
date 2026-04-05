<!-- 
  MarkdownCode.vue
  
  Markdown 代码块渲染组件
  
  功能：
  - 代码高亮显示
  - 添加复制按钮
  - 可选的行号显示
  - 语言标识
-->

<script setup>
import { defineProps } from "vue";

const props = defineProps({
  // 代码内容
  code: {
    type: String,
    required: true,
  },

  // 编程语言
  language: {
    type: String,
    default: "text",
  },

  // 是否显示行号
  showLineNumbers: {
    type: Boolean,
    default: true,
  },

  // 是否显示语言标签
  showLanguage: {
    type: Boolean,
    default: true,
  },
});

// 处理复制
const handleCopy = () => {
  navigator.clipboard.writeText(props.code).then(() => {
    // 显示复制成功提示
    const event = new CustomEvent("code-copied", {
      detail: { language: props.language },
    });
    window.dispatchEvent(event);
  });
};
</script>

<template>
  <div class="markdown-code">
    <div class="code-header">
      <span v-if="showLanguage" class="code-language">{{ language }}</span>
      <button class="copy-button" @click="handleCopy">复制代码</button>
    </div>

    <pre class="code-content"><code>{{ code }}</code></pre>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.markdown-code {
  border-radius: 8px;
  overflow: hidden;
  background-color: #0d1117;
  margin: 1.5rem 0;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background-color: #010409;
  border-bottom: 1px solid #30363d;

  .code-language {
    color: #79c0ff;
    font-weight: 600;
    font-size: 0.85rem;
    text-transform: uppercase;
  }

  .copy-button {
    padding: 0.4rem 0.8rem;
    background-color: transparent;
    border: 1px solid a.$color-primary;
    color: a.$color-primary;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85rem;
    transition: all 0.2s ease;

    &:hover {
      background-color: a.$color-primary;
      color: white;
    }

    &:active {
      transform: scale(0.98);
    }
  }
}

.code-content {
  margin: 0;
  padding: 1rem;
  overflow-x: auto;
  font-family: "Courier New", "Monaco", monospace;
  font-size: a.$font-size-small;
  line-height: 1.6;
  color: #c9d1d9;

  code {
    font-size: inherit;
  }
}

:deep(.hljs-string) {
  color: #a5d6ff;
}

:deep(.hljs-literal) {
  color: #79c0ff;
}

:deep(.hljs-number) {
  color: #79c0ff;
}

:deep(.hljs-attr) {
  color: #79c0ff;
}

:deep(.hljs-variable) {
  color: #ff7b72;
}

:deep(.hljs-template-variable) {
  color: #ff7b72;
}

:deep(.hljs-strong) {
  color: #ff7b72;
}

:deep(.hljs-emphasis) {
  color: #a5d6ff;
  font-style: italic;
}

:deep(.hljs-quote) {
  color: #8b949e;
}

:deep(.hljs-health) {
  color: #a5d6ff;
}

:deep(.hljs-deletion) {
  color: #ff7b72;
}

:deep(.hljs-section) {
  color: #79c0ff;
}

:deep(.hljs-link) {
  color: #a5d6ff;
}

:deep(.hljs-selector-attr) {
  color: #a5d6ff;
}

:deep(.hljs-selector-pseudo) {
  color: #a5d6ff;
}

:deep(.hljs-selector-tag) {
  color: #ff7b72;
}

:deep(.hljs-name) {
  color: #ff7b72;
}

:deep(.hljs-attr) {
  color: #79c0ff;
}

:deep(.hljs-keyword) {
  color: #ff7b72;
}

:deep(.hljs-selector-id) {
  color: #79c0ff;
}

:deep(.hljs-literal) {
  color: #79c0ff;
}

:deep(.hljs-selector-class) {
  color: #a5d6ff;
}

:deep(.hljs-attribute) {
  color: #a5d6ff;
}

:deep(.hljs-regex) {
  color: #a5d6ff;
}

:deep(.hljs-symbol) {
  color: #79c0ff;
}

:deep(.hljs-built-in) {
  color: #ff7b72;
}

:deep(.hljs-constructor) {
  color: #ff7b72;
}

:deep(.hljs-title) {
  color: #d2a8ff;
}

:deep(.hljs-section) {
  color: #79c0ff;
}

:deep(.hljs-meta) {
  color: #8b949e;
}

:deep(.hljs-params) {
  color: #c9d1d9;
}

:deep(.hljs-bullet) {
  color: #ff7b72;
}

:deep(.hljs-comment) {
  color: #8b949e;
}
</style>
