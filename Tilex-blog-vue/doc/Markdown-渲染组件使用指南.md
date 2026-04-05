# Markdown 文章渲染组件使用指南

## 概述

这是一个完整的 Markdown 文章渲染解决方案，包含以下模块：

- **markdown.js**：核心工具函数库
- **MarkdownRenderer.vue**：主渲染组件
- **MarkdownTOC.vue**：目录导航组件
- **MarkdownCode.vue**：代码块组件

## 核心特性

### 1. 统一入口

所有 Markdown 文章渲染都通过 `MarkdownRenderer` 组件，确保一致的处理流程和样式。

```vue
<template>
  <MarkdownRenderer
    :content="markdownContent"
    title="文章标题"
    :showTOC="true"
    :showReadingTime="true"
  />
</template>

<script setup>
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";
import { ref } from "vue";

const markdownContent = ref(`
# 标题
文章内容...
`);
</script>
```

### 2. 可扩展性

#### 自定义渲染选项

```vue
<MarkdownRenderer
  :content="markdown"
  :renderOptions="{
    includeTOC: true, // 包含目录
    includeAnchors: true, // 添加标题锚点
    includeLineNumbers: false, // 代码块行号
    includeCopyButton: true, // 复制按钮
    sanitize: true, // XSS 防护
  }"
/>
```

#### 自定义代码块选项

```vue
<MarkdownRenderer
  :content="markdown"
  :codeBlockOptions="{
    lineNumbers: true, // 显示行号
    copyButton: true, // 显示复制按钮
    language: true, // 显示语言标签
  }"
/>
```

#### 监听事件

```vue
<MarkdownRenderer
  :content="markdown"
  @toc-change="handleTOCChange"     // 目录更新
  @click-toc="handleTOCClick"       // 点击目录项
  @ready="handleReady"              // 组件加载完成
/>

<script setup>
const handleTOCChange = (toc) => {
  console.log('目录已更新:', toc);
};

const handleTOCClick = (id) => {
  console.log('点击的目录项:', id);
};

const handleReady = () => {
  console.log('组件已就绪');
};
</script>
```

### 3. 可控性

#### 安全过滤

默认启用 XSS 防护，自动清理不安全的 HTML 和脚本。

```vue
<!-- 启用（默认） -->
<MarkdownRenderer :content="markdown" :renderOptions="{ sanitize: true }" />

<!-- 禁用（仅在信任内容时） -->
<MarkdownRenderer :content="markdown" :renderOptions="{ sanitize: false }" />
```

#### 懒加载

自动为图片启用懒加载，提升首屏加载性能。

```vue
<!-- 启用（默认） -->
<MarkdownRenderer :content="markdown" :lazyLoad="true" />

<!-- 禁用 -->
<MarkdownRenderer :content="markdown" :lazyLoad="false" />
```

#### 目录生成

自动从 Markdown 标题生成目录，支持嵌套导航。

```vue
<!-- 显示目录 -->
<MarkdownRenderer :content="markdown" :showTOC="true" />

<!-- 隐藏目录 -->
<MarkdownRenderer :content="markdown" :showTOC="false" />
```

#### 强制指定字数

在首页等地方仅显示文章部分内容时，可通过 `wordCount` 属性强制指定完整文章的字数，使阅读时间基于完整内容计算。

```vue
<!-- 
  场景：首页显示文章预览（仅前400字）
  但阅读时间应基于完整文章（3500字）计算
-->
<MarkdownRenderer
  :content="trimMarkdown(article.content, 400)"
  :showReadingTime="true"
  :wordCount="article.wordCount || article.content.length"
/>

<!-- 输出：阅读时间约 18 分钟 · 字数 3500 -->
```

优先级规则：

- 若指定了 `wordCount`，则使用该值计算阅读时间
- 若未指定 `wordCount`，则使用实际 `content` 长度计算

## 工具函数 API

### `renderMarkdown(markdown, options)`

完整的 Markdown 处理流程。

```javascript
import { renderMarkdown } from "@/utils/markdown";

const result = renderMarkdown("# Hello\nWorld", {
  includeTOC: true,
  includeAnchors: true,
  sanitize: true,
});

console.log(result.html); // 渲染后的 HTML
console.log(result.toc); // 目录数组
```

### `trimMarkdown(markdown, limit)`

截取 Markdown 文本到指定字数，用于预览场景。

```javascript
import { trimMarkdown } from "@/utils/markdown";

// 截取前 400 个字符，并在末尾添加 "[阅读全文]" 提示
const preview = trimMarkdown(article.content, 400);
// 返回: "内容...。\n\n*[阅读全文]*"
```

特性：

- 自动在完整句子处截断（支持 。？！）
- 在已有段落标记处截断
- 自动添加 "[阅读全文]" 提示
- 避免在词中间截断

```javascript
import { parseMarkdown } from "@/utils/markdown";

const html = parseMarkdown("# 标题\n\n**粗体**正常文本");
```

### `generateTOC(markdown)`

从 Markdown 生成目录。

```javascript
import { generateTOC } from "@/utils/markdown";

const toc = generateTOC(markdown);
// 返回: [
//   { id: 'hello-world', title: 'Hello World', level: 1 },
//   { id: 'subtitle', title: 'Subtitle', level: 2 },
// ]
```

### `addHeadingAnchors(html)`

为 HTML 标题添加锚点。

```javascript
import { addHeadingAnchors } from "@/utils/markdown";

const html = addHeadingAnchors("<h1>标题</h1>");
// 返回: <h1 id="标题"><a href="#标题" class="anchor-link">#</a> 标题</h1>
```

### `addLineNumbers(html)`

为代码块添加行号。

```javascript
import { addLineNumbers } from "@/utils/markdown";

const html = addLineNumbers('<pre><code>console.log("hello")</code></pre>');
```

### `addCodeCopyButton(html)`

为代码块添加复制按钮。

```javascript
import { addCodeCopyButton } from "@/utils/markdown";

const html = addCodeCopyButton("<pre><code>...</code></pre>");
```

### `sanitizeHtml(html)`

清理不安全的 HTML。

```javascript
import { sanitizeHtml } from "@/utils/markdown";

const clean = sanitizeHtml('<p>Safe</p><script>alert("xss")</script>');
```

## 实际应用示例

### 博客详情页

```vue
<template>
  <homeLayout>
    <template #main>
      <div class="article-page">
        <MarkdownRenderer
          :key="articleId"
          :content="article.content"
          :title="article.title"
          :showTOC="true"
          :showReadingTime="true"
          :lazyLoad="true"
          @ready="onArticleReady"
        />
      </div>
    </template>
  </homeLayout>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import homeLayout from "@/Layout/homeLayout.vue";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";
import { getArticleById } from "@/api/blog";

const route = useRoute();
const article = ref({});
const articleId = ref(route.params.id);

onMounted(async () => {
  const res = await getArticleById(articleId.value);
  if (res.code === 209) {
    article.value = res.data;
  }
});

const onArticleReady = () => {
  console.log("文章加载完成");
};
</script>
```

### 首页博客卡片（预览 + 强制字数）

用于首页等地方显示文章列表，每条显示部分预览内容。

```vue
<!-- BlogCard.vue -->
<script setup>
import { defineProps } from "vue";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";
import { trimMarkdown } from "@/utils/markdown";

const props = defineProps({
  article: {
    type: Object,
    required: true,
    // 期望数据结构：
    // {
    //   id, title, content, wordCount, createdAt, tags, ...
    // }
  },
});

const getPreviewContent = (content, limit = 400) => {
  return trimMarkdown(content, limit);
};
</script>

<template>
  <div class="card">
    <div class="card__tips">
      <h2 class="card__title">{{ props.article.title }}</h2>
      <span class="card__date">{{ props.article.createdAt }}</span>
    </div>

    <div class="card__content">
      <!-- 
        关键点：使用 wordCount 指定完整文章字数
        这样预览显示 400 字，但阅读时间基于完整文章（比如 3500 字）计算
      -->
      <MarkdownRenderer
        :content="getPreviewContent(props.article.content, 400)"
        :showTOC="false"
        :showReadingTime="true"
        :wordCount="props.article.wordCount || props.article.content.length"
        :renderOptions="{
          includeTOC: false,
          includeAnchors: false,
          includeCopyButton: true,
          sanitize: true,
        }"
      />
    </div>

    <div class="card__footer">
      <div class="card__tags">
        <span v-for="tag in props.article.tags" :key="tag" class="card__tag">
          {{ tag }}
        </span>
      </div>
      <a :href="`/article/${props.article.id}`" class="read-more">
        全文阅读 >>
      </a>
    </div>
  </div>
</template>

<style scoped>
/* 样式定义... */
</style>
```

**后端数据格式建议**：

```json
{
  "id": "article-001",
  "title": "Vue3 性能优化指南",
  "content": "# Vue3 性能优化...",
  "wordCount": 3500,
  "createdAt": "2024-01-15",
  "tags": ["Vue", "性能优化"],
  "summary": "本文介绍了..."
}
```

### 预览组件（编辑器）

```vue
<template>
  <div class="preview-container">
    <div class="split-view">
      <div class="editor">
        <textarea
          v-model="markdown"
          placeholder="在这里输入 Markdown..."
          class="editor-input"
        ></textarea>
      </div>
      <div class="preview">
        <MarkdownRenderer
          :content="markdown"
          :showTOC="markdown.length > 1000"
          :showReadingTime="true"
          :lazyLoad="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";

const markdown = ref("");
</script>

<style scoped>
.preview-container {
  height: 100vh;
  display: flex;
}

.split-view {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  width: 100%;
  height: 100%;
}

.editor,
.preview {
  overflow: auto;
  padding: 1rem;
}

.editor-input {
  width: 100%;
  height: 100%;
  border: none;
  font-family: "Courier New", monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}
</style>
```

## 支持的 Markdown 语法

- 标题（h1-h6）
- 粗体、斜体、删除线
- 列表（有序、无序、嵌套）
- 代码块（带语言高亮）
- 行内代码
- 链接和图片
- 表格
- 块引用
- 分割线
- HTML 片段（带过滤）

## 代码高亮支持的语言

支持所有 highlight.js 包含的 180+ 种编程语言，常见的如：

- JavaScript / TypeScript
- Python / Java / C++ / C#
- HTML / CSS / SCSS / Less
- Vue / React / Angular
- SQL / MySQL / PostgreSQL
- JSON / YAML / XML
- Bash / PowerShell
- 等等...

## 性能优化

1. **代码分割**：组件使用独立加载，减少首屏包体积
2. **图片懒加载**：自动延迟加载非视口内的图片
3. **IntersectionObserver**：高效监听目录当前位置
4. **Markdown 缓存**：工具函数结果可自行缓存
5. **事件防抖**：适当场景下使用防抖减少重排

## 常见问题

**Q：如何自定义样式？**

A：组件使用 SCSS 预处理器和 CSS 变量，可在 `_variables.scss` 中修改主题色，也可覆盖 `.markdown-content` 类中的样式。

**Q：如何添加自定义插件？**

A：修改 `markdown.js` 中的 `markdownInstance` 配置，添加 markdown-it 插件：

```javascript
import mdTable from "markdown-it-table-of-contents";
markdownInstance.use(mdTable);
```

**Q：如何处理动态内容更新？**

A：使用 `:key` 绑定确保组件重新挂载：

```vue
<MarkdownRenderer :key="contentId" :content="content" />
```

**Q：支持数学公式吗？**

A：可安装 `markdown-it-katex` 或 `markdown-it-mathjax` 插件实现 LaTeX 支持。

**Q：如何在首页显示预览但保持正确的阅读时间？**

A：使用 `wordCount` 属性指定完整文章的字数：

```vue
<MarkdownRenderer
  :content="trimMarkdown(article.content, 400)"  <!-- 仅显示前400字 -->
  :wordCount="article.wordCount"                  <!-- 但基于完整字数计算阅读时间 -->
/>
```

这样可以在列表页显示简短预览，同时向用户显示真实的阅读时间。

**Q：trimMarkdown 函数的截断逻辑是什么？**

A：函数会优先在以下位置截断：

1. 中文句号（。）
2. 中文问号（？）
3. 中文感叹号（！）
4. 段落标记（\n\n）

如果上述位置都不存在，则直接在 `limit` 字符处截断。末尾会自动添加 `*[阅读全文]*` 提示。

**Q：如何获取到完整的文章字数？**

A：有两种方案：

1. 后端计算并返回 `wordCount` 字段
2. 前端在保存时计算：`article.wordCount = article.content.length`

建议在后端计算，因为后端可能会对 Markdown 进行额外处理（如去除注释等）。

## 总结

这个 Markdown 渲染方案通过统一入口、灵活配置和丰富的工具函数，实现了功能完整、易于维护的博客文章渲染系统。其中 `wordCount` 属性特别适合于列表页、预览卡片等场景，能够在显示部分内容的同时保持数据的准确性。
