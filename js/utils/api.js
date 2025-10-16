/**
 * @fileoverview 该文件用于集中管理项目中的所有 API 调用函数，提供给业务代码直接调用。
 *
 * 实现思路：
 * 1. 每个函数对应后端的一个具体接口，内部通过 request() 工具发送请求
 * 2. 将接口按功能分组封装，例如：文章管理、用户管理、认证等
 * 3. 外部调用时无需关心请求细节，只需传入必要参数即可
 * 4. 通过这种集中管理方式，方便统一修改接口路径或参数结构
 */

import { request } from "./request.js";

/**
 * 获取所有文章
 */
export function getArticles(params) {
  return request("/articles", "GET", null, params);
}

/**
 * 获取指定 ID 的文章
 */
export function getArticleById(id) {
  return request(`/articles/${id}`, "GET");
}

export function searchArticles(params) {
  return request("/articles/search", "POST", params);
}
