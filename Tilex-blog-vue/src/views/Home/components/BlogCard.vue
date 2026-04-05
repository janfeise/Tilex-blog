<!-- 博客卡片组件 -->
<script setup>
import { defineProps } from "vue";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";
import { trimMarkdown } from "@/utils/markdown";

const props = defineProps({
  article: {
    type: Object,
    required: true,
  },
});

const getPreviewContent = (content, limit = 400) => {
  return trimMarkdown(content, limit);
};
</script>

<template>
  <div class="card">
    <div class="card__tips">
      <h2 class="card__title">{{ props.article.title }}</h2>
      <!-- 时间 -->
      <span class="card__date">{{ props.article.createdAt }}</span>
    </div>

    <div class="card__content">
      <MarkdownRenderer
        :content="getPreviewContent(props.article.content, 400)"
        :showTOC="false"
        :showReadingTime="true"
        :wordCount="props.article.wordCount || props.article.content.length"
        :renderOptions="{
          includeTOC: false,
          includeAnchors: false,
          includeCopyButton: true,
          sanitize: true,
        }"
      />
    </div>

    <div class="card__footer">
      <div class="card__tags">
        <span class="card__tag"> vue </span>
        <span class="card__tag"> javascript </span>
      </div>

      <button
        class="blog__goto btn btn__read-more"
        @click="$router.push(`/article/${props.article.id}`)"
      >
        全文阅读 >>
      </button>
    </div>
  </div>
</template>
