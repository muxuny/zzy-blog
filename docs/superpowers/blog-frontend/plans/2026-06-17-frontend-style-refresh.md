# Frontend Style Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将博客前台、认证页和后台管理页统一优化为“内容工作台”视觉系统，并修复可见中文乱码。

**Architecture:** 先建立全局设计变量和组件库覆盖样式，再逐层优化公共组件、前台页面、认证页面和后台页面。业务接口、路由、权限和状态管理保持不变；所有改动集中在样式、模板文案和少量结构类名上。

**Tech Stack:** Vue 3、Vite、Vue Router、Pinia、Element Plus、marked、highlight.js、CSS 变量。

---

## 执行前说明

在 Windows 环境中，`npm` 被解析到了 `C:\WINDOWS\system32\npm` 并会报“拒绝访问”。本项目构建验证请使用：

```powershell
npm.cmd run build
```

当前基线结果：`npm.cmd run build` 可以通过，Vite 仅提示部分 chunk 超过 500 kB。这是已有打包体积警告，不属于本次样式任务的失败条件。

## 文件职责

- `src/styles/theme.css`：设计 token、亮暗色变量、Element Plus 基础变量映射。
- `src/styles/global.css`：全局重置、组件库覆盖、Markdown 正文、后台通用页面样式、响应式基础规则。
- `src/components/AppHeader.vue`：前台吸顶导航、登录注册入口、用户菜单中文文案。
- `src/components/ThemeToggle.vue`：主题切换图标按钮和无障碍标签。
- `src/components/ArticleCard.vue`：前台文章列表核心卡片。
- `src/components/MarkdownRenderer.vue`：不改逻辑，只保留为全局 `.article-content` 样式的承载组件。
- `src/views/Home.vue`：首页文章流和标签筛选面板。
- `src/views/ArticleDetail.vue`：文章详情阅读页。
- `src/views/TagArticles.vue`：标签文章列表页。
- `src/views/Login.vue`：登录页中文文案和认证面板样式。
- `src/views/Register.vue`：注册页中文文案、提示块和认证面板样式。
- `src/components/admin/AdminLayout.vue`：后台侧栏、顶部栏、菜单中文文案和工作台骨架。
- `src/views/admin/Dashboard.vue`：后台仪表盘指标卡基准。
- `src/views/admin/Articles.vue`、`ArticleEdit.vue`、`Tags.vue`、`Images.vue`、`Users.vue`、`Profile.vue`：修复可见中文乱码，套用全局后台页面样式。
- `src/components/admin/ArticleEditor.vue`、`ImageUploader.vue`：修复中文文案，轻量统一编辑器和上传控件样式。

## 任务 1：建立验证基线和乱码清单

**Files:**
- Inspect: `src/**/*.vue`
- No code changes

- [ ] **Step 1: 运行构建基线**

Run:

```powershell
npm.cmd run build
```

Expected: exit code `0`，输出包含 `✓ built in`。允许出现 chunk size warning。

- [ ] **Step 2: 搜索明显乱码**

Run:

```powershell
rg -n "鍚|閫|鐧|娉|鏍|鏂|闃|璇|绠|浠|宸|寰|鐢|瀵|棰|涓|鏄|鐘|鎿|灏|脳" src
```

Expected: 当前会列出多处 `.vue` 文件；把输出作为修复清单，不需要提交。

- [ ] **Step 3: 确认没有用户未提交改动**

Run:

```powershell
git status --short
```

Expected: 没有输出。如果有输出，只继续处理本计划涉及文件，不回退用户改动。

## 任务 2：落地全局设计 token 和组件库基础覆盖

**Files:**
- Modify: `src/styles/theme.css`
- Modify: `src/styles/global.css`

- [ ] **Step 1: 替换 `src/styles/theme.css` 为设计变量**

Replace the file with:

```css
:root {
  color-scheme: light;
  --bg-color: #f8fafc;
  --text-color: #172334;
  --muted-text-color: #667085;
  --card-bg: #ffffff;
  --panel-bg: #ffffff;
  --elevated-bg: #ffffff;
  --code-bg: #eef4fb;
  --border-color: #d9e1ec;
  --soft-border-color: #e6edf5;
  --primary-color: #2f80ed;
  --primary-hover-color: #1f6fd8;
  --accent-color: #2fb3a3;
  --warning-color: #f59e0b;
  --danger-color: #e5484d;
  --shadow-sm: 0 8px 22px rgba(23, 35, 52, 0.07);
  --shadow-md: 0 18px 44px rgba(23, 35, 52, 0.1);
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --app-header-height: 64px;
  --content-width: 1180px;
  --reading-width: 820px;
  --el-color-primary: var(--primary-color);
  --el-color-success: var(--accent-color);
  --el-color-warning: var(--warning-color);
  --el-color-danger: var(--danger-color);
  --el-border-radius-base: var(--radius-sm);
}

[data-theme="dark"] {
  color-scheme: dark;
  --bg-color: #0f172a;
  --text-color: #e5edf7;
  --muted-text-color: #a9b6c7;
  --card-bg: #182235;
  --panel-bg: #182235;
  --elevated-bg: #1f2a3d;
  --code-bg: #111c2f;
  --border-color: #2b3850;
  --soft-border-color: #26344a;
  --primary-color: #5b9cff;
  --primary-hover-color: #7fb2ff;
  --accent-color: #4fd1c5;
  --warning-color: #f6b84b;
  --danger-color: #ff6b6f;
  --shadow-sm: 0 10px 26px rgba(0, 0, 0, 0.22);
  --shadow-md: 0 20px 52px rgba(0, 0, 0, 0.3);
}

html {
  background: var(--bg-color);
}

body {
  background: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.24s ease, color 0.24s ease;
}
```

- [ ] **Step 2: 重写 `src/styles/global.css` 的基础段落**

Keep the file focused. At the top, use:

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  min-width: 320px;
  text-size-adjust: 100%;
}

body {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", Roboto, sans-serif;
  font-size: 15px;
  line-height: 1.6;
  letter-spacing: 0;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(248, 250, 252, 0.96)),
    var(--bg-color);
}

[data-theme="dark"] body {
  background:
    radial-gradient(circle at 20% 0%, rgba(47, 128, 237, 0.12), transparent 34%),
    var(--bg-color);
}

a {
  color: var(--primary-color);
  text-decoration: none;
}

a:hover {
  color: var(--primary-hover-color);
}

button,
input,
textarea,
select {
  font: inherit;
}

:focus-visible {
  outline: 3px solid rgba(47, 128, 237, 0.28);
  outline-offset: 2px;
}
```

- [ ] **Step 3: 添加全局 Element Plus 覆盖**

Append this block in `src/styles/global.css` after the base styles:

```css
.el-card {
  border-color: var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--card-bg);
  color: var(--text-color);
  box-shadow: var(--shadow-sm);
}

.el-card__header {
  border-bottom-color: var(--soft-border-color);
  padding: 16px 18px;
  font-weight: 700;
}

.el-button {
  border-radius: var(--radius-sm);
  font-weight: 650;
}

.el-button.is-text {
  color: var(--muted-text-color);
}

.el-button.is-text:hover {
  color: var(--primary-color);
  background: rgba(47, 128, 237, 0.08);
}

.el-input__wrapper,
.el-textarea__inner,
.el-select__wrapper {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  background: var(--panel-bg);
}

.el-table {
  --el-table-border-color: var(--soft-border-color);
  --el-table-header-bg-color: rgba(47, 128, 237, 0.06);
  --el-table-tr-bg-color: var(--panel-bg);
  --el-table-row-hover-bg-color: rgba(47, 128, 237, 0.06);
  color: var(--text-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.el-table th.el-table__cell {
  color: var(--muted-text-color);
  font-weight: 750;
}

.el-tag {
  border-radius: 999px;
  font-weight: 650;
}

.el-pagination {
  --el-pagination-button-bg-color: var(--panel-bg);
  --el-pagination-hover-color: var(--primary-color);
}

.el-empty__description p {
  color: var(--muted-text-color);
}
```

- [ ] **Step 4: 添加 Markdown 和后台通用样式**

Append:

```css
.article-content {
  color: var(--text-color);
  font-size: 16px;
  line-height: 1.85;
}

.article-content h1,
.article-content h2,
.article-content h3 {
  margin: 2em 0 0.75em;
  line-height: 1.25;
  color: var(--text-color);
}

.article-content p,
.article-content ul,
.article-content ol {
  margin: 1em 0;
}

.article-content pre {
  margin: 1.3em 0;
  padding: 18px;
  overflow-x: auto;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--code-bg);
}

.article-content code {
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.92em;
}

.article-content :not(pre) > code {
  padding: 0.16em 0.38em;
  border-radius: 5px;
  background: var(--code-bg);
}

.article-content img {
  display: block;
  max-width: 100%;
  margin: 1.4em auto;
  border-radius: var(--radius-md);
}

.article-content blockquote {
  margin: 1.3em 0;
  padding: 0.2em 0 0.2em 1.1em;
  border-left: 4px solid var(--accent-color);
  color: var(--muted-text-color);
}

.admin-page,
.admin-panel {
  color: var(--text-color);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-header h2,
.admin-page h2 {
  font-size: 24px;
  line-height: 1.25;
  font-weight: 800;
  color: var(--text-color);
}
```

- [ ] **Step 5: 添加响应式基础规则**

Append:

```css
@media (max-width: 768px) {
  body {
    font-size: 14px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .article-content {
    font-size: 15px;
  }
}
```

- [ ] **Step 6: 构建验证**

Run:

```powershell
npm.cmd run build
```

Expected: exit code `0`。

- [ ] **Step 7: 提交**

```powershell
git add src/styles/theme.css src/styles/global.css
git commit -m "style: add content workbench design tokens"
```

## 任务 3：优化前台公共组件

**Files:**
- Modify: `src/components/AppHeader.vue`
- Modify: `src/components/ThemeToggle.vue`
- Modify: `src/components/ArticleCard.vue`

- [ ] **Step 1: 更新 `AppHeader.vue` 模板文案和类名**

Use this template shape:

```vue
<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo" aria-label="返回首页">
        <span class="logo-mark">B</span>
        <span class="logo-text">ZZY Blog</span>
      </router-link>
      <div class="header-right">
        <ThemeToggle />
        <template v-if="authStore.isLoggedIn">
          <el-dropdown>
            <span class="user-info">
              {{ authStore.user?.nickname || authStore.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/admin/dashboard')">后台管理</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button text @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>
```

- [ ] **Step 2: 更新 `AppHeader.vue` 样式**

Replace scoped style with:

```css
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--app-header-height);
  padding: 0;
  border-bottom: 1px solid var(--soft-border-color);
  background: color-mix(in srgb, var(--panel-bg) 88%, transparent);
  backdrop-filter: blur(14px);
}

.header-inner {
  width: min(100%, var(--content-width));
  height: var(--app-header-height);
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-color);
  font-weight: 800;
}

.logo-mark {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--primary-color);
  color: #fff;
  box-shadow: 0 10px 24px rgba(47, 128, 237, 0.24);
}

.logo-text {
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  color: var(--text-color);
  cursor: pointer;
}

.user-info:hover {
  background: rgba(47, 128, 237, 0.08);
}

@media (max-width: 640px) {
  .header-inner {
    padding: 0 14px;
  }

  .logo-text {
    display: none;
  }
}
```

- [ ] **Step 3: 更新 `ThemeToggle.vue`**

Replace template with:

```vue
<template>
  <el-button
    class="theme-toggle"
    text
    :aria-label="themeStore.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    :title="themeStore.isDark ? '切换到亮色模式' : '切换到暗色模式'"
    :icon="themeStore.isDark ? Moon : Sunny"
    @click="themeStore.toggle()"
  />
</template>
```

Add scoped style:

```css
.theme-toggle {
  width: 36px;
  height: 36px;
  padding: 0;
}
```

- [ ] **Step 4: 更新 `ArticleCard.vue` 元信息和封面结构**

Use this metadata text:

```vue
<div class="card-meta">
  <span>{{ formatDate(article.createdAt) }}</span>
  <span v-if="article.authorName">{{ article.authorName }}</span>
  <span>阅读 {{ article.viewCount || 0 }}</span>
</div>
```

Use this image block:

```vue
<div class="card-image" v-if="article.coverImage">
  <img :src="article.coverImage" :alt="article.title" />
</div>
```

- [ ] **Step 5: 更新 `ArticleCard.vue` 样式**

Replace scoped style with:

```css
.article-card {
  margin-bottom: 18px;
  cursor: pointer;
  background: var(--card-bg);
  border: 1px solid var(--soft-border-color);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.article-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 38%, var(--soft-border-color));
  box-shadow: var(--shadow-md);
}

.card-body {
  display: flex;
  gap: 20px;
  align-items: stretch;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  margin-bottom: 10px;
  color: var(--text-color);
  font-size: 21px;
  line-height: 1.35;
  font-weight: 800;
}

.card-summary {
  color: var(--muted-text-color);
  font-size: 14px;
  line-height: 1.75;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin: 12px 0;
  color: var(--muted-text-color);
  font-size: 13px;
}

.card-meta span {
  position: relative;
}

.card-meta span + span::before {
  content: "";
  position: absolute;
  left: -8px;
  top: 50%;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: var(--border-color);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.card-image {
  width: 190px;
  flex: 0 0 190px;
}

.card-image img {
  width: 100%;
  height: 128px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--soft-border-color);
}

@media (max-width: 720px) {
  .card-body {
    flex-direction: column-reverse;
  }

  .card-image {
    width: 100%;
    flex-basis: auto;
  }

  .card-image img {
    height: auto;
    aspect-ratio: 16 / 9;
  }
}
```

- [ ] **Step 6: 构建验证并提交**

Run:

```powershell
npm.cmd run build
git add src/components/AppHeader.vue src/components/ThemeToggle.vue src/components/ArticleCard.vue
git commit -m "style: refresh public shared components"
```

Expected: build exit code `0` before commit.

## 任务 4：优化前台页面布局

**Files:**
- Modify: `src/views/Home.vue`
- Modify: `src/views/ArticleDetail.vue`
- Modify: `src/views/TagArticles.vue`

- [ ] **Step 1: 更新 `Home.vue` 标签面板文案和结构**

Replace the sidebar card with:

```vue
<aside class="sidebar">
  <section class="tag-panel">
    <div class="panel-label">筛选</div>
    <h2>标签</h2>
    <div class="tag-list">
      <button
        v-for="tag in tags"
        :key="tag.id"
        class="tag-filter"
        :class="{ active: activeTag === tag.id }"
        type="button"
        @click="filterByTag(tag)"
      >
        {{ tag.name }}
      </button>
    </div>
  </section>
</aside>
```

Set empty state:

```vue
<el-empty v-if="!articles.length && !loading" description="暂无文章" />
```

- [ ] **Step 2: 更新 `Home.vue` 样式**

Replace scoped style with:

```css
.main {
  width: min(100%, var(--content-width));
  margin: 0 auto;
  padding: 28px 24px 56px;
}

.container {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: calc(var(--app-header-height) + 24px);
}

.tag-panel {
  padding: 18px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.panel-label {
  margin-bottom: 4px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.tag-panel h2 {
  margin-bottom: 14px;
  font-size: 20px;
  color: var(--text-color);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-filter {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--panel-bg);
  color: var(--muted-text-color);
  cursor: pointer;
}

.tag-filter.active,
.tag-filter:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: rgba(47, 128, 237, 0.08);
}

.content {
  min-width: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin: 28px 0 0;
}

@media (max-width: 860px) {
  .container {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    order: 2;
  }
}
```

- [ ] **Step 3: 更新 `ArticleDetail.vue` 文案和结构**

Use this metadata block:

```vue
<div class="article-meta">
  <span>{{ formatDate(article.createdAt) }}</span>
  <span v-if="article.authorName">{{ article.authorName }}</span>
  <span>阅读 {{ article.viewCount || 0 }}</span>
</div>
```

Wrap content:

```vue
<article v-if="article" class="article-shell">
  <header class="article-head">
    <h1 class="article-title">{{ article.title }}</h1>
    <div class="article-meta">
      <span>{{ formatDate(article.createdAt) }}</span>
      <span v-if="article.authorName">{{ article.authorName }}</span>
      <span>阅读 {{ article.viewCount || 0 }}</span>
    </div>
    <div class="article-tags" v-if="article.tags?.length">
      <el-tag v-for="t in article.tags" :key="t.id" size="small">{{ t.name }}</el-tag>
    </div>
  </header>
  <MarkdownRenderer :content="article.content" />
</article>
```

- [ ] **Step 4: 更新 `ArticleDetail.vue` 样式**

Replace scoped style with:

```css
.main {
  width: min(100%, var(--reading-width));
  margin: 0 auto;
  padding: 38px 24px 72px;
}

.article-shell {
  padding: 34px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-lg);
  background: var(--panel-bg);
  box-shadow: var(--shadow-sm);
}

.article-head {
  margin-bottom: 28px;
  padding-bottom: 22px;
  border-bottom: 1px solid var(--soft-border-color);
}

.article-title {
  margin-bottom: 14px;
  color: var(--text-color);
  font-size: clamp(30px, 5vw, 44px);
  line-height: 1.14;
  font-weight: 850;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 16px;
  color: var(--muted-text-color);
  font-size: 14px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 640px) {
  .main {
    padding: 22px 14px 48px;
  }

  .article-shell {
    padding: 22px 18px;
  }
}
```

- [ ] **Step 5: 更新 `TagArticles.vue` 文案和样式**

Use title:

```vue
<header class="tag-page-head">
  <span>标签</span>
  <h1>{{ route.params.name }}</h1>
</header>
```

Set empty state:

```vue
<el-empty v-if="!articles.length" description="该标签下暂无文章" />
```

Scoped style:

```css
.main {
  width: min(100%, var(--reading-width));
  margin: 0 auto;
  padding: 34px 24px 64px;
}

.tag-page-head {
  margin-bottom: 22px;
  padding-left: 16px;
  border-left: 4px solid var(--accent-color);
}

.tag-page-head span {
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
}

.tag-page-head h1 {
  color: var(--text-color);
  font-size: 32px;
  line-height: 1.2;
}
```

- [ ] **Step 6: 构建验证并提交**

Run:

```powershell
npm.cmd run build
git add src/views/Home.vue src/views/ArticleDetail.vue src/views/TagArticles.vue
git commit -m "style: refresh public reading pages"
```

Expected: build exit code `0` before commit.

## 任务 5：优化登录注册页面并修复认证文案

**Files:**
- Modify: `src/views/Login.vue`
- Modify: `src/views/Register.vue`

- [ ] **Step 1: 修复 `Login.vue` 文案**

Use these exact strings:

```js
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
```

Success message:

```js
ElMessage.success('登录成功')
```

Template copy:

```vue
<h1>登录</h1>
<p class="auth-subtitle">进入后台，继续管理你的内容。</p>
<el-input v-model="form.username" placeholder="用户名" />
<el-input v-model="form.password" type="password" placeholder="密码" show-password />
<el-button type="primary" @click="handleLogin" :loading="loading" class="auth-submit">登录</el-button>
<div class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></div>
```

- [ ] **Step 2: 修复 `Register.vue` 文案**

Use these exact strings:

```js
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度为 3-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于 6 位', trigger: 'blur' }
  ]
}
```

Success message:

```js
ElMessage.success('注册成功，等待管理员审核')
```

Template copy:

```vue
<h1>注册</h1>
<p class="auth-subtitle">创建账号后，需要管理员审核才能登录后台。</p>
<el-input v-model="form.username" placeholder="用户名" />
<el-input v-model="form.password" type="password" placeholder="密码" show-password />
<el-input v-model="form.nickname" placeholder="昵称（选填）" />
<div class="auth-notice">注册后需管理员审核方可登录。</div>
<el-button type="primary" @click="handleRegister" :loading="loading" class="auth-submit">注册</el-button>
<div class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></div>
```

- [ ] **Step 3: 给两个认证页面使用同一套 scoped 样式**

Apply this style block in both files:

```css
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px;
  background:
    linear-gradient(90deg, rgba(47, 128, 237, 0.06) 1px, transparent 1px),
    linear-gradient(180deg, rgba(47, 128, 237, 0.06) 1px, transparent 1px),
    var(--bg-color);
  background-size: 34px 34px;
}

.auth-card {
  width: min(100%, 420px);
  border: 1px solid var(--soft-border-color);
}

.auth-card :deep(.el-card__body) {
  padding: 30px;
}

h1 {
  margin-bottom: 6px;
  color: var(--text-color);
  font-size: 30px;
  line-height: 1.2;
  font-weight: 850;
  text-align: center;
}

.auth-subtitle {
  margin-bottom: 24px;
  color: var(--muted-text-color);
  text-align: center;
}

.auth-submit {
  width: 100%;
  min-height: 40px;
}

.auth-notice {
  margin-bottom: 14px;
  padding: 10px 12px;
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: var(--radius-sm);
  color: #9a5a00;
  background: rgba(245, 158, 11, 0.11);
  text-align: center;
}

[data-theme="dark"] .auth-notice {
  color: #ffd28a;
}

.auth-link {
  margin-top: 4px;
  color: var(--muted-text-color);
  font-size: 14px;
  text-align: center;
}
```

- [ ] **Step 4: 构建验证并提交**

Run:

```powershell
npm.cmd run build
git add src/views/Login.vue src/views/Register.vue
git commit -m "style: refresh auth pages"
```

Expected: build exit code `0` before commit.

## 任务 6：优化后台布局和仪表盘

**Files:**
- Modify: `src/components/admin/AdminLayout.vue`
- Modify: `src/views/admin/Dashboard.vue`

- [ ] **Step 1: 修复 `AdminLayout.vue` 菜单和顶部栏文案**

Use these exact labels:

```vue
<div class="admin-logo">Blog Admin</div>
<el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
<el-menu-item index="/admin/articles"><el-icon><Document /></el-icon><span>文章管理</span></el-menu-item>
<el-menu-item index="/admin/tags"><el-icon><PriceTag /></el-icon><span>标签管理</span></el-menu-item>
<el-menu-item index="/admin/images"><el-icon><Picture /></el-icon><span>图片管理</span></el-menu-item>
<el-menu-item index="/admin/users" v-if="authStore.isAdmin"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
<el-menu-item index="/admin/profile"><el-icon><Setting /></el-icon><span>个人资料</span></el-menu-item>
```

Top bar:

```vue
<span>欢迎回来，{{ authStore.user?.nickname || authStore.user?.username }}</span>
<el-button text @click="$router.push('/')">返回博客</el-button>
<el-button text @click="logout">退出登录</el-button>
```

- [ ] **Step 2: 更新 `AdminLayout.vue` 结构类和样式**

Use root class:

```vue
<el-container class="admin-layout">
```

Use `el-main` class:

```vue
<el-main class="admin-main"><router-view /></el-main>
```

Scoped style:

```css
.admin-layout {
  min-height: 100vh;
  background: var(--bg-color);
}

.admin-sidebar {
  background: #172334;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  color: #fff;
  font-size: 18px;
  font-weight: 850;
  letter-spacing: 0;
}

.admin-sidebar :deep(.el-menu) {
  border-right: 0;
  background: transparent;
}

.admin-sidebar :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 10px;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.74);
}

.admin-sidebar :deep(.el-menu-item.is-active) {
  position: relative;
  color: #fff;
  background: rgba(47, 128, 237, 0.2);
}

.admin-sidebar :deep(.el-menu-item.is-active::before) {
  content: "";
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 999px;
  background: var(--accent-color);
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--soft-border-color);
  background: var(--panel-bg);
  color: var(--text-color);
}

.admin-main {
  padding: 28px;
  background: var(--bg-color);
}
```

- [ ] **Step 3: 更新 `Dashboard.vue` 结构和文案**

Use:

```vue
<template>
  <div class="admin-page dashboard-page">
    <div class="page-header">
      <div>
        <span class="page-kicker">概览</span>
        <h2>仪表盘</h2>
      </div>
    </div>
    <div class="stat-grid">
      <el-card class="stat-card">
        <span>文章总数</span>
        <strong>{{ stats.totalArticles }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>已发布</span>
        <strong>{{ stats.publishedArticles }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>标签数</span>
        <strong>{{ stats.totalTags }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>待审核</span>
        <strong>{{ stats.pendingUsers }}</strong>
      </el-card>
    </div>
  </div>
</template>
```

- [ ] **Step 4: 更新 `Dashboard.vue` 样式**

Use:

```css
.page-kicker {
  display: inline-block;
  margin-bottom: 4px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--accent-color);
}

.stat-card span {
  display: block;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
}

.stat-card strong {
  display: block;
  margin-top: 8px;
  color: var(--text-color);
  font-size: 34px;
  line-height: 1;
}

@media (max-width: 980px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **Step 5: 构建验证并提交**

Run:

```powershell
npm.cmd run build
git add src/components/admin/AdminLayout.vue src/views/admin/Dashboard.vue
git commit -m "style: refresh admin shell and dashboard"
```

Expected: build exit code `0` before commit.

## 任务 7：修复后台管理页和后台组件的可见中文

**Files:**
- Modify: `src/views/admin/Articles.vue`
- Modify: `src/views/admin/ArticleEdit.vue`
- Modify: `src/views/admin/Tags.vue`
- Modify: `src/views/admin/Images.vue`
- Modify: `src/views/admin/Users.vue`
- Modify: `src/views/admin/Profile.vue`
- Modify: `src/components/admin/ArticleEditor.vue`
- Modify: `src/components/admin/ImageUploader.vue`

- [ ] **Step 1: 修复 `Articles.vue` 文案**

Use exact replacements:

```vue
<div class="page-header"><h2>文章管理</h2><el-button type="primary" @click="$router.push('/admin/articles/create')">写文章</el-button></div>
<el-table-column prop="title" label="标题" min-width="200" />
<el-table-column prop="createdBy" label="作者" width="120" />
<el-table-column prop="status" label="状态" width="100">
  <template #default="{row}"><el-tag :type="row.status==='published'?'success':'info'">{{ row.status==='published'?'已发布':'草稿' }}</el-tag></template>
</el-table-column>
<el-table-column prop="viewCount" label="阅读" width="80" />
<el-table-column label="时间" width="180"><template #default="{row}">{{ formatDate(row.createdAt) }}</template></el-table-column>
<el-table-column label="操作" width="200">
```

Delete confirm:

```js
await ElMessageBox.confirm('确定删除？', '提示')
ElMessage.success('删除成功')
```

- [ ] **Step 2: 修复 `ArticleEdit.vue` 文案**

Use exact labels:

```vue
<h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
<el-form-item label="标题"><el-input v-model="form.title" placeholder="文章标题" /></el-form-item>
<el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
<el-form-item label="封面图"><ImageUploader v-model="form.coverImage" /></el-form-item>
<el-form-item label="标签"><el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
<el-form-item label="内容"><ArticleEditor v-model="form.content" /></el-form-item>
<el-button type="primary" @click="save('draft')">保存草稿</el-button>
<el-button type="success" @click="save('published')">发布</el-button>
<el-button @click="$router.back()">取消</el-button>
```

Success messages:

```js
ElMessage.success('更新成功')
ElMessage.success('创建成功')
```

- [ ] **Step 3: 修复 `Tags.vue` 文案**

Use:

```vue
<div class="page-header">
  <h2>标签管理</h2>
  <div class="inline-create">
    <el-input v-model="newName" placeholder="标签名" />
    <el-button type="primary" @click="handleCreate">新增</el-button>
  </div>
</div>
<el-table-column prop="name" label="标签名" />
<el-table-column label="创建时间"><template #default="{row}">{{ formatDate(row.createdAt) }}</template></el-table-column>
<el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column>
```

Messages:

```js
ElMessage.success('创建成功')
ElMessage.success('删除成功')
```

Add scoped style:

```css
.inline-create {
  display: flex;
  gap: 8px;
}

.inline-create .el-input {
  width: 200px;
}
```

- [ ] **Step 4: 修复 `Images.vue` 文案**

Use:

```vue
<div class="page-header"><h2>图片管理</h2><ImageUploader v-model="uploadUrl" @success="load" /></div>
<el-table-column label="预览" width="100">
<el-table-column prop="originalName" label="文件名" />
<el-table-column label="大小" width="100">
<el-table-column label="上传时间" width="180">
<el-table-column label="操作" width="120">
```

Message:

```js
ElMessage.success('删除成功')
```

- [ ] **Step 5: 修复 `Users.vue` 文案**

Use:

```vue
<div class="admin-page"><h2>用户管理</h2>
<el-table-column prop="username" label="用户名" />
<el-table-column prop="nickname" label="昵称" />
<el-table-column prop="email" label="邮箱" />
<el-table-column prop="role" label="角色" width="100">
<el-table-column prop="status" label="状态" width="100">
  <template #default="{row}">
    <el-tag :type="row.status==='active'?'success':row.status==='pending'?'warning':'info'">{{ row.status==='active'?'已激活':row.status==='pending'?'待审核':'已禁用' }}</el-tag>
  </template>
</el-table-column>
<el-table-column label="操作" width="200">
<el-button v-if="row.status==='pending'" size="small" type="success" @click="handleApprove(row.id)">审核通过</el-button>
<el-button v-if="row.status==='active'" size="small" type="warning" @click="handleDisable(row.id)">禁用</el-button>
```

Messages:

```js
ElMessage.success('已通过')
ElMessage.success('已禁用')
```

- [ ] **Step 6: 修复 `Profile.vue` 文案**

Use:

```vue
<div class="admin-page"><h2>个人资料</h2>
<el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
<el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
<el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
<el-descriptions-item label="角色">{{ user?.role }}</el-descriptions-item>
<el-descriptions-item label="状态"><el-tag :type="user?.status==='active'?'success':'warning'">{{ user?.status }}</el-tag></el-descriptions-item>
<el-descriptions-item label="注册时间">{{ formatDate(user?.createdAt) }}</el-descriptions-item>
```

- [ ] **Step 7: 修复后台组件文案**

`ArticleEditor.vue`:

```vue
<el-input v-model="content" type="textarea" :rows="20" placeholder="使用 Markdown 语法编写..." @input="emit" class="editor-textarea" />
<div class="preview-header">预览</div>
```

`ImageUploader.vue`:

```vue
<el-button type="primary">上传图片</el-button>
<el-button size="small" type="danger" @click="$emit('update:modelValue','')" circle>×</el-button>
```

Messages:

```js
ElMessage.success('上传成功')
ElMessage.error('上传失败')
```

- [ ] **Step 8: 轻量统一编辑器和上传控件样式**

In `ArticleEditor.vue`, update scoped style:

```css
.editor-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  min-height: 500px;
}

.editor-textarea :deep(textarea) {
  min-height: 500px;
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 14px;
  line-height: 1.7;
}

.editor-preview {
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  background: var(--panel-bg);
}

.preview-header {
  padding: 10px 12px;
  border-bottom: 1px solid var(--soft-border-color);
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
  background: rgba(47, 128, 237, 0.06);
}

.editor-preview :deep(.article-content) {
  padding: 16px;
}

@media (max-width: 980px) {
  .editor-wrapper {
    grid-template-columns: 1fr;
  }
}
```

In `ImageUploader.vue`, update scoped style:

```css
.uploader {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.preview {
  position: relative;
}

.preview img {
  max-width: 200px;
  max-height: 120px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  object-fit: cover;
}

.preview .el-button {
  position: absolute;
  top: -8px;
  right: -8px;
}
```

- [ ] **Step 9: 扫描乱码、构建验证并提交**

Run:

```powershell
rg -n "鍚|閫|鐧|娉|鏍|鏂|闃|璇|绠|浠|宸|寰|鐢|瀵|棰|涓|鏄|鐘|鎿|灏|脳" src
npm.cmd run build
```

Expected: `rg` 没有输出；build exit code `0`。

Commit:

```powershell
git add src/views/admin src/components/admin
git commit -m "fix: restore admin Chinese copy"
```

## 任务 8：最终视觉验证和收尾

**Files:**
- Inspect only unless verification finds defects

- [ ] **Step 1: 运行最终构建**

Run:

```powershell
npm.cmd run build
```

Expected: exit code `0`，允许已有 chunk size warning。

- [ ] **Step 2: 启动本地服务**

Run:

```powershell
npm.cmd run dev -- --host 127.0.0.1
```

Expected: Vite prints a local URL, usually `http://127.0.0.1:5173/`.

- [ ] **Step 3: 浏览器检查前台页面**

Open:

```text
http://127.0.0.1:5173/
http://127.0.0.1:5173/login
http://127.0.0.1:5173/register
```

Expected:

- 顶部导航没有溢出。
- 首页标签面板和文章卡片视觉统一。
- 登录/注册页中文正常，表单不破版。
- 亮色和暗色切换后背景、卡片、输入框和文字都可读。

- [ ] **Step 4: 浏览器检查后台页面**

If a token exists in local storage, open:

```text
http://127.0.0.1:5173/admin/dashboard
http://127.0.0.1:5173/admin/articles
http://127.0.0.1:5173/admin/tags
http://127.0.0.1:5173/admin/images
http://127.0.0.1:5173/admin/profile
```

Expected:

- 侧栏深色工作台样式生效。
- 仪表盘指标卡有左侧状态条。
- 表格、按钮、标签和分页风格统一。
- 页面没有明显中文乱码。

- [ ] **Step 5: 移动宽度检查**

Use browser responsive mode or viewport width around `390px`.

Expected:

- 首页双栏收为单栏。
- 文章卡片封面和文字纵向排列。
- 登录/注册卡片不超出屏幕。
- 顶部导航品牌文字可隐藏，但按钮仍可点击。

- [ ] **Step 6: 状态检查**

Run:

```powershell
git status --short
```

Expected: 没有输出。如果有验证修复，提交：

```powershell
git add src
git commit -m "fix: polish frontend style refresh"
```

## 自检清单

- 设计 token：任务 2 覆盖。
- 全局组件库样式统一：任务 2 覆盖。
- 顶部导航和主题切换：任务 3 覆盖。
- 首页、文章详情、标签页：任务 4 覆盖。
- 登录注册：任务 5 覆盖。
- 后台布局和仪表盘：任务 6 覆盖。
- 其他后台页面、编辑器、上传控件中文修复：任务 7 覆盖。
- 构建和浏览器验证：任务 8 覆盖。
- 非目标限制：所有任务只修改样式、模板文案和轻量结构类名，不改接口、权限、路由或业务流程。
