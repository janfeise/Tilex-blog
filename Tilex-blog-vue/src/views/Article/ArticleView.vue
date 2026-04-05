<!-- 
  ArticleView.vue - 文章详情页
-->

<script setup>
import { ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useRouter } from "vue-router";
import articleLayout from "@/Layout/article/articleLayout.vue";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";
import ArticleTableOfContents from "@/components/ArticleTableOfContents.vue";
import { getArticleById } from "@/api/blog";
import { ElMessage, ElLoading } from "element-plus";
import articleList from "./components/articleList.vue";

const route = useRoute();
const router = useRouter();
const article = ref(null);
const loading = ref(true);
const tableOfContents = ref([]);

const loadArticle = async (id) => {
  loading.value = true;

  try {
    const res = await getArticleById(id);

    if (res.status === 200) {
      article.value = res.data;

      if (!route.hash) {
        window.scrollTo({
          top: 0,
          left: 0,
          behavior: "smooth",
        });
      } else {
        // 1. 先解码
        const rawHash = route.hash.substring(1);
        if (!rawHash) return;

        const targetId = decodeURIComponent(rawHash);

        // 2. 延迟执行，确保内容已渲染
        let element = document.getElementById(targetId);
        setTimeout(() => {
          element = document.getElementById(targetId);

          if (element) {
            element.scrollIntoView({ behavior: "smooth" });
          } else {
            console.error(
              `未找到 ID 为 "${targetId}" 的元素。请检查 HTML 结构。`,
            );
          }
        }, 100); // 100ms 延迟通常足以应付大多数动态渲染
        if (element) {
          element.scrollIntoView({ behavior: "smooth" });
        }
      }
    } else {
      ElMessage.error("文章加载失败");
    }
  } catch (error) {
    ElMessage.error("获取文章失败，请检查网络连接");
  } finally {
    loading.value = false;
  }
};

// 处理组件事件
const handleMarkdownReady = () => {
  console.log("Markdown 渲染完成");
};

const handleTOCChange = (toc) => {
  tableOfContents.value = toc;
};

const handleTOCClick = (id) => {
  console.log("用户点击目录项:", id);
  router.replace({ hash: `#${id}` });
};

// 当文章 ID 变化时重新加载文章；hash 仅用于定位，不触发顶部回滚
watch(
  () => route.params.id,
  (id) => {
    loadArticle(id);
  },
  { immediate: true },
);
</script>

<template>
  <articleLayout>
    <template #sidebar>
      <articleList />
    </template>

    <template #content>
      <div v-if="loading" class="loading-container">
        <p>加载中...</p>
      </div>

      <article v-else-if="article" class="article-container">
        <!-- 使用 MarkdownRenderer 组件，仅负责内容渲染 -->
        <MarkdownRenderer
          :key="article.id"
          :content="article.content"
          :title="article.title"
          :showTOC="false"
          :showReadingTime="true"
          :lazyLoad="true"
          :renderOptions="{
            includeTOC: true,
            includeAnchors: true,
            includeLineNumbers: false,
            includeCopyButton: true,
            sanitize: true,
          }"
          @ready="handleMarkdownReady"
          @toc-change="handleTOCChange"
        />

        <!-- 文章底部信息 -->
        <div class="article-footer">
          <div class="article-meta">
            <span class="meta-item">
              作者：<strong>{{ article.author || "佚名" }}</strong>
            </span>
            <span class="meta-item">
              发布：<strong>{{ formatDate(article.createdAt) }}</strong>
            </span>
            <span v-if="article.updatedAt" class="meta-item">
              更新：<strong>{{ formatDate(article.updatedAt) }}</strong>
            </span>
          </div>

          <div class="article-tags">
            <span v-for="tag in article.tags" :key="tag" class="tag">
              {{ tag }}
            </span>
          </div>
        </div>
      </article>

      <div v-else class="error-container">
        <p>文章加载失败，请返回首页</p>
      </div>
    </template>

    <template #toc>
      <!-- 文章目录独立模块 -->
      <ArticleTableOfContents
        :toc="tableOfContents"
        @click-toc="handleTOCClick"
      />
    </template>
  </articleLayout>
</template>

<script>
// 日期格式化函数
function formatDate(date) {
  return new Date(date).toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
}

export default {
  methods: {
    formatDate,
  },
};
</script>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.article-container {
  padding: 2rem 0;
  animation: fadeIn 0.3s ease-in;
}

.article-footer {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 2px solid a.$color-grey-light;
}

.article-meta {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
  color: a.$color-grey;
  font-size: 0.95rem;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    strong {
      color: a.$color-black;
    }
  }
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;

  .tag {
    display: inline-block;
    padding: 0.4rem 0.8rem;
    background-color: a.$color-bg;
    color: a.$color-primary;
    border-radius: 20px;
    font-size: 0.85rem;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background-color: a.$color-primary;
      color: white;
    }
  }
}

.loading-container,
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: a.$color-grey;
  font-size: 1.1rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: a.$breakpoint-mobile) {
  .article-container {
    padding: 1rem 0;
  }

  .article-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
