<!-- 侧边导航栏 -->
<script setup>
import { useRouter, useRoute } from "vue-router"; // 路由

//选中菜单触发的回调
const router = useRouter();
const route = useRoute();

const handleOpen = (index) => {
  if (index === "/upload") {
    router.push("/upload");
  }
  if (index === "/gallery") {
    router.push("/gallery");
  }
};
</script>

<template>
  <span class="sidebar__title">Tilex Blog</span>
  <div class="sidebar__container">
    <el-menu
      router
      unique-opened
      :default-active="route.path"
      @open="handleOpen"
    >
      <!-- 博客管理  -->
      <el-sub-menu index="/upload" class="sidebar__menu">
        <template #title>
          <span>博客管理</span>
        </template>
        <el-menu-item index="/upload">
          <template #title>上传博客</template>
        </el-menu-item>
        <el-menu-item index="/edit">
          <template #title>编辑博客</template>
        </el-menu-item>
      </el-sub-menu>

      <!-- 画廊管理 -->
      <el-sub-menu index="/gallery">
        <template #title>
          <span>画廊管理</span>
        </template>
        <el-menu-item index="/gallery">图片列表</el-menu-item>
        <el-menu-item index="/upload-image">上传图片</el-menu-item>
        <el-menu-item index="/tag-management">标签管理</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<style scoped>
.sidebar__title {
  display: block;
  padding: 2rem;
  color: #f7f7f7;
  font-size: 2rem;
  font-family: "PixelOperator";
  background-color: #333;
  text-align: center;
}

.sidebar__container {
  overflow: hidden;
  height: 100%;
}

.el-menu {
  height: 100%;
}

.el-menu,
.el-menu-item {
  background-color: #333;
}

.el-menu-item {
  width: 100%;
}

.el-sub-menu {
  border-bottom: 1px solid #eee;
}

.el-sub-menu__title {
  text-align: center;
}

.el-menu .el-menu-item:first-child,
.el-sub-menu {
  width: 100%;
  border-top: 1px solid #eee;
}

.el-menu .el-menu-item,
.el-menu .el-sub-menu span {
  color: #f7f7f7;
  border-bottom: 1px solid #eee;
  font-size: 1.6rem;
  font-family: "KaiTi";
}

::v-deep(.el-sub-menu__title:hover) {
  background-color: #f7f7ff;
}

::v-deep(.el-sub-menu__title:hover),
::v-deep(.el-sub-menu__title:hover) span {
  color: #333;
}

::v-deep(.el-menu-item:hover),
::v-deep(.el-menu-item.is-active) {
  color: #333;
  background-color: #f7f7ff;
}

/* 针对一级菜单的标题区域 */
:deep(.el-sub-menu__title) {
  letter-spacing: 1rem;
}
</style>
