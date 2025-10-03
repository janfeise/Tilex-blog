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
