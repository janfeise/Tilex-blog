<!-- 
    文章列表组件：用于侧边栏展示所有文章列表
-->
<script setup>
import { useArticleListStore } from "@/stores/articleList";
import { useRoute } from "vue-router";
import { ref, watch, nextTick } from "vue";

// 获取文章列表数据
const articleListStore = useArticleListStore();

// 通过url获取当前所在文章的id
const activeId = ref(0);

// 文章列表容器ref
const articleListContainer = ref(null);

// 滚动到active文章到竖直中间位置
const scrollToActiveArticle = async () => {
  await nextTick();
  if (!articleListContainer.value) return;

  const activeElement = articleListContainer.value.querySelector(".active");
  if (!activeElement) return;

  const container = articleListContainer.value;
  const containerHeight = container.clientHeight;
  const elementTop = activeElement.offsetTop;
  const elementHeight = activeElement.clientHeight;

  // 计算滚动位置，使active元素位于容器中间
  const scrollPosition = elementTop - containerHeight / 2 + elementHeight / 2;
  container.scrollTo({
    top: scrollPosition,
    behavior: "smooth",
  });
};

// 监听路由变化，更新activeId
const route = useRoute();
watch(
  () => route.params.id,
  (newId) => {
    activeId.value = parseInt(newId);
    // 滚动到active文章到中间位置
    scrollToActiveArticle();
  },
  { immediate: true },
);
</script>

<template>
  <div class="article-list" ref="articleListContainer">
    <ul>
      <li
        v-for="article in articleListStore.articleList"
        :key="article.id"
        :class="{ active: article.id === activeId }"
        @click="$router.push(`/article/${article.id}`)"
      >
        {{ article.title }}
      </li>
    </ul>
  </div>
</template>
