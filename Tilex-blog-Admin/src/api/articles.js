import request from "../utils/request";

/**
 * 上传所有文章
 */
export function uploadArticles(data) {
  return request.post("/articles/batch", data);
}

/**
 * 获取所有文章
 */
export function getArticles(params) {
  return request.get("/articles", params);
}

/**
 * 更新文章
 * @param {number} articleId - 文章ID
 * @param {Object} data - 更新的数据
 * @param {string} data.title - 文章标题
 * @param {string} data.content - 文章内容
 * @returns {Promise} - 返回Promise对象
 */
export function updateArticle(articleId, data) {
  return request.put(`/articles/${articleId}`, data);
}
