<!-- 搜索对话框组件 -->
<script setup>
import { ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import { searchArticles } from "@/api/blog";
import { useRouter } from "vue-router";

const router = useRouter();

// Props 和 Emits
const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(["update:visible"]);

// 搜索状态
const searchKeyword = ref("");
const searchResults = ref([]);
const isLoading = ref(false);
const hasSearched = ref(false);

// 搜索输入框 ref
const inputRef = ref(null);

// 防抖定时器
let debounceTimer = null;

/**
 * 执行搜索
 */
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning("请输入搜索关键词");
    return;
  }

  isLoading.value = true;
  hasSearched.value = true;

  try {
    const res = await searchArticles({ keyword: searchKeyword.value });

    if (res.status === 200) {
      searchResults.value = res.data || [];
      if (searchResults.value.length === 0) {
        ElMessage.info("没有找到相关文章");
      }
    } else {
      ElMessage.error(res.message || "搜索失败");
    }
  } catch (error) {
    ElMessage.error("搜索失败，请检查网络连接");
    console.error("搜索错误:", error);
  } finally {
    isLoading.value = false;
    // 一直聚焦在搜索框
    focusSearchInput();
  }
};

/**
 * 防抖搜索
 */
const handleSearch = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    performSearch();
  }, 300);
};

/**
 * 清空搜索
 */
const handleClear = () => {
  searchKeyword.value = "";
  searchResults.value = [];
  hasSearched.value = false;
};

/**
 * 处理键盘按键
 */
const handleKeydown = (e) => {
  if (e.key === "Enter") {
    performSearch();
  }
};

/**
 * 点击搜索结果，跳转到文章页面
 */
const handleResultClick = (articleId) => {
  closeDialog();
  router.push(`/article/${articleId}`);
};

/**
 * 关闭对话框
 */
const closeDialog = () => {
  emit("update:visible", false);
};

/**
 * 聚焦搜索输入框
 */
const focusSearchInput = async () => {
  await nextTick();
  try {
    // 方案1: 调用 el-input 组件的 focus 方法
    if (inputRef.value?.focus) {
      inputRef.value.focus();
    }
  } catch (error) {
    console.error("聚焦失败:", error);
  }
};

/**
 * 监听关键词变化
 */
watch(searchKeyword, () => {
  if (searchKeyword.value) {
    handleSearch();
  } else {
    searchResults.value = [];
    hasSearched.value = false;
  }
});

/**
 * 监听对话框显示/隐藏，自动聚焦搜索框
 */
watch(
  () => props.visible,
  async (visible) => {
    if (visible) {
      // 对话框显示时，自动聚焦输入框
      await focusSearchInput();
    }
  },
);
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    @update:model-value="emit('update:visible', $event)"
    @opened="focusSearchInput"
    width="600"
    class="search-dialog"
    :close-on-click-modal="true"
  >
    <!-- 搜索标题 -->
    <template #header>
      <div class="search-header">
        <h3 class="search-title">搜索文章</h3>
      </div>
    </template>

    <!-- 搜索框 -->
    <div class="search-input-wrapper">
      <el-input
        ref="inputRef"
        v-model="searchKeyword"
        placeholder="输入关键词以搜索"
        :prefix-icon="Search"
        clearable
        :disabled="isLoading"
        @keydown="handleKeydown"
        class="search-input"
      />
      <div class="search-actions">
        <button
          class="btn btn--primary"
          @click="performSearch"
          :disabled="isLoading || !searchKeyword.trim()"
        >
          {{ isLoading ? "搜索中..." : "搜索" }}
        </button>
        <button
          class="btn btn--secondary"
          @click="handleClear"
          :disabled="!searchKeyword && !searchResults.length"
        >
          清空
        </button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-content">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="search-loading">
        <p>搜索中...</p>
      </div>

      <!-- 未搜索状态 -->
      <div v-else-if="!hasSearched" class="search-empty-state">
        <p class="empty-text">输入关键词开始搜索</p>
      </div>

      <!-- 无结果状态 -->
      <div v-else-if="searchResults.length === 0" class="search-empty-state">
        <p class="empty-text">没有找到相关文章</p>
        <p class="empty-hint">试试其他关键词</p>
      </div>

      <!-- 搜索结果列表 -->
      <div v-else class="search-results">
        <div
          v-for="article in searchResults"
          :key="article.id"
          class="search-result-item"
          @click="handleResultClick(article.id)"
        >
          <!-- 标题（支持高亮） -->
          <h4 class="result-title" v-html="article.title"></h4>

          <!-- 元数据 -->
          <div class="result-meta">
            <span class="meta-date">{{ article.createdAt }}</span>
            <span v-if="article.author" class="meta-author"
              >作者：{{ article.author }}</span
            >
          </div>

          <!-- 搜索结果片段（支持高亮） -->
          <div
            v-if="article.snippets && article.snippets.length"
            class="result-snippets"
          >
            <p
              v-for="(snippet, index) in article.snippets.slice(0, 3)"
              :key="index"
              class="snippet-item"
              v-html="snippet"
            ></p>
          </div>
          <!-- 降级到摘要或内容截断 -->
          <p
            v-else
            class="result-content"
            v-html="article.summary || article.content?.substring(0, 150)"
          ></p>

          <!-- 标签 -->
          <div v-if="article.tags && article.tags.length" class="result-tags">
            <span v-for="tag in article.tags" :key="tag" class="tag">
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style lang="scss" scoped>
@use "@/styles/abstracts" as a;

.search-dialog {
  :deep(.el-dialog) {
    border-radius: 1rem;
    box-shadow: a.$shadow-lg;
  }

  :deep(.el-dialog__header) {
    border-bottom: 2px solid a.$color-primary-light;
    padding: 2rem;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__close) {
    color: a.$color-grey-dark;

    &:hover {
      color: a.$color-primary;
    }
  }
}

.search-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-title {
  margin: 0;
  font-size: a.$font-size-large;
  color: a.$color-grey-dark-1;
  font-weight: 600;
}

// 搜索输入框区域
.search-input-wrapper {
  padding: 2rem;
  border-bottom: 1px solid a.$color-grey;
  display: flex;
  gap: 1rem;
  align-items: flex-end;

  :deep(.el-input__wrapper) {
    flex: 1;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    border-radius: 0.6rem;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }

  :deep(.el-input) {
    --el-input-focus-border-color: a.$color-primary;
  }
}

.search-actions {
  display: flex;
  gap: 0.8rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 0.6rem;
  font-size: a.$font-size-body;
  cursor: pointer;
  transition: all a.$transition-fast;
  font-weight: 500;
  min-width: 7.5rem;
  white-space: nowrap;
  flex-shrink: 0;

  &--primary {
    background-color: a.$color-primary;
    color: white;

    &:hover:not(:disabled) {
      background-color: a.$color-primary-dark;
      box-shadow: a.$shadow-md;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &--secondary {
    background-color: a.$color-grey;
    color: a.$color-grey-dark-1;

    &:hover:not(:disabled) {
      background-color: #ddd;
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

// 搜索内容区域
.search-content {
  max-height: 50rem;
  overflow-y: auto;
  padding: 2rem;
}

// 加载状态
.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 150px;

  p {
    font-size: a.$font-size-body;
    color: a.$color-grey-dark;
  }
}

// 空状态
.search-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: a.$font-size-body;
  color: a.$color-grey-dark-1;
  margin: 0 0 0.5rem 0;
  font-weight: 500;
}

.empty-hint {
  font-size: a.$font-size-small;
  color: a.$color-grey-dark;
  margin: 0;
}

// 搜索结果列表
.search-results {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

// 单个搜索结果项
.search-result-item {
  padding: 1.5rem;
  border: 1px solid a.$color-grey;
  border-radius: 0.8rem;
  background-color: a.$color-white;
  cursor: pointer;
  transition: all a.$transition-fast;
  position: relative;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background-color: a.$color-primary;
    border-radius: 0.8rem 0 0 0.8rem;
  }

  &:hover {
    box-shadow: a.$shadow-md;
    border-color: a.$color-primary;
    transform: translateX(4px);
  }
}

.result-title {
  margin: 0 0 0.8rem 0;
  font-size: a.$font-size-heading;
  color: a.$color-grey-dark-1;
  font-weight: 600;
  line-height: 1.4;

  // 高亮样式继承自父元素
  :deep(.markdown-highlight) {
    background-color: #ffeb3b;
    color: #000;
    font-weight: 600;
    padding: 0.1rem 0.3rem;
    border-radius: 0.2rem;
  }
}

.result-meta {
  display: flex;
  gap: 1.2rem;
  font-size: a.$font-size-small;
  color: a.$color-grey-dark;
  margin-bottom: 1rem;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid a.$color-grey-light;
}

.meta-date {
  display: flex;
  align-items: center;

  &::before {
    content: "📅 ";
    margin-right: 0.3rem;
  }
}

.meta-author {
  display: flex;
  align-items: center;

  &::before {
    content: "✍️ ";
    margin-right: 0.3rem;
  }
}

.result-content {
  margin: 1rem 0;
  font-size: a.$font-size-body;
  color: a.$color-grey-dark;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;

  // 高亮样式
  :deep(.markdown-highlight) {
    background-color: #ffeb3b;
    color: #000;
    font-weight: 600;
    padding: 0.1rem 0.3rem;
    border-radius: 0.2rem;
  }
}

// 搜索结果片段容器
.result-snippets {
  margin: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

// 单个片段项
.snippet-item {
  margin: 0;
  font-size: a.$font-size-body;
  color: a.$color-grey-dark;
  line-height: 1.6;
  padding: 0.6rem;
  background-color: #fffbea;
  border-left: 3px solid a.$color-primary;
  border-radius: 0.4rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  // 高亮样式
  :deep(.markdown-highlight) {
    background-color: #ffeb3b;
    color: #000;
    font-weight: 600;
    padding: 0.1rem 0.3rem;
    border-radius: 0.2rem;
  }
}

.result-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.tag {
  display: inline-block;
  padding: 0.3rem 0.8rem;
  background-color: a.$color-primary-light;
  color: a.$color-primary-dark;
  border-radius: a.$border-radius-lg;
  font-size: a.$font-size-small-sm;
  font-weight: 500;
  transition: all a.$transition-fast;

  &:hover {
    background-color: a.$color-primary;
    color: white;
  }
}

// 响应式
@media (max-width: a.$breakpoint-tablet) {
  .search-dialog {
    :deep(.el-dialog) {
      width: 90vw !important;
      max-width: 500px;
    }
  }

  .search-input-wrapper {
    flex-direction: column;
    align-items: stretch;

    .search-actions {
      width: 100%;

      .btn {
        flex: 1;
      }
    }
  }

  .search-content {
    max-height: 40rem;
  }

  .result-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
