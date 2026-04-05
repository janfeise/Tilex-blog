<script setup>
/**
 * 向父组件传递文章状态：是否发生更改
 */

import { MdEditor } from "md-editor-v3";
import { ref, defineExpose, computed } from "vue";
import { ElNotification } from "element-plus"; // 通知
import { updateArticle } from "../api/articles";

/**
 *  接受父组件传递的 props
 *
 * aritcle: {
 * id,
 * title,
 * content}
 */
const props = defineProps({
  article: {
    type: Object,
    required: true,
    default: () => ({ content: "Hello Editor" }), // 建议添加默认值
  },
});

// computed 实现去掉后缀但同步赋值
const articleTitle = computed({
  get() {
    return props.article.title.replace(/\.md$/, "");
  },
  set(val) {
    props.article.title = val.replace(/\.md$/, "");
  },
});

/**
 * 检测文章内容是否发生了更改
 */
// 1. 保存原始值（刚打开编辑页面时的内容)
const originalContent = ref(props.article.content);
const originalTitle = ref(articleTitle.value);

/**
 * 2. 检测文章是否更改
 */
const checkChange = () => {
  return (
    originalContent.value !== props.article.content ||
    articleTitle.value !== originalTitle.value
  );
};

/**
 * 将文章是否更改的状态 暴露给父组件
 */
defineExpose({
  checkChange,
});

/**
 * 封装提示信息
 */
const notify = (title, message, type = "info", duration = 2000) => {
  ElNotification({ title, message, type, duration });
};

const onSave = async () => {
  // 检查是否有修改
  if (!checkChange()) {
    notify("提示", "文件未修改，无需保存", "info");
    return;
  }

  try {
    const res = await updateArticle(props.article.id, {
      title: articleTitle.value + ".md",
      content: props.article.content,
    });

    if (res?.status === 200) {
      // 保存成功后，更新 originalContent
      originalContent.value = props.article.content;
      originalTitle.value = articleTitle.value;
      notify("Success", "文件已保存", "success");
    } else {
      notify("Failed", "文件保存失败", "warning");
    }
  } catch (error) {
    console.error("保存文章出错：", error);
    notify("Error", "保存过程中出现异常", "error");
  }
};
</script>

<template>
  <div class="md-editor__container" ref="scrollContainer">
    <div class="md-editor__title">
      <h2>编辑</h2>
      <el-input v-model="articleTitle">
        <template #append>.md</template>
      </el-input>
    </div>
    <MdEditor
      v-model="article.content"
      class="custom-editor"
      @onSave="onSave"
    />
  </div>
</template>

<style scoped>
.md-editor__container {
  padding: 0 3rem;
  margin-top: -1rem;
}

.md-editor__title {
  padding-bottom: 1rem;
}

.md-editor__title h2 {
  padding-bottom: 5px;
}

.custom-editor {
  height: 85vh;
}
</style>
