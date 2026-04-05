<!-- 画廊列表 -->
<script setup>
import { ref, onMounted } from "vue";
import { getGalleries, disableGallery, enableGallery } from "../api/gallery.js";
import { ElMessage } from "element-plus";

const galleries = ref([]);

onMounted(() => {
  getGalleries().then((res) => {
    if (res.code === 200) {
      galleries.value = res.data.images;
      console.log("画廊列表：", galleries.value);
    } else {
      ElMessage.error("获取画廊列表失败");
    }
  });
});

// 禁用图片
const disableImage = (id) => {
  disableGallery(id).then((res) => {
    if (res.code === 200) {
      ElMessage.success("图片已禁用");
      // 刷新画廊列表
      getGalleries().then((res) => {
        if (res.code === 200) {
          galleries.value = res.data.images;
        }
      });
    } else {
      ElMessage.error("禁用图片失败");
    }
  });
};

const enableImage = (id) => {
  enableGallery(id).then((res) => {
    if (res.code === 200) {
      ElMessage.success("图片已启用");
      // 刷新画廊列表
      getGalleries().then((res) => {
        if (res.code === 200) {
          galleries.value = res.data.images;
        }
      });
    } else {
      ElMessage.error("启用图片失败");
    }
  });
};
</script>

<template>
  <div class="gallery-list">
    <div class="gallery-list__title">画廊列表</div>

    <div class="gallery-list__container">
      <div v-for="(item, index) in galleries" :key="item.id">
        <div class="gallery-list__item" :class="{ disable: item.isDeleted }">
          <div class="gallery-list__item-image">
            <img :src="item.imageUrl" :alt="item.title || '图片'" />
          </div>
          <div class="gallery-list__item-tip">
            <div class="gallery-list__item-title">
              {{ item.title || "暂无标题" }}
            </div>
            <div class="gallery-list__item-description">
              {{ item.description || "暂无描述" }}
            </div>
            <div class="gallery-list__item-tags">
              <el-tag type="primary">Tag 1</el-tag>
              <el-tag type="success">Tag 2</el-tag>
              <el-tag type="info">Tag 3</el-tag>
            </div>
          </div>
          <div class="hr"></div>
          <div class="gallery-list__item-operation">
            <el-button type="primary" text>编辑</el-button>
            <el-button
              type="danger"
              text
              @click="disableImage(item.id)"
              v-if="item.isDeleted === 0"
              >禁用</el-button
            >
            <el-button
              type="success"
              text
              @click="enableImage(item.id)"
              v-if="item.isDeleted === 1"
              >启用</el-button
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.gallery-list {
  &__title {
    padding: 1rem 1.5rem;
    font-size: 1.5rem;
    font-weight: bold;
    box-shadow: rgba(0, 0, 0, 0.06) 0px 1px 2px 0px;
  }

  &__container {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 2rem;
    min-height: 100vh;
    background-color: #f0f0f0;
    padding: 2rem;
    padding-bottom: 10rem;
  }

  &__item {
    height: 25rem;
    border-radius: 8px;
    background-color: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &-image {
      width: 100%;
      height: 12rem;
      background-color: #ccc;
      border-radius: 4px 4px 0 0;

      & > img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 4px 4px 0 0;
      }
    }

    &-tip {
      font-size: 16px;
      padding: 0.9rem;
      height: 9rem;
    }

    &-title {
      font-weight: 700;
      margin-bottom: 2px;
      height: 3rem;
    }

    &-description {
      font-size: 14px;
      color: #777;
      margin-bottom: 0.5rem;
      height: 2rem;
    }

    &-tags {
      text-align: left;
      gap: 0.5rem;

      & > .el-tag:not(:last-child) {
        margin-right: 0.5rem;
      }
    }

    &-operation {
      padding: 0.9rem;
      text-align: right;

      & > .el-button {
        padding: 0;

        &:focus {
          outline: none;
        }
      }
    }
  }
}

.hr {
  margin: 0 0.9rem;
  border: none;
  border-top: 1px solid #eee;
}

.disable {
  opacity: 0.4;

  &:hover {
    opacity: 0.7;
  }
}
</style>
