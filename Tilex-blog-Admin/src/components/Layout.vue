<script setup>
import sidebar from "./sidebar.vue";
</script>

<template>
  <div class="layout">
    <!-- 公共导航栏 -->
    <div class="siderbar__wrap">
      <sidebar />
    </div>

    <div class="main-container">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  position: relative;
}

.siderbar__wrap {
  position: fixed;
  top: 0;
  left: 0;

  height: 100vh;
  width: 14rem;
  box-shadow: 5px 0 10px rgba(0, 0, 0, 0.2); /* 🔥 右侧阴影 */
}

.main-container {
  flex: 1;
  margin-left: 14rem;
}
</style>
