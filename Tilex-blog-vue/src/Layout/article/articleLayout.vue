<script setup>
/**
 * 文章详情页布局：使用经典布局，自定义 classic-layout__content 区域，显示文章内容。
 */
import ClassicLayout from "@/Layout/classicLayout.vue";
</script>

<template>
  <ClassicLayout>
    <template #classic-layout__content>
      <div class="article-layout">
        <!-- 文章列表 -->
        <aside class="article-layout__sidebar">
          <slot name="sidebar" />
        </aside>

        <!-- 文章内容 -->
        <section class="article-layout__content">
          <slot name="content" />
        </section>

        <!-- 文章目录 -->
        <nav class="article-layout__toc">
          <slot name="toc" />
        </nav>
      </div>
    </template>
  </ClassicLayout>
</template>
