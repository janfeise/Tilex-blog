/**
 * @description 关于页面路由
 */

const aboutRouter = [
  {
    path: "/about",
    name: "About",
    component: () => import("@/views/About/index.vue"),
  },
];

export default aboutRouter;
