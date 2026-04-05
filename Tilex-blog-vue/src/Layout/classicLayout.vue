<script setup>
/**
 * 经典布局：使用经典的博客布局，顶部有导航栏，左侧是文章列表，右侧是文章内容。
 *
 * 哪些页面使用这个布局：
 * - 文章详情页
 * - about页面
 * - 画廊页面
 */
import Nav from "@/components/Nav.vue";
</script>

<template>
  <Nav direction="topSide" />

  <main class="classic-layout">
    <slot name="classic-layout__content"></slot>
  </main>
</template>
