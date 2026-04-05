<!-- 
  MarkdownTOC.vue
  
  Markdown 目录组件
  
  功能：
  - 显示文章目录
  - 支持不同级别的标题缩进
  - 点击跳转到对应位置
  - 高亮当前阅读位置
-->

<script setup>
import { defineProps, defineEmits, ref, onMounted, onUnmounted } from "vue";

const props = defineProps({
  // 目录数据
  toc: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["click"]);

// 当前活动的目录项 ID
const activeId = ref("");

// 观察者实例
let observer = null;

onMounted(() => {
  // 使用 IntersectionObserver 高亮当前阅读位置
  const headings = document.querySelectorAll(
    ".markdown-content h1, .markdown-content h2, .markdown-content h3, .markdown-content h4, .markdown-content h5, .markdown-content h6",
  );

  const observerOptions = {
    root: null,
    rootMargin: "-50px 0px -66%",
    threshold: 0,
  };

  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        activeId.value = entry.target.id;
      }
    });
  }, observerOptions);

  headings.forEach((heading) => {
    observer.observe(heading);
  });
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});

// 处理目录项点击
const handleClick = (id) => {
  emit("click", id);
};
</script>

<template>
  <nav class="markdown-toc">
    <div class="toc-title">目录</div>
    <ul class="toc-list">
      <li
        v-for="item in toc"
        :key="item.id"
        :class="[
          'toc-item',
          `level-${item.level}`,
          { active: activeId === item.id },
        ]"
      >
        <a
          :href="`#${item.id}`"
          class="toc-link"
          @click.prevent="handleClick(item.id)"
        >
          {{ item.title }}
        </a>
      </li>
    </ul>
  </nav>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.markdown-toc {
  padding: 1rem;
  color: a.$color-grey-dark;

  .toc-title {
    font-size: a.$font-size-small;
    font-weight: bold;
    margin-bottom: 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid a.$color-grey-light;
  }

  .toc-list {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  .toc-item {
    margin: 0.25rem 0;
    transition: all 0.2s ease;

    &.level-1 {
      margin-left: 0;
    }

    &.level-2 {
      margin-left: 1rem;
    }

    &.level-3 {
      margin-left: 2rem;
    }

    &.level-4 {
      margin-left: 3rem;
    }

    &.level-5 {
      margin-left: 4rem;
    }

    &.level-6 {
      margin-left: 5rem;
    }

    &.active {
      background-color: rgba(64, 158, 255, 0.1);
      border-radius: 4px;
    }

    .toc-link {
      display: block;
      padding: 0.5rem 0.75rem;
      color: a.$color-grey-dark;
      text-decoration: none;
      font-size: a.$font-size-small-sm;
      border-radius: 4px;
      transition: all 0.2s ease;

      &:hover {
        color: a.$color-grey-dark-2;
        background-color: rgba(64, 158, 255, 0.05);
      }
    }

    &.active .toc-link {
      color: a.$color-grey-dark-2;
      font-weight: 600;
    }
  }
}
</style>
