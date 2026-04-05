<!-- 画廊：上传图片 -->
<script setup>
import { UploadFilled } from "@element-plus/icons-vue";
import { ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { uploadGalleries } from "../api/gallery";

// 图片列表
const imageList = ref([]);

const value = ref("");

// 加载动画
const loading = ref(false);

const tags = [
  {
    tag: "Option1",
    label: "Option1",
  },
  {
    tag: "Option2",
    label: "Option2",
  },
  {
    tag: "Option3",
    label: "Option3",
  },
  {
    tag: "Option4",
    label: "Option4",
  },
  {
    tag: "Option5",
    label: "Option5",
  },
];

// 图片
const handleChange = (file) => {
  // 给每个文件生成预览地址
  file.url = URL.createObjectURL(file.raw);
  imageList.value.unshift({
    url: file.url,
    title: "",
    description: "",
    tags: [],
    raw: file.raw,
  });
};

// 删除图片
const closeImage = (index) => {
  imageList.value.splice(index, 1);
};

// 上传所有图片
const uploadAll = () => {
  loading.value = true;
  const formData = new FormData();

  imageList.value.forEach((image, index) => {
    formData.append("files", image.raw); // 文件
    formData.append("titles", image.title);
    formData.append("descriptions", image.description);

    // tags 需要转字符串
    formData.append("tags", JSON.stringify(image.tags));
  });

  uploadGalleries(formData).then((res) => {
    if (res.code === 200) {
      ElMessage({
        message: `${imageList.value.length}张图片已上传成功！`,
        type: "success",
      });
    } else {
      ElMessage.error("上传失败");
    }

    loading.value = false;
    imageList.value = [];
  });
};
</script>

<template>
  <div class="upload-images" v-loading="loading">
    <div class="upload-images__title">
      <h2>上传新图片</h2>
    </div>

    <div class="upload-images__container">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        multiple
        :auto-upload="false"
        :on-change="handleChange"
        :show-file-list="false"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div>
          <span class="el-upload__text"
            ><em>点击</em>或将多张图片拖拽到此处上传</span
          >
          <div class="el-upload__tip">
            支持 JPG、PNG、GIF、WebP 格式，且不超过 5MB
          </div>
        </div>
      </el-upload>

      <div class="upload-images__detail" v-show="imageList.length">
        <div class="upload-images__detail--tip">
          <span>待上传队列（{{ imageList.length }}）</span>
          <el-button type="primary" @click="uploadAll">开始全部上传</el-button>
        </div>

        <div
          class="upload-images__item"
          v-for="(image, index) in imageList"
          :key="index"
        >
          <div class="upload-images__item-left">
            <img :src="image.url" alt="待上传图片预览" />

            <button
              class="upload-images__item-close"
              @click="closeImage(index)"
            >
              ✕
            </button>
          </div>

          <div class="upload-images__item-right">
            <div class="upload-images__item-title">
              <span>图片标题</span>
              <el-input v-model="image.title" placeholder="图片标题" />
            </div>

            <div class="upload-images__item-tags">
              <span>关联标签</span>

              <el-select
                v-model="image.tags"
                placeholder="未分类"
                multiple
                clearable
              >
                <el-option
                  v-for="item in tags"
                  :key="item.tag"
                  :label="item.label"
                  :value="item.tag"
                />
              </el-select>
            </div>

            <div class="upload-images__item-detail">
              <span>描述信息</span>
              <el-input
                v-model="image.description"
                placeholder="可选描述"
                type="textarea"
                clearable
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.upload-images {
  background-color: #f1f1f1;

  & div {
    width: 100%;
  }

  &__title {
    padding: 1rem 1.5rem;
    background-color: white;
    box-shadow: rgba(0, 0, 0, 0.06) 0px 1px 2px 0px;
  }

  &__container {
    min-height: 100vh;
    padding: 2rem 7rem;

    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__detail {
    margin-top: 2rem;
    display: flex;
    flex-direction: column;

    &--tip {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      span {
        font-size: 1.1rem;
        font-weight: bold;
      }
    }
  }

  &__item {
    height: 12rem;
    margin-bottom: 1rem;
    padding: 2rem;
    border-radius: 10px;
    font-size: 0.9rem;
    color: #9099a4;
    background-color: white;

    display: flex;

    &-left {
      flex: 0 0 20%;
      padding-right: 1rem;
      position: relative;

      // 修正：确保容器高度充满，防止图片没撑满时按钮飘走
      display: flex;
      height: 100%;

      & img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 10px;
      }
    }

    &-right {
      display: grid;
      gap: 1rem;
      grid-template-columns: repeat(2, 1fr);

      & div {
        display: flex;
        flex-direction: column;
      }
    }

    &-close {
      position: absolute;
      top: -8px;
      right: 8px;
      width: 20px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;

      background-color: #ef4444;
      color: white;
      border: none;
      border-style: none;
      border-radius: 50%;
      font-size: 12px;
      cursor: pointer;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      transition: all 0.3s;
      z-index: 10;

      &:hover {
        background-color: #ef4455;
      }
    }

    &-title {
    }

    &-tags {
    }

    &-detail {
      grid-column: span 2;
    }
  }

  :deep(.el-upload-dragger) {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .el-upload__text {
    font-size: 1.1rem;
    color: #606266;

    em {
      color: #409eff;
    }
  }

  .el-upload__tip {
    margin-top: 0.5rem;
    font-size: 0.8rem;
    color: #9099a4;
  }
}
</style>
