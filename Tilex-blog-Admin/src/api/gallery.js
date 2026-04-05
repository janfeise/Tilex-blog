import request from "../utils/request";

/**
 * 上传所有图片
 */
export function uploadGalleries(data) {
  return request({
    url: "/gallery/batch-upload",
    method: "post",
    data: data,
    timeout: 60000, // 设置超时时间为60秒
  });
}

/**
 * 获取所有图片
 */
export function getGalleries() {
  return request({
    url: "/gallery/admin/images",
    method: "get",
  });
}

/**
 * 禁用图片
 */
export function disableGallery(id) {
  return request({
    url: `/gallery/admin/images/${id}/disable`,
    method: "put",
  });
}

/**
 * 启用图片
 */
export function enableGallery(id) {
  return request({
    url: `/gallery/admin/images/${id}/enable`,
    method: "put",
  });
}
