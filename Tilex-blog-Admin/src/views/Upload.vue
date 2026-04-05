<!-- 上传博客文章的页面 -->
<script setup>
import { UploadFilled } from "@element-plus/icons-vue";

import { uploadArticles as apiUploadArticles } from "../api/articles";

import { ref } from "vue";
import { formatTime } from "../utils/formatTime";

// import { UploadProps, UploadUserFile } from "element-plus";

const fileList = ref([]);

const tableData = ref([]);

// 用于存储动态提示信息
const alerts = ref([]);

const addAlert = (message, type = "success") => {
  const id = Date.now();
  alerts.value.push({
    id,
    message,
    type,
  });
  // 自动删除
  setTimeout(() => {
    alerts.value = alerts.value.filter((a) => a.id !== id);
  }, 1500);
};

let uploadTimer = null; // 防抖：计时器需定义在外层
const handleChange = (_, uploadFiles) => {
  clearTimeout(uploadTimer);

  uploadTimer = setTimeout(() => {
    uploadFiles.forEach((file) => {
      // 判断是否已经上传过（通过 name 或 uid）
      const exists = fileList.value.some(
        (f) => f.uid === file.uid || f.name === file.name
      );
      if (exists) {
        addAlert(file.name + "已上传", "error");
        return;
      } else {
        // 新文件加入列表
        fileList.value.push(file);
        tableData.value.push({
          title: file.name,
          time: formatTime(new Date()),
        });
        addAlert(file.name + " 上传成功！");
      }
    });
  }, 100);
};

/**
 * 批量上传文章
 */
const uploadArticles = async () => {
  if (fileList.value.length === 0) {
    addAlert("没有可上传的文件", warning);
    return;
  }

  try {
    // 将上传的文件列表 fileList（文件对象数组）转换成接口需要的格式： { title: String, content: String }[]
    const articles = await Promise.all(
      fileList.value.map((file) => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.readAsText(file.raw); // 读取文本内容
          reader.onload = () => {
            resolve({
              title: file.name,
              content: reader.result,
            });
          };
          reader.onerror = reject;
        });
      })
    );

    const res = await apiUploadArticles(articles);

    if (res.status === 200) {
      addAlert("全部文章上传成功");
      // 情况
      fileList.value = [];
      tableData.value = [];
    } else {
      addAlert(res.message || "上传失败", "error");
    }
  } catch (e) {
    addAlert(e.message || "上传接口调用失败", "error");
  }
};

const handleDelete = function (index) {
  // 删除 fileList 对应项
  fileList.value = fileList.value.filter((_, i) => i !== index);

  // 同步删除 tableData 数据
  tableData.value = tableData.value.filter((_, i) => i !== index);
};
</script>

<template>
  <div class="upload">
    <div class="alert">
      <div style="max-width: 600px" class="alert__container">
        <el-alert
          v-for="alert in alerts"
          :key="alert.id"
          :title="alert.message"
          :type="alert.type"
          :closeable="false"
        />
      </div>
    </div>

    <h2 class="upload__title">上传</h2>
    <div class="upload__container">
      <el-upload
        class="upload-demo"
        stripe
        drag
        action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
        multiple
        @change="handleChange"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          Drop file here or <em>click to upload</em>
        </div>
      </el-upload>
    </div>

    <!-- 文章列表 -->
    <div class="articles__table" v-if="tableData.length > 0">
      <div class="upload__button">
        <el-button type="primary" @click="uploadArticles">全部上传</el-button>
      </div>
      <el-table stripe :data="tableData" style="width: 99%">
        <el-table-column type="index" label="id" width="100"> </el-table-column>
        <el-table-column property="title" label="title"> </el-table-column>
        <el-table-column property="time" label="time"> </el-table-column>
        <el-table-column label="Operations">
          <template #default="scope">
            <el-button
              size="small"
              @click="handleEdit(scope.$index, scope.row)"
            >
              Edit
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >
              Delete
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.el-alert {
  margin: 20px 0 0;
}
.el-alert:first-child {
  margin: 0;
}

.alert {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding-top: 2rem;
  padding-left: 10rem;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 100;
}

.alert__container {
  min-width: 600px;
}

.upload {
  padding: 2rem;
  padding-bottom: 10rem;
}

.upload__title {
  margin-bottom: 2rem;
}

.upload__button {
  text-align: right;
  margin-bottom: 1rem;
}

.articles__table {
  padding-top: 1rem;
}
</style>
