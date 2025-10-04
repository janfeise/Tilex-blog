# 时闲的博客

> 时间：2025/10/03

## 技术栈

- 原生js
- SCSS
- Spring Boot

------

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

------

## 动态加载组件

### 核心原则

> ⚠️ **关键点**：组件的 DOM 元素只有在加载完成后才能被获取和操作

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
