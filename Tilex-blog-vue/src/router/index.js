import { createRouter, createWebHistory } from "vue-router";
import articleRouter from "./modules/article";
import aboutRouter from "./modules/about";

// 导入各个模块的路由
const routes = [
  // 首页路由
  {
    path: "/",
    name: "Home",
    component: () => import("@/views/Home/index.vue"),
  },
  // 文章路由
  ...articleRouter,
  // 关于页面路由
  ...aboutRouter,
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
