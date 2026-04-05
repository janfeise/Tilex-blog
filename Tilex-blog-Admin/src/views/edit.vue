<!-- 博客修改页面：对已有文章进行编辑 -->
<script setup>
import { getArticles } from "../api/articles";
import { ref, onMounted } from "vue";
import MarkdownEditor from "../components/MarkdownEditor.vue";
import { ElMessage, ElMessageBox } from "element-plus"; // 消息框

const tableData = ref([]); // 存储所有文章对应的表格数据

const dialogVisible = ref(false); // 控制 Dialog 的显示/隐藏

// 传递给子组件的数据：props
const article = ref({
  id: "",
  title: "",
  content: "",
});

// 进入页面时获取所有文章
onMounted(async () => {
  const { data } = await getArticles({ all: true });

  const sortArticles = data.records.sort((a, b) => a.id - b.id); // 排序后的文章集合

  sortArticles.forEach((article) => {
    tableData.value.push({
      id: article.id,
      title: article.title,
      updateAt: article.updatedAt,
      articleContent: article.content,
    });
  });
});

/**
 * 点击编辑按钮的事件处理函数
 * @param {number} index - 行索引
 * @param {object} row - 该行的数据对象 (包含 id, title, updateAt)
 */
const handleEdit = async (index, row) => {
  // 传递 props
  article.value.id = tableData.value[index].id;
  article.value.title = tableData.value[index].title;
  article.value.content = tableData.value[index].articleContent;

  // 打开 md-editor
  dialogVisible.value = true;
};

// 对话框获取子组件：Md-editor 的文章状态
const editorRef = ref(null); // 可以拿到子组件实例

const handleClose = (done) => {
  // 如果文章未修改，直接关闭对话框
  if (!editorRef.value?.checkChange()) {
    done(); // 关闭
    // 刷新当前页面
    window.location.reload();
    return;
  }

  // 文章已修改，弹出确认提示
  ElMessageBox.confirm("文件已修改，未保存，确认关闭？", "警告", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      // 用户确认关闭
      ElMessage({
        type: "info",
        message: "文件未保存",
      });
      done(); // 执行关闭操作
    })
    .catch(() => {
      // 用户取消关闭
      ElMessage({
        type: "success",
        message: "已取消关闭",
      });
    });
};
</script>

<template>
  <div class="edit">
    <h2 class="edit__title">编辑文章</h2>

    <!-- 2. 编辑文章 -->

    <div class="articles__table">
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column property="id" sortable label="id" width="80">
        </el-table-column>
        <el-table-column property="title" label="title"> </el-table-column>
        <el-table-column property="updateAt" sortable label="updateAt">
        </el-table-column>
        <el-table-column label="Operations">
          <template #default="scope">
            <el-button
              @click="handleEdit(scope.$index, scope.row)"
              style="width: 30%"
              size="small"
            >
              Edit
            </el-button>
            <!-- <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >
              Delete
            </el-button> -->
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :before-close="handleClose"
      fullscreen
      destroy-on-close
    >
      <MarkdownEditor ref="editorRef" :article="article" />
    </el-dialog>
  </div>
</template>

<style scoped>
.edit {
  padding: 2rem;
}

.edit__title {
  margin-bottom: 1rem;
}
</style>
