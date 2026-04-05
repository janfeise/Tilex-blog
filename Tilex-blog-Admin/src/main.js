import { createApp } from "vue";
import "./style.css";

// 整体导入 ElementPlus 组件库
import ElementPlus from "element-plus"; //导入 ElementPlus 组件库的所有模块和功能
import "element-plus/dist/index.css"; //导入 ElementPlus 组件库所需的全局 CSS 样式
import * as ElementPlusIconsVue from "@element-plus/icons-vue"; //导入 ElementPlus 组件库的图标模块

// 导入：md-editor的样式
import "md-editor-v3/lib/style.css";

// 路由
import router from "./router";

import App from "./App.vue";

const app = createApp(App);

// 将 ElementPlus 的图标组件注册到 Vue 应用中
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(ElementPlus);
app.use(router);

app.mount("#app");
