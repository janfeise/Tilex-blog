<!-- 首页 -->
<script setup>
import homeLayout from "@/Layout/homeLayout.vue";
import BlogCard from "./components/BlogCard.vue";
import SearchDialog from "./components/SearchDialog.vue";
import { Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getArticles } from "@/api/blog";
import { ref, onMounted, onUnmounted } from "vue";
import { useArticleListStore } from "@/stores/articleList";

// 文章列表状态管理
const articleListStore = useArticleListStore();

// 模态框是否显示
const dialogVisible = ref(false);

const loadArticles = async () => {
  try {
    const res = await getArticles();
    if (res.status === 200) {
      articleListStore.setArticleList(res.data.records || []);
    } else {
      ElMessage.error(res.message || "获取文章列表失败");
    }
  } catch (error) {
    ElMessage.error("获取文章列表失败，请检查网络连接");
  }
};

// 加载文章列表
onMounted(() => {
  loadArticles();
});

// 按键监听
const handler = (e) => {
  if (e.key === "/") {
    dialogVisible.value = !dialogVisible.value;
  }
};

// 添加按键监听
onMounted(() => {
  document.addEventListener("keydown", handler);
});

// 移除按键监听
onUnmounted(() => {
  document.removeEventListener("keydown", handler);
});
</script>

<template>
  <homeLayout>
    <template #main>
      <div class="blog-list">
        <div v-for="article in articleListStore.articleList" :key="article.id">
          <BlogCard :article="article" />
        </div>
      </div>
    </template>
  </homeLayout>

  <div class="search-btn" @click="dialogVisible = true">
    <Search />
  </div>

  <!-- 搜索对话框组件 -->
  <SearchDialog
    :visible="dialogVisible"
    @update:visible="dialogVisible = $event"
  />
</template>

<style lang="scss" scoped>
.search-btn {
  opacity: 0;
  animation: slide-up 0.2s ease-out 0.5s forwards;
}

// 特质搜索动画
@keyframes slide-up {
  0% {
    right: 0;
    opacity: 0;
  }
  100% {
    right: 1rem;
    opacity: 1;
  }
}
</style>
