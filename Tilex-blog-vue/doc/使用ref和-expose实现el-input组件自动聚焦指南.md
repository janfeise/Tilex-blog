# 使用 ref 与 expose 实现 el-input 自动聚焦

> input组件文档：https://element-plus.org/zh-CN/component/input#api

在基于 Vue 3 与 Element Plus 的项目中，输入框自动聚焦是一个看似简单却容易踩坑的交互细节。尤其是在搜索弹窗、表单切换、模态框打开等场景中，如果处理不当，往往会出现“看起来写了代码但就是不生效”的问题

这篇文章从实际项目中的搜索功能出发，对自动聚焦问题进行拆解，并给出一套稳定、可复用的实现方案

------

## 问题分析

往往会尝试最直觉的方式：直接调用 `focus()`。但在 Element Plus 中，这种方式经常失效

原因并不复杂：`el-input` 并不是原生的 `<input>`，而是一个组件封装，它的真实输入框被包裹在多层 DOM 结构中。因此，你拿到的 `ref` 并不是 DOM，而是组件实例

来看一下真实结构：

```html
<div class="el-input">
  <div class="el-input__wrapper">
    <input class="el-input__inner" type="text" />
  </div>
</div>
```

这意味着：

- `ref` 指向的是组件实例
- 真正可聚焦的是内部的 `input`
- 直接 `.focus()` 很可能无效

------

## 常见但无效的写法

### 模板中使用 autofocus

```vue
<el-input v-model="keyword" autofocus="true" />
```

这个方式只在页面首次加载时生效，对于动态渲染的组件完全无效。

------

### 在 onMounted 中调用 focus

```js
onMounted(() => {
  inputRef.value?.focus()
})
```

这种方式只能在组件首次挂载时触发，对于对话框反复打开的场景没有任何作用

------

## 一套稳定的自动聚焦方案

解决问题的关键在于两点：

- 等待 DOM 渲染完成
- 正确访问真实的 input 元素

可以封装一个通用函数：

```js
import { ref, nextTick } from "vue"

const inputRef = ref(null)

const focusInput = async () => {
  await nextTick()

  if (inputRef.value?.focus) {
    inputRef.value.focus()
  } else if (inputRef.value?.$el) {
    const input = inputRef.value.$el.querySelector("input")
    input?.focus()
  }
}
```

这段代码做了三件关键的事情：

- 使用 `nextTick` 确保 DOM 已完成更新
- 优先调用组件自身暴露的 `focus` 方法
- 兜底通过 `$el` 查找真实 input

这种“双重保险”的写法在复杂场景中非常稳定。

------

## 在对话框中正确触发聚焦

在弹窗场景中，触发时机同样重要。推荐优先使用 Element Plus 提供的 `@opened` 事件：

```vue
<el-dialog @opened="focusInput">
  <el-input ref="inputRef" v-model="keyword" />
</el-dialog>
```

如果需要进一步增强稳定性，可以增加监听：

```js
watch(
  () => props.visible,
  async (val) => {
    if (val) {
      await focusInput()
    }
  }
)
```

这样可以覆盖更多边界情况，例如动画延迟、状态同步问题等

------

## 自定义组件的最佳实践：expose

当你对 `el-input` 进行二次封装时，推荐主动暴露 `focus` 方法，让父组件可以直接调用

```vue
<script setup>
import { ref, defineExpose, nextTick } from "vue"

const inputRef = ref(null)

const focus = async () => {
  await nextTick()
  inputRef.value?.focus()
}

defineExpose({ focus })
</script>

<template>
  <el-input ref="inputRef" />
</template>
```

父组件调用方式：

```js
const inputRef = ref(null)

const openDialog = () => {
  dialogVisible.value = true
  inputRef.value?.focus()
}
```

这种方式的好处在于：

- 屏蔽内部实现细节
- 提高组件复用性
- 让调用方式更直观

------

## 为什么 nextTick 是关键

很多自动聚焦失败的根本原因，其实是时机问题

来看执行顺序：

```
状态变更
→ 组件更新
→ DOM 渲染
→ nextTick 回调执行
```

如果不等待 `nextTick`，那么你调用 `focus()` 时，DOM 可能还不存在

```js
// 错误
inputRef.value?.focus()

// 正确
await nextTick()
inputRef.value?.focus()
```

这是 Vue 响应式机制决定的行为，而不是组件的问题。

------

## 完整示例：当父组件打开对话框时，自动聚焦到input组件上

```vue
<script setup>
import { ref, watch, nextTick } from "vue"

const props = defineProps({
  visible: Boolean
})

const inputRef = ref(null)
const keyword = ref("")

const focusInput = async () => {
  await nextTick()

  if (inputRef.value?.focus) {
    inputRef.value.focus()
  } else {
    inputRef.value?.$el
      ?.querySelector("input")
      ?.focus()
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) focusInput()
  }
)
</script>

<template>
  <el-dialog :model-value="props.visible" @opened="focusInput">
    <el-input ref="inputRef" v-model="keyword" />
  </el-dialog>
</template>
```

这个实现已经可以覆盖绝大多数实际业务场景

------

## 实践经验总结

自动聚焦问题本质上不是 API 使用问题，而是组件封装与渲染时机共同作用的结果。理解这一点之后，解决方案就会变得清晰。

在实际开发中，可以遵循以下原则：

- 始终等待 DOM 更新完成
- 不假设组件实例就是 DOM
- 优先使用组件提供的方法，必要时手动访问内部结构
- 在组件封装时主动暴露能力，而不是让父组件“猜”

当这些原则形成习惯之后，这类问题基本不会再成为阻碍开发效率的因素
