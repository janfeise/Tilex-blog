<template>
  <el-container style="height: 100vh;">
    <!-- 左侧侧边栏 -->
    <el-aside width="200px">
      <Sidebar />
    </el-aside>

    <!-- 右侧内容区 -->
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
</script>
