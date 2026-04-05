# 画廊后台管理 API 文档

## 概述

本文档说明画廊系统的后台管理接口。所有后台接口返回包括**已删除的图片**，用于管理员进行全面的图片管理。

---

## API 端点

### 基础路径
```
/gallery/admin
```

---

## 接口列表

### 1. 获取所有图片（包括已删除的）

#### 端点
```
GET /gallery/admin/images
```

#### 描述
获取所有图片，**包括已删除的图片**。前台查询是过滤已删除图片，但后台需要查看所有图片便于管理员恢复。

#### 响应示例
```json
{
  "code": 200,
  "data": {
    "images": [
      {
        "id": 1,
        "title": "山水风景",
        "description": "美丽的山水风景",
        "imageUrl": "http://oss.tilex.world/d/blog-images/2026/03/20/xxx.png",
        "sortOrder": 1,
        "isDeleted": 0,
        "createdAt": "2026-03-20T15:25:31",
        "updatedAt": "2026-03-20T15:25:31"
      },
      {
        "id": 2,
        "title": "已删除的图片",
        "description": "这张图片已被删除",
        "imageUrl": "http://oss.tilex.world/d/blog-images/2026/03/19/yyy.png",
        "sortOrder": 2,
        "isDeleted": 1,  // ← 已删除标记
        "createdAt": "2026-03-19T10:00:00",
        "updatedAt": "2026-03-20T14:00:00"
      }
    ],
    "totalCount": 2
  },
  "message": "success"
}
```

#### 字段说明
- `isDeleted = 0`: 正常显示的图片（前台可见）
- `isDeleted = 1`: 已删除的图片（前台隐藏，后台可恢复）

---

### 2. 获取指定图片详情

#### 端点
```
GET /gallery/admin/images/{imageId}
```

#### 描述
获取指定 ID 的图片详情，**不考虑删除状态**。管理员可以查看任何状态的图片。

#### 参数
- `imageId` (path): 图片 ID

#### 响应示例
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "title": "已删除的图片",
    "description": "这张图片已被删除",
    "imageUrl": "http://oss.tilex.world/d/blog-images/2026/03/19/yyy.png",
    "alistPath": "/d/blog-images/2026/03/19/yyy.png",
    "alistDomain": "oss.tilex.world",
    "sourceType": "alist",
    "hasSignParam": 1,
    "sortOrder": 2,
    "isDeleted": 1,
    "createdAt": "2026-03-19T10:00:00",
    "updatedAt": "2026-03-20T14:00:00"
  },
  "message": "success"
}
```

---

### 3. 禁用图片

#### 端点
```
PUT /gallery/admin/images/{imageId}/disable
```

#### 描述
禁用指定图片，将其从前台隐藏。禁用的图片不删除，只是不显示。

#### 参数
- `imageId` (path): 图片 ID

#### 响应
```json
{
  "code": 200,
  "data": null,
  "message": "Image disabled successfully"
}
```

#### 说明
- 禁用后 `isDeleted` 字段变为 `1`
- 前台查询将**不返回**该图片
- 管理员后台仍可查看和恢复
- 可以重复禁用已禁用的图片时会返回错误

---

### 4. 启用图片

#### 端点
```
PUT /gallery/admin/images/{imageId}/enable
```

#### 描述
启用已禁用的图片，将其恢复到前台显示。

#### 参数
- `imageId` (path): 图片 ID

#### 响应
```json
{
  "code": 200,
  "data": null,
  "message": "Image enabled successfully"
}
```

#### 说明
- 启用后 `isDeleted` 字段变为 `0`
- 前台查询将**返回**该图片
- 恢复已启用的图片时会返回错误

---

### 5. 获取统计信息

#### 端点
```
GET /gallery/admin/statistics
```

#### 描述
获取图片库的统计信息，包括总数、启用数和禁用数。

#### 响应示例
```json
{
  "code": 200,
  "data": {
    "totalCount": 100,
    "activeCount": 95,
    "disabledCount": 5
  },
  "message": "success"
}
```

#### 字段说明
- `totalCount`: 所有图片总数（启用 + 禁用）
- `activeCount`: 当前启用的图片数（前台显示）
- `disabledCount`: 已禁用的图片数（前台隐藏）

---

## 前台 vs 后台接口对比

| 操作 | 前台接口 | 后台接口 | 区别 |
|------|--------|--------|------|
| 获取所有图片 | `GET /gallery/images` | `GET /gallery/admin/images` | 后台返回所有图片（包含已删除） |
| 获取单张图片 | `GET /gallery/images/{id}` | `GET /gallery/admin/images/{id}` | 后台不过滤删除状态 |
| 搜索图片 | `GET /gallery/search` | ❌ 不需要 | 搜索仅用于前台 |
| 删除图片 | `DELETE /gallery/images/{id}` | `PUT /gallery/admin/images/{id}/disable` | 后台接口语义更清晰 |
| 恢复图片 | `POST /gallery/images/{id}/restore` | `PUT /gallery/admin/images/{id}/enable` | 后台接口语义更清晰 |

---

## 业务流程说明

### 场景1：用户删除图片
```
前台删除 → DELETE /gallery/images/123 
→ 后端设置 is_deleted=1
→ 停止前台展示
→ 后台仍可查看和恢复
```

### 场景2：管理员禁用某些图片
```
后台禁用 → PUT /gallery/admin/images/456/disable
→ 后端设置 is_deleted=1
→ 停止前台展示
→ 后台仍可查看和恢复
```

### 场景3：管理员恢复已删除/禁用的图片
```
后台启用 → PUT /gallery/admin/images/789/enable
→ 后端设置 is_deleted=0
→ 前台恢复显示
```

### 场景4：后台查看统计情况
```
获取统计 → GET /gallery/admin/statistics
→ 返回总数、活跃数、禁用数
→ 帮助管理员了解图片库状态
```

---

## 错误处理

### 常见错误响应

#### 图片不存在
```json
{
  "code": 400,
  "message": "Image not found: 999"
}
```

#### 禁用已禁用的图片
```json
{
  "code": 400,
  "message": "Image is already disabled"
}
```

#### 启用已启用的图片
```json
{
  "code": 400,
  "message": "Image is already enabled"
}
```

---

## 数据库对应

| 字段 | 值 | 含义 | 前台显示 |
|------|-----|-----|--------|
| `is_deleted` | 0 | 正常/启用 | ✅ 显示 |
| `is_deleted` | 1 | 禁用/已删除 | ❌ 隐藏 |

---

## 总结

本后台管理 API 实现了：
- ✅ 查看所有图片（包括已删除）
- ✅ 禁用前台显示的图片
- ✅ 恢复已禁用的图片
- ✅ 查看图片库统计信息
- ✅ 通过 `is_deleted` 字段统一管理图片状态

这样设计消除了之前 `status` 字段与 `is_deleted` 字段的冗余，用单一字段实现了灵活的图片管理。

---

**文档版本**：v1.0  
**最后更新**：2026年3月25日
