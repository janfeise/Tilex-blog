<template>
  <div class="upload-container">
    <h2>上传文章页面</h2>

    <!-- 文件选择区域 -->
    <div class="upload-section">
      <el-upload
        :before-upload="beforeUpload"
        :file-list="fileList"
        :on-remove="handleRemove"
        accept=".md"
        :show-file-list="false"
        drag
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将 Markdown 文件拖到此处，或<em>点击选择文件</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">只能上传 .md 文件</div>
        </template>
      </el-upload>
    </div>

    <!-- 文章预览区域 -->
    <div class="preview-section" v-if="articles.length > 0">
      <h3>文章预览</h3>
      <div class="article-list">
        <div
          v-for="(article, index) in articles"
          :key="index"
          class="article-item"
        >
          <div class="article-header">
            <h4>{{ article.title }}</h4>
            <span class="article-time">{{ article.createdAt }}</span>
          </div>
          <div class="article-content">
            <pre>{{ article.content }}</pre>
          </div>
          <div class="article-actions">
            <el-button
              type="primary"
              size="small"
              @click="uploadSingleArticle(article, index)"
              :loading="uploadingIndex === index"
            >
              上传此文章
            </el-button>
            <el-button type="danger" size="small" @click="removeArticle(index)">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 批量操作 -->
      <div class="batch-actions">
        <el-button
          type="success"
          @click="uploadAllArticles"
          :disabled="articles.length === 0"
          :loading="uploadingAll"
        >
          上传所有文章
        </el-button>
        <el-button
          type="info"
          @click="clearAllArticles"
          :disabled="articles.length === 0"
        >
          清空列表
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { UploadFilled } from "@element-plus/icons-vue";
import { uploadArticle } from "../utils/api.js";
import { formatDate } from "../utils/date.js";

const fileList = ref([]); // 存储选择的文件
const articles = ref([]); // 存储解析后的文章数据
const uploadingIndex = ref(-1); // 当前正在上传的文章索引
const uploadingAll = ref(false); // 是否正在批量上传

// 选择文件时调用，阻止自动上传
const beforeUpload = (file) => {
  // 检查文件类型
  if (!file.name.toLowerCase().endsWith(".md")) {
    ElMessage.error("只能上传 .md 文件");
    return false;
  }

  // 读取文件内容
  readFileContent(file);
  return false;
};

// 读取文件内容并解析
const readFileContent = (file) => {
  const reader = new FileReader();

  reader.onload = (e) => {
    const content = e.target.result;
    const title = extractTitle(content, file.name);
    const createdAt = formatDate(new Date(), "YYYY-MM-DD HH:mm:ss");

    const article = {
      title,
      content,
      createdAt,
      fileName: file.name,
      file: file,
    };

    articles.value.push(article);
    ElMessage.success(`文件 "${file.name}" 解析成功`);
  };

  reader.onerror = () => {
    ElMessage.error(`读取文件 "${file.name}" 失败`);
  };

  reader.readAsText(file, "UTF-8");
};

// 提取标题
const extractTitle = (content, fileName) => {
  const lines = content.split("\n");

  // 查找第一个以 # 开头的行作为标题
  for (const line of lines) {
    const trimmedLine = line.trim();
    if (trimmedLine.startsWith("#")) {
      // 移除 # 号和空格，返回标题
      return trimmedLine.replace(/^#+\s*/, "");
    }
  }

  // 如果没有找到标题，使用文件名（去掉扩展名）
  return fileName.replace(/\.md$/i, "");
};

// 移除文件
const handleRemove = (file, fileListArg) => {
  fileList.value = [...fileListArg];
};

// 移除文章
const removeArticle = (index) => {
  articles.value.splice(index, 1);
  ElMessage.success("文章已删除");
};

// 上传单篇文章
const uploadSingleArticle = async (article, index) => {
  uploadingIndex.value = index;

  try {
    const response = await uploadArticle({
      title: article.title,
      content: article.content,
      createdAt: article.createdAt,
    });

    ElMessage.success(`文章 "${article.title}" 上传成功`);
    articles.value.splice(index, 1);
  } catch (error) {
    console.error("上传失败:", error);
    ElMessage.error(`文章 "${article.title}" 上传失败`);
  } finally {
    uploadingIndex.value = -1;
  }
};

// 批量上传所有文章
const uploadAllArticles = async () => {
  uploadingAll.value = true;
  let successCount = 0;
  let failCount = 0;

  for (let i = articles.value.length - 1; i >= 0; i--) {
    try {
      const article = articles.value[i];
      await uploadArticle({
        title: article.title,
        content: article.content,
        createdAt: article.createdAt,
      });

      articles.value.splice(i, 1);
      successCount++;
    } catch (error) {
      console.error("上传失败:", error);
      failCount++;
    }
  }

  uploadingAll.value = false;

  if (successCount > 0) {
    ElMessage.success(`成功上传 ${successCount} 篇文章`);
  }
  if (failCount > 0) {
    ElMessage.error(`${failCount} 篇文章上传失败`);
  }
};

// 清空所有文章
const clearAllArticles = () => {
  articles.value = [];
  ElMessage.success("已清空文章列表");
};
</script>

<style scoped>
.upload-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.upload-section {
  margin-bottom: 30px;
}

.preview-section {
  margin-top: 30px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.article-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
  transition: all 0.3s ease;
}

.article-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.article-header h4 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.article-time {
  color: #909399;
  font-size: 14px;
}

.article-content {
  margin-bottom: 15px;
}

.article-content pre {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin: 0;
  font-family: "Courier New", monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.article-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.batch-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* Element Plus 上传组件样式调整 */
:deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

:deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

:deep(.el-icon--upload) {
  font-size: 67px;
  color: #c0c4cc;
  margin: 40px 0 16px;
  line-height: 50px;
}

:deep(.el-upload__text) {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

:deep(.el-upload__text em) {
  color: #409eff;
  font-style: normal;
}

:deep(.el-upload__tip) {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
  text-align: center;
}
</style>
