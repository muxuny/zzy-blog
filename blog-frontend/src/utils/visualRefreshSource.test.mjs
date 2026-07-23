import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

function read(relativePath) {
  return readFileSync(new URL(relativePath, import.meta.url), 'utf8')
}

test('theme toggle renders an appearance popover with palette and mode choices', () => {
  const source = read('../components/ThemeToggle.vue')

  assert.match(source, /class="appearance-trigger"/)
  assert.match(source, /外观/)
  assert.match(source, /themeStore\.palettes/)
  assert.match(source, /themeStore\.modes/)
  assert.match(source, /setPalette/)
  assert.match(source, /setMode/)
  assert.match(source, /class="palette-swatch"/)
})

test('theme css defines fixed palette packages and reduced motion support', () => {
  const theme = read('../styles/theme.css')
  const global = read('../styles/global.css')
  const login = read('../views/Login.vue')
  const register = read('../views/Register.vue')

  assert.match(theme, /\[data-palette="juniper"\]/)
  assert.match(theme, /\[data-palette="fog"\]/)
  assert.match(theme, /\[data-palette="copper"\]/)
  assert.match(theme, /\[data-theme="dark"\]/)
  assert.match(theme, /\[data-palette="fog"\]\[data-theme="dark"\]/)
  assert.match(theme, /\[data-palette="copper"\]\[data-theme="dark"\]/)
  assert.match(theme, /--theme-grid-x/)
  assert.match(theme, /--header-backdrop-bg/)
  assert.match(theme, /--el-color-primary:\s*var\(--primary-color\)/)
  assert.match(theme, /--el-bg-color:\s*var\(--panel-bg\)/)
  assert.match(theme, /--el-text-color-primary:\s*var\(--text-color\)/)
  assert.match(global, /prefers-reduced-motion:\s*reduce/)
  assert.doesNotMatch(global, /rgba\(47,\s*128,\s*237,\s*0\.08\)/)
  assert.doesNotMatch(global, /rgba\(47,\s*128,\s*237,\s*0\.06\)/)
  assert.match(global, /--el-table-header-bg-color:\s*color-mix\(in srgb,\s*var\(--primary-color\)\s*6%,\s*var\(--panel-bg\)\)/)
  assert.match(global, /--el-table-row-hover-bg-color:\s*color-mix\(in srgb,\s*var\(--primary-color\)\s*6%,\s*var\(--panel-bg\)\)/)
  assert.doesNotMatch(login, /rgba\(47,\s*128,\s*237,\s*0\.06\)/)
  assert.doesNotMatch(register, /rgba\(47,\s*128,\s*237,\s*0\.06\)/)
  assert.match(login, /var\(--theme-grid-x\)/)
  assert.match(login, /var\(--theme-grid-y\)/)
  assert.match(register, /var\(--theme-grid-x\)/)
  assert.match(register, /var\(--theme-grid-y\)/)
})

test('app header uses a full-width translucent sticky backdrop without top gap', () => {
  const source = read('../components/AppHeader.vue')

  assert.match(source, /\.app-header::before/)
  assert.match(source, /\.app-header::after/)
  assert.match(source, /\.app-header::before\s*\{[\s\S]*left:\s*0;[\s\S]*right:\s*0;[\s\S]*width:\s*auto;[\s\S]*pointer-events:\s*none;/)
  assert.match(source, /\.app-header::after\s*\{[\s\S]*left:\s*0;[\s\S]*right:\s*0;[\s\S]*width:\s*auto;[\s\S]*pointer-events:\s*none;/)
  assert.match(source, /backdrop-filter:\s*blur/)
  assert.match(source, /mask-image:\s*linear-gradient/)
  assert.match(source, /top:\s*0/)
  assert.doesNotMatch(source, /width:\s*100vw/)
  assert.doesNotMatch(source, /border-radius:\s*0 0 24px 24px/)
})

test('global background keeps the prototype grid visible across light and dark themes', () => {
  const theme = read('../styles/theme.css')

  assert.match(theme, /body\s*\{[\s\S]*linear-gradient\(90deg,\s*var\(--theme-grid-x\)\s*1px,\s*transparent\s*1px\)/)
  assert.match(theme, /body\s*\{[\s\S]*linear-gradient\(180deg,\s*var\(--theme-grid-y\)\s*1px,\s*transparent\s*1px\)/)
  assert.match(theme, /background-size:\s*44px 44px/)
  assert.doesNotMatch(
    theme,
    /body\s*\{[\s\S]*linear-gradient\(180deg,\s*color-mix\(in srgb,\s*var\(--panel-bg\)\s*82%,\s*transparent\),\s*var\(--bg-color\)\)/
  )
})

test('home page uses the prototype index composition instead of the old hero card layout', () => {
  const source = read('../views/Home.vue')

  assert.match(source, /class="index-hero"/)
  assert.match(source, /class="signal-panel"/)
  assert.match(source, /class="toolbar"/)
  assert.match(source, /class="content-grid"/)
  assert.match(source, /class="article-entry"/)
  assert.doesNotMatch(source, /hero-board|hero-panel|spotlight-card|content-layout/)
  assert.doesNotMatch(source, /<ArticleCard\b/)
})

test('home page exposes weekly content signal instead of reading ranking', () => {
  const source = read('../views/Home.vue')

  assert.match(source, /contentSignal/)
  assert.match(source, /本周内容信号/)
  assert.match(source, /aria-label="本周内容信号"/)
  assert.match(source, /signal-panel/)
  assert.match(source, /signal-bars/)
  assert.match(source, /buildContentSignal/)
  assert.doesNotMatch(source, /filterSummary/)
  assert.doesNotMatch(source, /本周阅读信号/)
  assert.doesNotMatch(source, /signal-strip/)
  assert.doesNotMatch(source, /signal-card/)
  assert.doesNotMatch(source, /已读/)
  assert.doesNotMatch(source, /hot|ranking|热门|排行/)
  assert.doesNotMatch(source, /featuredArticle\.viewCount/)
  assert.doesNotMatch(source, /viewCount\s*\|\|\s*0/)
  assert.match(source, /formatDate\(article\.updatedAt \|\| article\.createdAt\)/)
  assert.doesNotMatch(source, /formatDate\(article\.createdAt\)/)
})

test('article cards include focused line and reduced motion styling', () => {
  const source = read('../components/ArticleCard.vue')

  assert.match(source, /<article\b[\s\S]*class="article-card article-entry"/)
  assert.match(source, /\.article-card::before/)
  assert.match(source, /transform:\s*scaleY/)
  assert.match(source, /theme-glow-color/)
  assert.match(source, /prefers-reduced-motion:\s*reduce/)
  assert.doesNotMatch(source, /<el-card\b/)
  assert.doesNotMatch(source, /:deep\(\.el-card__body\)/)
  assert.doesNotMatch(source, /viewCount/)
  assert.doesNotMatch(source, />\s*阅读\s*\{\{\s*article\.viewCount\s*\|\|\s*0\s*\}\}\s*</)
})

test('article detail styles toc active indicator and themed resume dialog', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /\.toc-link::before/)
  assert.match(source, /\.toc-link\.active::before/)
  assert.match(source, /resume-reading-dialog/)
  assert.match(source, /theme-glow-color/)
})

test('article detail uses the reading-surface prototype skeleton', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /class="detail-head"/)
  assert.match(source, /class="detail-meta"/)
  assert.match(source, /class="reading-canvas"/)
  assert.match(source, /class="article-body"/)
  assert.match(source, /class="section-meter"/)
  assert.match(source, /class="meter-dot"/)
  assert.doesNotMatch(source, /class="article-hero"/)
  assert.doesNotMatch(source, /class="reading-summary"/)
})

test('reading space uses continuation-focused visual classes', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /reading-main::before/)
  assert.match(source, /\.continue-panel::before/)
  assert.match(source, /reading-progress-track/)
  assert.match(source, /theme-glow-color/)
})

test('reading space uses the private reading prototype skeleton', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /class="page-head"/)
  assert.match(source, /class="head-meta"/)
  assert.match(source, /class="reading-layout"/)
  assert.match(source, /class="continue-panel"/)
  assert.match(source, /class="reading-side"/)
  assert.match(source, /class="favorite-index"/)
  assert.doesNotMatch(source, /class="page-heading"/)
  assert.doesNotMatch(source, /last-read-section/)
})

test('reading history and favorites share the private index skeleton', () => {
  const history = read('../views/ReadingHistory.vue')
  const favorites = read('../views/Favorites.vue')

  assert.match(history, /class="page-head"/)
  assert.match(history, /class="head-meta"/)
  assert.match(history, /class="history-timeline"/)
  assert.match(history, /class="timeline-group"/)
  assert.doesNotMatch(history, /class="page-heading"/)

  assert.match(favorites, /class="page-head"/)
  assert.match(favorites, /class="head-meta"/)
  assert.match(favorites, /class="creator-toolbar"/)
  assert.match(favorites, /class="favorite-index"/)
  assert.match(favorites, /class="back-to-reading"/)
  assert.doesNotMatch(favorites, /class="page-heading"/)
  assert.doesNotMatch(favorites, /class="filter-toolbar"/)
  assert.doesNotMatch(favorites, /class="favorite-list"/)
})

test('reading space isolates its fixed continuation backdrop', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /\.reading-main\s*\{(?=[^}]*position:\s*relative;)(?=[^}]*isolation:\s*isolate;)[^}]*\}/)
})

test('reading space decorative continuation line ignores pointer events', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /\.continue-panel::before,[\s\S]*?pointer-events:\s*none;/)
})

test('creator article workspace uses control-console visual treatment', () => {
  const source = read('../views/creator/MyArticles.vue')

  assert.match(source, /creator-shell/)
  assert.match(source, /class="page-head"/)
  assert.match(source, /class="head-meta"/)
  assert.match(source, /class="creator-toolbar"/)
  assert.match(source, /class="searchbox"/)
  assert.match(source, /class="status-filters"/)
  assert.match(source, /class="creator-workspace"/)
  assert.match(source, /class="group-rail"/)
  assert.match(source, /class="article-flow-panel"/)
  assert.match(source, /class="creator-list"/)
  assert.match(source, /class="creator-article-card/)
  assert.match(source, /class="row-meta"/)
  assert.match(source, /class="row-actions"/)
  assert.match(source, /\.group-item::before/)
  assert.match(source, /\.head-meta\s*\{[\s\S]*border:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.head-meta\s*\{[\s\S]*box-shadow:\s*var\(--shadow-soft\)/)
  assert.match(source, /\.article-flow-panel\s*\{[\s\S]*border:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.creator-article-card::before/)
  assert.match(source, /\.creator-article-card:not\(\.is-empty-card\):hover/)
  assert.match(source, /status-pill/)
  assert.doesNotMatch(source, /class="page-header"/)
  assert.doesNotMatch(source, /class="workspace"/)
  assert.doesNotMatch(source, /class="group-panel"/)
  assert.doesNotMatch(source, /class="table-wrap"/)
  assert.doesNotMatch(source, /class="article-row"/)
  assert.doesNotMatch(source, /class="table-head"/)
  assert.doesNotMatch(source, /class="article-primary-action"/)
  assert.doesNotMatch(source, /<el-table\b/)
  assert.doesNotMatch(source, /#2f80ed/)
  assert.doesNotMatch(source, /#7c5cff/)
})

test('secondary public and auth pages no longer expose legacy visual shells', () => {
  const tagArticles = read('../views/TagArticles.vue')
  const login = read('../views/Login.vue')
  const register = read('../views/Register.vue')

  assert.match(tagArticles, /class="tag-shell"/)
  assert.match(tagArticles, /class="page-head"/)
  assert.match(tagArticles, /class="head-meta"/)
  assert.match(tagArticles, /class="article-stream"/)
  assert.doesNotMatch(tagArticles, /tag-page-head/)

  for (const source of [login, register]) {
    assert.match(source, /class="auth-shell"/)
    assert.match(source, /class="auth-panel"/)
    assert.match(source, /class="auth-index"/)
    assert.match(source, /class="eyebrow"/)
    assert.doesNotMatch(source, /<el-card\b/)
    assert.doesNotMatch(source, /auth-card/)
  }
})

test('creator writing and preview pages use the prototype surface system', () => {
  const write = read('../views/creator/ArticleWrite.vue')
  const preview = read('../views/creator/ArticlePreview.vue')

  assert.match(write, /class="compose-shell"/)
  assert.match(write, /class="page-head"/)
  assert.match(write, /class="head-meta"/)
  assert.match(write, /class="compose-grid"/)
  assert.match(write, /class="compose-panel"/)
  assert.match(write, /class="compose-rail"/)
  assert.doesNotMatch(write, /class="page-header"/)
  assert.doesNotMatch(write, /\.page-header/)

  assert.match(preview, /class="detail-head preview-head"/)
  assert.match(preview, /class="detail-meta"/)
  assert.match(preview, /class="reading-canvas/)
  assert.match(preview, /class="article-body preview-body"/)
  assert.match(preview, /class="section-meter"/)
  assert.doesNotMatch(preview, /preview-head,\s*\n\s*\.preview-body,[\s\S]*background:\s*var\(--panel-bg\)/)
})

test('primary page summaries use index rails instead of card shells', () => {
  const indexedPages = [
    read('../views/ReadingSpace.vue'),
    read('../views/ReadingHistory.vue'),
    read('../views/Favorites.vue')
  ]
  const articleDetail = read('../views/ArticleDetail.vue')

  for (const source of indexedPages) {
    assert.match(source, /\.head-meta\s*\{[\s\S]*border-left:\s*1px solid var\(--border-color\)/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*border-radius/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*box-shadow/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*background:\s*color-mix/)
  }

  assert.match(articleDetail, /\.detail-meta\s*\{[\s\S]*border-left:\s*1px solid var\(--border-color\)/)
  assert.doesNotMatch(articleDetail, /\.detail-meta\s*\{[^}]*border-radius/)
  assert.doesNotMatch(articleDetail, /\.detail-meta\s*\{[^}]*box-shadow/)
  assert.doesNotMatch(articleDetail, /\.detail-meta\s*\{[^}]*background:\s*color-mix/)
})
