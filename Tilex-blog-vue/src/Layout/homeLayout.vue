<!-- HomeLayout.vue 首页布局文件 -->
<script setup>
import { Expand } from "@element-plus/icons-vue";
import Nav from "@/components/Nav.vue";

// 侧边栏展开状态
const isSideOpen = ref(true);
</script>

<template>
  <div class="home flex">
    <div class="home__side-container" :class="{ 'is-open': isSideOpen }">
      <Nav
        direction="leftSide"
        :isSideOpen="isSideOpen"
        @update:isSideOpen="isSideOpen = $event"
      />
    </div>

    <!-- 侧边栏展开按钮（独立于容器外） -->
    <Transition name="slide-left">
      <button
        id="close-btn"
        v-show="!isSideOpen"
        class="home__side-open-btn"
        @click="isSideOpen = true"
        aria-label="展开侧边栏"
      >
        <Expand />
      </button>
    </Transition>

    <div
      class="home__main-container"
      :style="{ marginLeft: isSideOpen ? '30rem' : '15rem' }"
    >
      <slot name="main"></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.slide-left-enter-active {
  animation: slide-left 0.3s ease-out;
}

.slide-left-leave-active {
  animation: slide-left 0.3s ease-out reverse; /* 离开时反向 */
}

@keyframes slide-left {
  0% {
    left: 0;
    opacity: 0;
  }
  100% {
    left: 5rem;
    opacity: 1;
  }
}
</style>
