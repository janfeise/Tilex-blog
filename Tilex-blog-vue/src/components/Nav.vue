<script setup>
/**
 * Nav.vue
 * 导航栏组件
 *
 * @description
 * 控制导航栏方向
 *
 * @param direction { 'leftSide' | 'topSide' }
 */
import { defineProps, defineEmits, ref, onMounted, onUnmounted } from "vue";
import { Close } from "@element-plus/icons-vue";

const emit = defineEmits(["update:isSideOpen"]);

const props = defineProps({
  direction: {
    type: String,
    default: "leftSide",
  },
  isSideOpen: {
    type: Boolean,
    default: true,
  },
});

// 菜单数据
const menus = ref([
  { index: "1", title: "首页", route: "/" },
  { index: "4", title: "画廊", route: "/gallery" },
]);

// profile 菜单数据
const profileMenus = ref([
  { index: "1", title: "归档", route: "/archive" },
  { index: "2", title: "友链", route: "/links" },
  { index: "3", title: "关于我", route: "/about" },
]);

// 监听关闭按钮快捷键
onMounted(() => {
  document.addEventListener("keydown", handler);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handler);
});

const handler = (e) => {
  if (e.key === "Escape") {
    emit("update:isSideOpen", !props.isSideOpen);
  }
};
</script>

<template>
  <div
    v-show="isSideOpen"
    class="nav"
    :class="{
      flex: direction === 'topSide',
      'flex-column': direction === 'leftSide',
      nav__top: direction === 'topSide',
    }"
    :style="{ height: direction === 'topSide' ? '10rem' : '100vh' }"
  >
    <div v-if="direction === 'topSide'" class="nav__top--logo">
      <!-- 顶部导航栏 -->
      <span @click="$router.push('/')">Tilex Blog</span>
    </div>

    <div v-else-if="direction === 'leftSide'">
      <div class="nav__left-side--logo flex-column">
        <div class="nav__left-side--profile flex-between">
          <img src="../assets/pic/avatar.png" alt="Profile Picture" />
          <Close
            class="nav__close-btn"
            @click="emit('update:isSideOpen', false)"
          />
        </div>

        <div class="nav__left-side--tag">
          <h3>Tilex</h3>
          <span>追寻炽烈的爱</span>
        </div>
      </div>
    </div>

    <div class="nav__links">
      <el-menu
        :default-active="direction === 'leftSide' ? 1 : ''"
        :mode="direction === 'topSide' ? 'horizontal' : 'vertical'"
        ellipsis="false"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.index"
          :index="item.index"
          @click="$router.push(item.route)"
        >
          {{ item.title }}
        </el-menu-item>

        <div
          :class="{
            'nav__left-side--profile-links': direction === 'leftSide',
            'nav__top--profile-links': direction === 'topSide',
          }"
        >
          <span
            v-for="item in profileMenus"
            :key="item.index"
            @click="$router.push(item.route)"
          >
            {{ item.title }}
          </span>
        </div>
      </el-menu>
    </div>
  </div>
</template>
