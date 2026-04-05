<!-- 
  ArticleTableOfContents.vue
  
  文章目录独立模块
  
  特性：
  - 独立的 TOC 管理和显示
  - 支持目录项点击跳转
  - 自动高亮当前阅读位置
  - 响应式设计
-->

<script setup>
import { defineProps, defineEmits } from "vue";
import MarkdownTOC from "./MarkdownTOC.vue";

defineProps({
  // TOC 数据数组
  toc: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits([
  "click-toc", // 点击目录项时触发
]);

// 处理目录项点击
const handleTOCClick = (id) => {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: "smooth" });
  }

  emit("click-toc", id);
};
</script>

<template>
  <div v-if="toc.length > 0" class="article-toc">
    <MarkdownTOC :toc="toc" @click="handleTOCClick" />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.article-toc {
  position: sticky;
  top: 2rem;
  height: fit-content;
  padding: 1rem;

  @media (max-width: a.$breakpoint-tablet) {
    display: none;
  }
}
</style>
