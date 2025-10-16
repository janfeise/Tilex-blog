# 时闲的博客

> 时间：2025/10/03

## 技术栈

- 原生js
- SCSS
- Spring Boot

---

启动项目：

前端：

```cmd
npx vite
```

> 由于 `import hljs from "highlight.js"` 使用了 ESModule 语法和现代依赖解析，必须通过 `npx vite` 等构建工具启动项目来正确处理模块打包和依赖加载（node_modules），浏览器才能正常识别和运行。

后端：

- 数据库打开
- springboot启动

---

## 功能概述

| 功能名称 | 功能描述                                             | 实现时间   |
| -------- | ---------------------------------------------------- | ---------- |
| 季节图   | 根据当前日期自动判断季节，动态切换背景样式和标题展示 | 2025/10/04 |

---

目录

```pgsql
E:.  
|   index.html               -- 首页
|  
+---assets                   -- 存放图片、图标、字体等静态资源
|   +---fonts                 -- 字体文件
|   +---icon                  -- 图标文件
|   \---pic                   -- 图片资源
|   
+---components               -- 可复用组件
|       blog-article.html     -- 博客页面的文章组件
|       footer.html           -- 页脚组件
|       nav.html              -- 导航栏组件
|       seasons.html          -- 季节相关组件
|   
+---css                       -- 存放各类CSS文件
|       style.css            -- 全局样式，有scss自动生成
|       style.css.map        -- CSS映射文件，用于调试  
|   
+---js                        -- 存放各类JS文件
|   main.js                  -- 项目主要逻辑文件  
|   router.js                -- 路由控制文件：控制各个页面加载的组件
|   +---components            -- 组件相关JS
|   |       index.js          -- 组件入口逻辑
|   |       nav.js            -- 导航栏逻辑
|   |       seasons.js        -- 季节组件逻辑
|   +---dom                   -- DOM操作相关JS
|   |       blogDom.js        -- 博客页面DOM操作
|   |       headerDom.js      -- 页头DOM操作
|   |       index.js          -- 首页DOM操作
|   +---load-components       -- 加载组件逻辑
|   |       index.js          -- 组件加载入口
|   +---pages                 -- 页面逻辑JS
|   |       blog.js           -- 博客页面逻辑
|   \---utils                 -- 工具函数
|           api.js            -- 接口请求封装
|           date.js           -- 日期处理工具
|           findDom.js        -- DOM查询工具
|           loadComponent.js  -- 组件加载工具
|           request.js        -- 通用请求函数
|   
+---pages                     -- 存放HTML页面
|       blog.html             -- 博客页面
|   
\---sass                      -- Sass源码文件
    +---abstracts            -- 抽象类（变量、函数、混入等）
    +---base                 -- 基础样式（reset、typography等）
    +---components           -- 组件样式
    +---layout               -- 布局相关样式
    +---pages                -- 页面样式
    \---themes               -- 主题样式
```

---

## 路由配置系统

路由配置系统负责根据当前页面路径，自动加载对应的组件；它解决了多页面应用中"不同页面需要加载不同组件"的问题。

### 流程

```
页面路径 → 查找路由配置 → 加载对应组件 → 执行初始化函数
```

**功能**：

- 统一管理所有页面的组件配置
- 自动识别当前页面并加载对应组件
- 支持公共组件和页面特定组件的分离
- 可动态扩展新路由

------

### 配置结构

#### 1. 公共组件配置

所有页面都需要的组件（如导航栏、页脚等）：

```javascript
const commonComponents = [
  {
    container: headerNavContainer,  // 插入容器
    name: "nav",                   // 组件文件名
    initFuc: initNav,             // 初始化函数
  },
];
```

#### 2. 页面特定组件配置

每个页面独有的组件：

```javascript
const pageSpecificComponents = {
  // 首页
  "/": [
    {
      container: headerSeasonsContainer,
      name: "seasons",
      initFuc: initSeasons,
    },
  ],

  // 博客页面
  "/pages/blog.html": [
    {
      container: blogWrapper,
      name: "blog-article",
      initFuc: initBlog,
    },
  ],
  
  // 其他页面...
};
```

------

### 核心功能

#### 1. 自动路由识别

系统会自动识别当前页面路径并加载对应组件：

```javascript
export function loadPageComponents() {
  const currentPath = getCurrentPath();           // 获取当前路径
  let componentConfigs = getComponentsForPath(currentPath);  // 获取配置
  
  // 加载组件
  loadComponents(componentConfigs);
}
```

#### 2. 路径标准化

处理不同路径格式，确保匹配准确：

```javascript
function getCurrentPath() {
  let path = window.location.pathname;

  // 统一处理根路径
  if (path === "/" || path === "/index.html") {
    return "/";
  }

  return path;
}
```

#### 3. 组件配置合并

自动合并公共组件和页面特定组件：

```javascript
function getComponentsForPath(path) {
  const specific = pageSpecificComponents[path] || [];
  return [...commonComponents, ...specific];  // 公共组件 + 特定组件
}
```

#### 4. 容器有效性检查

过滤掉容器不存在的组件配置，避免加载失败：

```javascript
const validConfigs = componentConfigs.filter((config) => {
  if (!config.container) {
    console.warn(`Container not found for component: ${config.name}`);
    return false;
  }
  return true;
});
```

------

### 使用示例

#### 完整示例：添加新页面路由

假设我们要为 `/pages/about.html` 页面添加组件配置：

**第一步**：准备 DOM 容器

```javascript
// dom/aboutDom.js
export const aboutContainer = document.querySelector(".about-container");
```

**第二步**：编写初始化函数

```javascript
// pages/about.js
export function initAbout(container) {
  const title = container.querySelector(".about__title");
  if (title) {
    title.textContent = "关于我们";
  }
}
```

**第三步**：在路由配置中添加页面

```javascript
// router.js
import { aboutContainer } from "./dom/aboutDom.js";
import { initAbout } from "./pages/about.js";

const pageSpecificComponents = {
  // ...其他配置
  
  "/pages/about.html": [
    {
      container: aboutContainer,
      name: "about",
      initFuc: initAbout,
    },
  ],
};
```

**第四步**：在入口文件中调用

```javascript
// main.js
import { loadPageComponents } from "./router.js";

window.addEventListener("DOMContentLoaded", () => {
  loadPageComponents();  // 自动识别当前页面并加载组件
});
```

------

### 高级功能

#### 1. 动态添加路由

运行时动态注册新路由：

```javascript
import { addRoute } from "./router.js";

// 动态添加新路由
addRoute("/pages/contact.html", [
  {
    container: contactContainer,
    name: "contact-form",
    initFuc: initContactForm,
  },
]);
```

#### 2. 获取路由配置

查询某个路径的完整组件配置：

```javascript
import { getRouteConfig } from "./router.js";

const config = getRouteConfig("/pages/blog.html");
console.log(config);  // 输出该页面的所有组件配置
```

------

### 工作流程图

```
┌─────────────────────────────────────────────────┐
│  页面加载 (DOMContentLoaded)                     │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  loadPageComponents()                           │
│  - 获取当前路径                                  │
│  - 查找路由配置                                  │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  getComponentsForPath(path)                     │
│  - 合并公共组件                                  │
│  - 添加页面特定组件                              │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  容器有效性检查                                  │
│  - 过滤无效配置                                  │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  loadComponents(configs)                        │
│  - 异步加载组件 HTML                             │
│  - 插入到指定容器                                │
│  - 执行初始化函数                                │
└─────────────────────────────────────────────────┘
```

------

### 最佳实践

#### 1. 组件配置对象的结构

```javascript
{
  container: HTMLElement,    // 必需：组件插入的容器
  name: string,             // 必需：组件 HTML 文件名（不含 .html 后缀）
  initFuc: Function,        // 可选：初始化函数
}
```

**关于 `name` 属性的说明**：

- `name` 只需要填写组件 HTML 文件的文件名（不含 `.html` 后缀）
- 所有<u>组件 HTML 文件</u>统一存放在 `../../components/` 目录下
- 系统会自动拼接完整路径：`BASE_PATH + name + ".html"`

**示例**：

```javascript
// ✅ 正确：只传文件名
{
  container: headerNavContainer,
  name: "nav",              // 实际加载: ../../components/nav.html
  initFuc: initNav,
}

// ✅ 正确：文件名可以包含连字符
{
  container: blogWrapper,
  name: "blog-article",     // 实际加载: ../../components/blog-article.html
  initFuc: initBlog,
}

// ❌ 错误：不要包含路径或后缀
{
  name: "../../components/nav.html",  // ❌ 多余
  name: "nav.html",                   // ❌ 不需要后缀
}
```

#### 2. 路径命名规范

- 使用完整路径（如 `/pages/blog.html`）
- 根路径统一使用 `"/"`
- 避免使用相对路径

#### 3. 容器元素要求

- 容器必须是静态 DOM 元素（页面加载时就存在）
- 在组件加载前，容器已经被正确获取
- 使用语义化的类名（如 `.header__nav-container`）

#### 4. 初始化函数设计

```javascript
function initComponent(container) {
  // ✅ 推荐：使用 container 作为查找范围
  const element = container.querySelector(".component__element");
  
  // ❌ 不推荐：直接使用 document 查找
  // const element = document.querySelector(".component__element");
  
  if (element) {
    // 执行初始化逻辑
  }
}
```

#### 5. 错误处理

```javascript
export function loadPageComponents() {
  const currentPath = getCurrentPath();
  let componentConfigs = getComponentsForPath(currentPath);

  // 如果没有精确匹配，使用默认配置
  if (!pageSpecificComponents[currentPath] && currentPath !== "/") {
    console.warn(
      `No specific configuration for path: ${currentPath}, using common components only`
    );
    componentConfigs = commonComponents;
  }

  // 过滤无效配置
  const validConfigs = componentConfigs.filter((config) => {
    if (!config.container) {
      console.warn(`Container not found for component: ${config.name}`);
      return false;
    }
    return true;
  });

  if (validConfigs.length > 0) {
    loadComponents(validConfigs);
  }
}
```

------

### 与 loadComponents 的协作

路由系统负责**配置管理**，`loadComponents` 负责**实际加载**：

```javascript
// router.js - 配置层
const componentConfigs = getComponentsForPath("/pages/blog.html");
// 返回：[{ container, name, initFuc }, ...]

// loadComponents.js - 执行层
loadComponents(componentConfigs);
// 1. 加载 HTML 文件
// 2. 插入到容器
// 3. 执行初始化函数
```

这种分离使得：

- 路由配置更清晰
- 组件加载逻辑可复用
- 易于测试和维护

------

### 调试技巧

#### 1. 查看当前路径

```javascript
console.log("当前路径:", getCurrentPath());
```

#### 2. 查看加载的组件配置

```javascript
const configs = getComponentsForPath(getCurrentPath());
console.log("组件配置:", configs);
```

#### 3. 检查容器是否存在

```javascript
const validConfigs = componentConfigs.filter((config) => {
  console.log(`组件 ${config.name} 的容器:`, config.container);
  return !!config.container;
});
```

------

### 常见问题

**Q: 为什么我的组件没有加载？**

A: 检查以下几点：

1. 路径是否匹配（注意大小写和斜杠）
2. 容器元素是否存在（在组件加载前）
3. 组件配置是否正确添加到 `pageSpecificComponents`

**Q: 如何在所有页面都显示某个组件？**

A: 将组件配置添加到 `commonComponents` 数组中

**Q: 初始化函数什么时候执行？**

A: 在组件 HTML 插入到容器之后立即执行

**Q: 可以动态修改路由配置吗？**

A: 可以，使用 `addRoute()` 函数动态添加新路由

---

## 动态加载组件

### 流程

从 `components/` 文件夹中加载目标组件，挂载到指定位置后，执行 `initFunc` 函数进行初始化

在 `router.js` 文件夹统一管理组件加载：

```js
"/": [
    {
        container: headerSeasonsContainer,
        name: "seasons",
        initFuc: initSeasons,
    },
]
```

在 `/` 主页将组件：`seasons.html` 加载到指定位置：`headerSeasonsContainer`，然后执行初始化函数：`initSeasons`

### 核心原则

> ⚠️ **关键点**：组件的 DOM 元素只有在加载完成后才能被获取和操作——异步

### DOM 元素获取策略

#### 1. 两种获取场景

##### 场景 A：静态 DOM 元素

**特点**：元素在页面初始加载时就已存在于 HTML 中

**获取方式**：可以直接使用标准 DOM API 获取

```js
// 直接获取页面中已存在的元素
const headerNavContainer = document.querySelector(".header__nav-container");
```

**适用情况**：

- 写在 HTML 中的静态元素
- 不需要动态插入的页面结构

---

##### 场景 B：动态加载的组件

**特点**：组件的 HTML 是通过 JavaScript 动态插入的

**问题**：如果在组件加载前尝试获取其 DOM，会得到 `null`

```js
// ❌ 错误示例：组件还未加载
const seasonDate = document.querySelector(".seasons__date"); 
// 结果：null
```

**原因分析**：

- 页面初始加载时，动态组件的 DOM 结构尚不存在
- 必须等待组件插入完成后才能访问其内部元素

**解决方案**：

1. **预留插入位置**：在 HTML 中提前准备好静态容器元素
2. **等待加载完成**：组件插入后，通过容器元素查找组件内部的 DOM

```js
// ✅ 正确示例：在组件加载后通过容器获取
function initSeasonsDate(container) {
  // container 是已经插入了组件的容器元素
  const el = container.querySelector(".seasons__date");
  
  if (el) {
    el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  } else {
    console.warn("未找到 .seasons__date 元素");
  }
}
```

---

### 完整示例：动态加载 `seasons` 组件

#### 第一步：准备插入容器

在 HTML 中预留组件的插入位置（静态容器）：

```html
<!-- index.html -->
<header class="header">
  <div class="header__nav-container"></div>
  <div class="header__seasons-container"></div>	<!-- 为 seasons 组件预留的插入位置 -->
</header>
```

在 JavaScript 中获取这个容器：

```js
// headerDom.js
// 这个容器是静态的，可以直接获取
const headerSeasonsContainer = document.querySelector(".header__seasons-container");
```

------

#### 第二步：配置组件加载

在页面加载完成后，配置需要动态加载的组件：

```js
// load-components/index.js
window.addEventListener("DOMContentLoaded", () => {
  loadComponents([
    {
      container: headerSeasonsContainer,    // 插入位置
      name: "seasons",                     // 组件文件名
      initFuc: initSeasonsDate,           // 初始化函数
    },
  ]);
});
```

------

#### 第三步：编写初始化函数

定义组件加载后的初始化逻辑：

```js
// components/season.js
function initSeasonsDate(container) {
  // 此时组件已插入，可以安全地查找内部元素
  const el = container.querySelector(".seasons__date");
  
  if (el) {
    // 更新日期显示
    el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  } else {
    console.warn("未找到 .seasons__date 元素");
  }
}
```

**要点说明**：

- `container` 参数是已经插入了组件的容器元素
- 使用 `container.querySelector()` 而不是 `document.querySelector()`，范围更精确

------

#### 第四步：实现组件加载器

创建统一的组件加载和初始化机制：

```js
// load-components/index.js
const loadComponents = async (components = []) => {
  for (const { container, name, initFuc } of components) {
    // 1. 加载组件 HTML 并插入到 container 中
    // await loadComponentHTML(container, name);
    
    // 2. 组件插入完成后，执行初始化函数
    initFuc(container);
    // ⚠️ 关键：此时 container 内已经有了组件的 DOM 结构
    
    // 3. 其他后续处理...
  }
};
```

---

### 总结

| 情况               | DOM 获取方式                   | 说明                                               |
| ------------------ | ------------------------------ | -------------------------------------------------- |
| 静态 DOM           | `document.querySelector(...)`  | 页面加载时就存在                                   |
| 动态插入组件的 DOM | `container.querySelector(...)` | 等组件插入后再获取，`container` 是插入位置的父元素 |

**流程**：

> “插入位置的容器 DOM 一定存在 → 动态插入组件 → 在容器内获取组件 DOM → 初始化操作”

---



---

## SCSS 配置

Sass 是一个 CSS 预处理器，而 SCSS 是 Sass 的主流语法（完全兼容 CSS 写法）

### 安装

在项目根目录执行以下命令安装 Sass：

```bash
npm i sass
```

------

### 配置 `package.json`

在 `package.json` 的 `scripts` 文件中添加以下命令：

```json
"scripts": {
  "compile:sass": "sass ./sass/main.scss ./css/style.css -w",
  "prefix:css": "postcss ./css/style.css --use autoprefixer -o ./css/style.prefix.css",
  "build:css": "npm run compile:sass && npm run prefix:css"
}
```

- `compile:sass`：编译 SCSS 为 CSS，并监听文件变化，有更改时自动编译
- `prefix:css`：使用 `autoprefixer` 自动补全兼容性前缀
- `build:css`：一键构建样式文件（编译 + 前缀处理）

### 运行

```bash
npm run compile:sass
```

------

### SCSS 目录结构

```csharp
scss/
│
├─ abstracts/      # 抽象工具（变量、函数、mixin 等）
├─ base/           # 全局基础样式（reset、全局样式等）
├─ components/     # 组件样式（按钮、卡片、导航等）
├─ layout/         # 布局（头部、底部、栅格、侧边栏等）
├─ pages/          # 页面级样式
├─ themes/         # 主题（深色 / 浅色等主题切换）
│
└─ main.scss       # 主入口文件（import以上所有）
```

> `components` 中的文件为 **可复用的组件**

------

### 模块导入

在每个文件夹中都创建一个 `_index.scss` 文件，用于统一导出该文件夹内的所有样式模块：

```scss
// _index.scss
// 导出：@forward "模块名"
@forward "./base";
```

在主入口 `main.scss` 中通过 `@use` 导入：

```scss
@use "base";
```

### 各文件夹详细说明

#### 1. `abstracts/` —— 抽象工具 

> ❝ 不产生任何 CSS，只为全局提供工具和变量支持 ❞

存放项目的**基础构建块**：

- `_variables.scss`：颜色、字体、间距、z-index 等全局变量
- `_mixins.scss`：复用的 mixin（例如响应式断点、文本省略等）
- `_functions.scss`：自定义函数

#### 2. `base/` —— 基础样式 

> ❝ 项目的全局默认样式层 ❞

这里存放**不会随组件而改变的全局样式**：

- `_global.scss`：全局 HTML、body、a、img 等基础样式
- `_utilities.scss`：一些通用的帮助类（如 `.hidden`、`.clearfix`）
- `_animations.scss`：常用的动画，(如：`.fade-in`)

### 使用其它 SCSS 文件夹中的内容

在 SCSS 中，如果你想使用其他文件夹（例如 `abstracts/` 中定义的变量、mixin 或函数等），可以通过 `@use` 语法进行导入，并使用别名访问其内容。

例如，我们在 `abstracts/_variables.scss` 中定义了一个变量：

```scss
// abstracts/_variables.scss
$primary-color: #3498db;
```

在其它文件中使用：

```scss
// components/_button.scss
@use "../abstracts" as a;

.button {
  background-color: a.$primary-color; // 使用 abstracts 中定义的变量
}
```

---

## 组件

以下为一个导航栏组件的封装示例（React JSX）：

```jsx
// Navbar.jsx
function Navbar({ links }) {
  return (
    <nav className="navbar">
      <ul>
        {links.map(link => (
          <li key={link.url}>
            <a href={link.url}>{link.label}</a>
          </li>
        ))}
      </ul>
    </nav>
  );
}
```

组件使用示例：

```jsx
<Navbar 
  links={[
    { url: '/', label: '首页' },
    { url: '/about', label: '关于' },
  ]} 
/>
```
