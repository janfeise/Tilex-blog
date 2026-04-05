/**
 * 文章路由
 */

const articleRouter = [
  {
    path: "/article/:id",
    name: "ArticleDetail",
    component: () => import("@/views/Article/ArticleView.vue"),
  },
];

export default articleRouter;
