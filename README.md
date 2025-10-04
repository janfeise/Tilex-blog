# 时闲的博客

> 时间：2025/10/03

## 技术栈

- 原生js
- SCSS (Sass)：用于样式预处理和模块化管理
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

每个文件夹中都创建一个 `_index.scss` 文件，统一导出该文件夹内的所有样式模块：

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

## DOM元素获取

### 1. 获取 DOM 的逻辑

#### 情况 A：页面静态存在的 DOM

- 当某个 DOM 元素在页面加载时就已经存在（不需要动态插入）
- 获取方式：直接使用 `querySelector` 或 `getElementById` 等方法即可

```js
const headerNavContainer = document.querySelector(".header__nav-container");
```

#### 情况 B：动态插入的组件

- 当组件 HTML 是动态插入的，不能在页面初始加载时直接获取，因为此时还没有加载dom元素，如果此时获取结果为：`null`
- 原因：组件的 DOM 在初始页面加载时还不存在
- 解决方案：
	1. 确定组件的插入位置。组件必须插入到页面中已有的静态 DOM 元素上（先有插入位置，才能插入组件）。
	2. 动态加载组件后，可通过 `container.querySelector()` 获取组件内部的 DOM 元素进行操作。

```js
function initSeasonsDate(container) {
  const el = container.querySelector(".seasons__date");
  if (el) {
    el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  } else {
    console.warn(".seasons__date 元素未找到");
  }
}
```

我帮你把示例逻辑润色得更清晰、条理更顺畅，并稍微优化了注释风格，使其更易读、易理解：

------

### 2. 示例：动态插入 `seasons` 组件

#### 1. 确定插入位置

在 HTML 中为 `seasons` 组件预留一个静态 DOM 容器：

```html
<!-- index.html -->
<header class="header">
    <div class="header__nav-container"></div>
    <div class="header__seasons-container"></div> <!-- seasons 插入位置 -->
</header>
```

在 JS 中直接获取该容器：

```js
// headerDom.js
const headerSeasonsContainer = document.querySelector(
  ".header__seasons-container"
);
```

------

#### 2. 动态加载组件

监听 `DOMContentLoaded` 后加载组件：

```js
// load-components/index.js
window.addEventListener("DOMContentLoaded", () => {
  loadComponents([
    {
      container: headerSeasonsContainer,
      name: "seasons",
      initFuc: initSeasonsDate,
    },
  ]);
});
```

------

#### 3. 初始化函数

组件加载完成后，操作组件内部 DOM：

```js
// components/season.js
function initSeasonsDate(container) {
  const el = container.querySelector(".seasons__date");
  if (el) {
    el.textContent = formatDate(new Date(), "「YYYY/MM/DD」");
  } else {
    console.warn(".seasons__date 元素未找到");
  }
}
```

------

#### 4. 组件加载器

统一处理组件动态加载和初始化：

```js
const loadComponents = async (components = []) => {
  for (const { container, name, initFuc } of components) {
		// 插入组件的代码，插入后对组件进行初始化
      
        initFuc(container); // 关键点：确保组件加载完再执行初始化, container为传入的参数：headerSeasonsContainer
      
      	// 其它代码
  }
};
```

### 3. 总结

| 情况               | DOM 获取方式                   | 说明                                               |
| ------------------ | ------------------------------ | -------------------------------------------------- |
| 静态 DOM           | `document.querySelector(...)`  | 页面加载时就存在                                   |
| 动态插入组件的 DOM | `container.querySelector(...)` | 等组件插入后再获取，`container` 是插入位置的父元素 |

**核心思路**：

> “插入位置的容器 DOM 一定存在 → 动态插入组件 → 在容器内获取组件 DOM → 初始化操作”

------









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
