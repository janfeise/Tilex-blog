<template>
  <el-menu :default-active="activeMenu" @select="handleSelect" router>
    <el-menu-item index="/upload">上传文章</el-menu-item>
  </el-menu>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.path)

const handleSelect = (index) => {
  activeMenu.value = index
  router.push(index)
}
</script>
