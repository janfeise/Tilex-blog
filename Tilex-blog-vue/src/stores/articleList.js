/**
 * 文章列表的状态管理
 */
import { defineStore } from "pinia";
import { ref } from "vue";

export const useArticleListStore = defineStore(
  "articleList",
  () => {
    const articleList = ref([]);

    function setArticleList(list) {
      articleList.value = list;
    }

    return { articleList, setArticleList };
  },
  {
    persist: true, // 启用持久化
  },
);
