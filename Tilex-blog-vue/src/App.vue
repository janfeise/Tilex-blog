<script setup>
import { ArrowUp } from "@element-plus/icons-vue";
</script>

<template>
  <router-view />
  <el-backtop :right="7" :bottom="100" class="back-to-top">
    <el-icon color="a.$color-primary" size="large">
      <arrow-up />
    </el-icon>
  </el-backtop>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.back-to-top {
  background-color: a.$color-white;
}
</style>
