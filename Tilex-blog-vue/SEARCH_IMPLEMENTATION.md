# 搜索功能实现总结

## ✅ 完成内容

### 1. **SearchDialog.vue 组件** (`src/views/Home/components/SearchDialog.vue`)

- ✅ 搜索输入框，支持 Enter 按键触发搜索
- ✅ 搜索按钮和清空按钮
- ✅ 防抖搜索（300ms 延迟）
- ✅ 加载状态提示
- ✅ 空状态处理（三级：未输入 → 搜索中 → 无结果）
- ✅ 搜索结果卡片展示：
  - 标题（支持 HTML 高亮）
  - 发布日期 + 作者
  - **搜索结果片段** (优先使用 snippets，最多显示 3 个)
  - 降级方案：摘要 (summary) 或内容截断 (150 字符)
  - 标签列表
- ✅ 关键词高亮渲染（使用 `<span class='markdown-highlight'>` ）
- ✅ 点击结果卡片跳转到文章详情页面
- ✅ 快捷键支持（`/` 为 toggle 模式）
- ✅ 响应式设计（平板/手机适配）

### 2. **SCSS 样式**

- ✅ `src/styles/components/_search.scss` - 全局高亮样式
  - `.markdown-highlight` 类：黄色背景 (#ffeb3b) + 粗体，提供高亮笔效果
  - 对话框全局覆盖样式
- ✅ SearchDialog.vue 内嵌 scoped 样式：
  - 搜索对话框整体风格（与项目主色一致）
  - 结果卡片样式（左侧竖条装饰，悬停效果）
  - 按钮样式（主色和副色）
  - 空状态提示样式（emoji + 文字）

### 3. **项目集成**

- ✅ 导入 SearchDialog 组件到 `src/views/Home/index.vue`
- ✅ 替换原有的 el-dialog 占位符
- ✅ 保持原有 `/` 快捷键 toggle 逻辑
- ✅ 在 `src/styles/components/_index.scss` 导入 `_search.scss`

### 4. **功能流程**

1. 用户按 `/` 键或点击右下搜索按钮 → 打开搜索对话框
2. 输入关键词 → 自动防抖搜索，调用 `searchArticles` API
3. 后端返回结果，展示卡片列表
4. 关键词自动高亮（黄色背景）
5. 用户点击结果 → 跳转到对应文章
6. 按 `/` 再次切换或点击外部关闭对话框

## 🔧 API 调用

接口：`searchArticles(params)` （[src/api/blog.js](src/api/blog.js#L29)）

```javascript
// 请求
searchArticles({ keyword: "搜索词" })

// 响应 (示例)
{
  status: 200,
  data: {
    records: [
      {
        id: 1,
        title: "文章标题 <span class='markdown-highlight'>搜索词</span>",
        author: "作者名",
        createdAt: "2026-04-05",
        // snippets 优先被使用 (搜索词相关的文章片段，已包含高亮标记)
        snippets: [
          "...相关内容 <span class='markdown-highlight'>搜索词</span> 相关内容...",
          "...另一个片段 <span class='markdown-highlight'>搜索词</span> 片段...",
          "...第三个片段 <span class='markdown-highlight'>搜索词</span> 片段..."
        ],
        // 降级方案 (snippets 为空时使用)
        summary: "文章摘要...",
        content: "完整内容...",
        tags: ["vue", "javascript"]
      }
    ]
  }
}
```

**数据字段说明：**

- `snippets` (可选)：搜索词相关的主文内容片段数组，已包含高亮标记 `<span class='markdown-highlight'>`
  - 前端会显示最多前 3 个片段
  - 每个片段最多显示 2 行
  - 片段左侧有蓝色边框装饰，背景色为淡黄色
- `summary` (可选)：文章摘要，当 snippets 为空时使用
- `content` (可选)：完整文章内容，当 snippets 和 summary 都为空时使用（前端截断为 150 字符）

## 📋 文件清单

| 文件                                         | 状态    | 说明                              |
| -------------------------------------------- | ------- | --------------------------------- |
| `src/views/Home/components/SearchDialog.vue` | ✅ 新建 | 搜索组件，包含完整逻辑和样式      |
| `src/styles/components/_search.scss`         | ✅ 新建 | 搜索全局样式（高亮等）            |
| `src/views/Home/index.vue`                   | ✅ 更新 | 导入 SearchDialog，替换 el-dialog |
| `src/styles/components/_index.scss`          | ✅ 更新 | 导入 \_search.scss                |

## 🎨 样式特点

- **配色方案**：遵循项目主色 (#87ceeb) 系统
- **高亮效果**：黄色背景 (#ffeb3b) + 粗体，类似高亮笔效果
- **卡片设计**：左侧 3px 蓝色竖条装饰（参考 BlogCard 风格）
- **片段容器**：
  - 多个 snippets 纵向排列，间距 0.8rem
  - 每个片段有左侧蓝色边框 + 淡黄色背景 (#fffbea)
  - 片段内容最多显示 2 行，超出部分用省略号截断
- **动画交互**：悬停卡片上升，支持 0.2s 快速过渡
- **响应式**：750px 以下切换为竖排按钮和小屏幕适配

## 🧪 测试清单

- [ ] 按 `/` 打开搜索框，再按 `/` 关闭
- [ ] 输入关键词，点击搜索按钮执行搜索
- [ ] 搜索结果正确展示（包括 snippets 片段）
- [ ] snippets 高亮效果清晰可见（黄色背景）
- [ ] 最多显示 3 个 snippets（其他被截断）
- [ ] 无 snippets 时降级到 summary 或 content
- [ ] 关键词高亮效果清晰可见（黄色背景）
- [ ] 点击搜索结果卡片能跳转到对应文章
- [ ] 清空按钮清除输入和结果
- [ ] 无搜索结果时显示空状态提示
- [ ] 按住搜索词输入时自动防抖搜索
- [ ] 小屏幕上搜索框样式适配正确

## 💡 额外说明

- 快捷键 `/` 为 **toggle 方式**（每按一次切换显示/隐藏），不是 ESC 关闭
- 高亮样式 `.markdown-highlight` 需要后端返回 HTML 结构
- 组件使用 Element Plus 的 el-dialog，支持点击外部关闭
- 防抖延迟 300ms，避免频繁搜索请求
- 搜索结果内容摘要最大 3 行显示（line-clamp）

## 🚀 后续优化方向（可选）

- [ ] 键盘上下箭头选择结果 + Enter 跳转
- [ ] 搜索建议/自动完成
- [ ] 搜索历史记录
- [ ] 按时间/热度排序搜索结果
- [ ] 搜索结果分页加载
