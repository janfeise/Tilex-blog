/**
 * @fileoverview 该文件负责封装全局的 HTTP 请求工具，用于与后端 API 通信。
 *
 * 实现思路：
 * 1. 定义统一的请求函数 request()，支持 GET / POST / PUT / DELETE 等常用方法
 * 2. 自动拼接基础 URL，并支持查询参数（params）和请求体（data）的处理
 * 3. 在内部统一处理请求错误和响应数据解析，简化调用方的逻辑
 * 4. 所有 API 请求均通过该模块发起，保证请求逻辑可维护、可扩展
 */

const BASE_URL = "https://api.example.com"; // 后端接口基础地址

/**
 * 封装 fetch 请求
 * @param {string} url - 请求路径（相对 BASE_URL）
 * @param {string} method - 请求方法 (GET, POST, PUT, DELETE...)
 * @param {object} [data] - 请求体参数（POST/PUT）
 * @param {object} [params] - 查询参数（GET）
 * @returns {Promise<any>} - 返回解析后的响应数据
 */
export async function request(url, method = "GET", data = null, params = null) {
  let fullUrl = BASE_URL + url;

  // 拼接查询参数
  if (params) {
    const query = new URLSearchParams(params).toString();
    fullUrl += `?${query}`;
  }

  const options = {
    method,
    headers: {
      "Content-Type": "application/json",
    },
  };

  if (data) {
    options.body = JSON.stringify(data);
  }

  try {
    const response = await fetch(fullUrl, options);

    if (!response.ok) {
      throw new Error(`HTTP 错误！状态码: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("请求失败：", error);
    throw error;
  }
}
