# 个人博客系统设计文档

> 日期：2026-04-30
> 状态：v1 设计定稿

---

## 1. 项目概述

个人博客网站，支持文章管理、图片上传、标签筛选、用户注册与审核、管理后台等功能。前端使用 Vue 3，后端使用 Spring Boot 2.7.10 + MyBatis-Plus，数据库 MySQL。

## 2. 项目结构

```
D:\03PerProject\
├── blog-backend/               # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── BlogApplication.java
│   │   ├── config/             # 配置：CORS、文件上传、Security、MyBatis-Plus
│   │   ├── controller/         # REST 控制器（按模块分包）
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # MyBatis-Plus Mapper 接口
│   │   ├── entity/             # 数据库实体
│   │   ├── dto/                # 请求/响应 DTO
│   │   └── common/             # 全局异常处理、响应封装、常量
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── static/             # Vue 构建产物（生产部署）
│   ├── src/test/java/com/blog/ # 单元测试
│   │   ├── controller/
│   │   ├── service/
│   │   └── mapper/
│   └── pom.xml
│
├── blog-frontend/              # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 公共组件
│   │   ├── router/             # Vue Router 配置
│   │   ├── api/                # Axios API 封装
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── styles/             # 全局样式（亮色/深色主题）
│   │   └── utils/              # 工具函数
│   ├── index.html
│   └── package.json
│
└── uploads/                    # 上传图片存储目录
```

## 3. 技术栈

### 后端

| 技术 | 版本/选型 |
|------|-----------|
| JDK | 1.8 |
| Spring Boot | 2.7.10 |
| MyBatis-Plus | 3.5.x (mybatis-plus-boot-starter) |
| MySQL Connector | 最新兼容版 |
| Spring Security | 内置 |
| JWT | jjwt-api / jjwt-impl / jjwt-jackson |
| 参数校验 | hibernate-validator |
| 工具类 | hutool-all（雪花 ID、字符串工具等） |
| 构建工具 | Maven |

### 前端

| 技术 | 版本/选型 |
|------|-----------|
| Vue | 3.x |
| 构建工具 | Vite |
| UI 组件库 | Element Plus |
| 路由 | Vue Router |
| 状态管理 | Pinia |
| HTTP | Axios |
| Markdown | marked + highlight.js |
| 工具库 | @vueuse/core |

## 4. 数据库设计

### 4.1 基类字段（所有表公共）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID（主键） |
| created_by | VARCHAR(50) | 创建人 |
| created_at | DATETIME | 创建时间 |
| updated_by | VARCHAR(50) | 修改人 |
| updated_at | DATETIME | 修改时间 |
| deleted | TINYINT(1) | 逻辑删除：0-正常，1-删除 |
| version | INT | 乐观锁版本号 |

### 4.2 user — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像 URL |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | admin / user |
| status | VARCHAR(20) | pending / active / disabled |
| + 基类字段 |

### 4.3 article — 文章表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID |
| title | VARCHAR(200) | 文章标题 |
| content | LONGTEXT | Markdown 内容 |
| summary | VARCHAR(500) | 摘要 |
| cover_image | VARCHAR(255) | 封面图 URL |
| status | VARCHAR(20) | draft / published |
| view_count | INT | 阅读数 |
| + 基类字段 |

### 4.4 tag — 标签表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID |
| name | VARCHAR(50) | 标签名，唯一 |
| + 基类字段 |

### 4.5 article_tag — 文章标签关联表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID |
| article_id | BIGINT | 文章 ID |
| tag_id | BIGINT | 标签 ID |
| + 基类字段 |

### 4.6 image — 图片记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID |
| original_name | VARCHAR(255) | 原始文件名 |
| filename | VARCHAR(255) | 存储文件名（UUID 命名） |
| size | BIGINT | 文件大小（字节） |
| mime_type | VARCHAR(50) | MIME 类型 |
| url | VARCHAR(255) | 访问路径 |
| + 基类字段 |

### 4.7 实体关系

```
user (1) ──< article (N)   — 用户拥有多篇文章
article (N) >──< tag (N)    — 通过 article_tag 多对多
```

## 5. API 设计

### 5.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页响应包含 `total`、`page`、`size` 字段。

### 5.2 认证模块

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/auth/register | 公开 | 用户注册（status=pending） |
| POST | /api/auth/login | 公开 | 登录，返回 JWT |
| GET | /api/auth/me | 登录 | 获取当前用户信息 |

### 5.3 文章模块

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/articles | 公开 | 分页文章列表（支持 tag 参数筛选） |
| GET | /api/articles/{id} | 公开 | 文章详情 |
| POST | /api/articles | 用户 | 创建文章 |
| PUT | /api/articles/{id} | 本人/管理员 | 更新文章 |
| DELETE | /api/articles/{id} | 本人/管理员 | 删除文章（逻辑删除） |

### 5.4 标签模块

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/tags | 公开 | 所有标签列表 |
| POST | /api/admin/tags | 管理员 | 创建标签 |
| DELETE | /api/admin/tags/{id} | 管理员 | 删除标签 |

### 5.5 图片上传

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/admin/upload/image | 登录 | 上传图片 |
| DELETE | /api/admin/upload/image/{id} | 登录 | 删除图片 |

### 5.6 用户管理（管理员）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /api/admin/users | 管理员 | 用户列表 |
| GET | /api/admin/users/{id} | 管理员 | 用户详情 |
| PUT | /api/admin/users/{id}/approve | 管理员 | 审核通过 |
| PUT | /api/admin/users/{id}/disable | 管理员 | 禁用用户 |

## 6. 前端路由与页面

### 6.1 路由表

```
/                        首页 - 公开文章卡片列表
/article/:id             文章详情 - Markdown 渲染
/tag/:tagName            标签筛选 - 按标签展示文章
/login                   登录
/register                注册

/admin/dashboard         后台首页 - 数据概览
/admin/articles          文章管理列表
/admin/articles/create   写文章
/admin/articles/edit/:id 编辑文章
/admin/tags              标签管理
/admin/images            图片管理
/admin/users             用户管理（仅管理员可见）
/admin/profile           个人资料
```

### 6.2 核心组件

- **AppHeader.vue** — 导航栏（包含亮色/深色切换按钮）
- **ArticleCard.vue** — 文章卡片（封面、标题、摘要、标签、日期）
- **MarkdownRenderer.vue** — Markdown 渲染组件（marked + highlight.js）
- **ThemeToggle.vue** — 亮色/深色切换
- **ArticleEditor.vue** — Markdown 编辑器 + 图片上传
- **ImageUploader.vue** — 拖拽/粘贴上传组件

## 7. 关键流程

### 7.1 JWT 认证

1. 用户登录 → 后端验证账号密码（BCrypt）→ 签发 JWT（含 userId、role）
2. 前端 localStorage 存储 token，axios 拦截器自动携带 Bearer token
3. 后端 Spring Security Filter 解析 token，设置 SecurityContext
4. 接口层通过注解或代码校验角色和资源归属

### 7.2 图片上传

1. 前端上传 → 后端接收 MultipartFile
2. UUID 重命名文件，存入 `uploads/` 目录
3. 记录图片信息到 `image` 表，返回访问 URL
4. 前端在 Markdown 编辑器中插入 `![alt](url)`

### 7.3 用户注册流程

1. 用户提交注册 → `user.status = pending`
2. 管理员登录后台 → 用户管理列表 → 审核通过 → `status = active`
3. 用户方可登录使用

### 7.4 亮色/深色模式

1. CSS 变量定义两套颜色 token
2. Pinia store 存储当前主题
3. 默认跟随系统 `prefers-color-scheme`
4. 用户手动切换持久化到 localStorage

## 8. 权限模型

| 角色 | 可操作 |
|------|--------|
| 未登录 | 查看文章列表、文章详情、标签 |
| user（已激活） | 创建/编辑/删除自己的文章、上传图片 |
| admin | 所有文章管理、标签管理、用户审核、用户禁用 |

后端通过 Spring Security 的 `@PreAuthorize` 和自定义权限校验实现。

## 9. 测试策略

- **Mapper 层**：MyBatis-Plus BaseMapper 的 CRUD 基本不测，关注自定义 SQL 查询
- **Service 层**：单元测试核心业务逻辑，Mock Mapper
- **Controller 层**：MockMvc 测试接口请求/响应
- 测试框架：JUnit 5 + Mockito + AssertJ

## 10. 后续规划（v2+）

- 点赞功能
- 评论系统（支持回复）
- RSS 订阅
- 文章搜索（全文检索）
- 统计分析（访问量、热门文章等）
