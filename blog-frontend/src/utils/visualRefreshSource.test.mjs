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

test('home page exposes weekly content signal instead of reading ranking', () => {
  const source = read('../views/Home.vue')

  assert.match(source, /contentSignal/)
  assert.match(source, /本周内容信号/)
  assert.match(source, /aria-label="本周内容信号"/)
  assert.match(source, /content-signal/)
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

  assert.match(source, /\.article-card::before/)
  assert.match(source, /transform:\s*scaleY/)
  assert.match(source, /theme-glow-color/)
  assert.match(source, /prefers-reduced-motion:\s*reduce/)
})

test('article detail styles toc active indicator and themed resume dialog', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /\.toc-link::before/)
  assert.match(source, /\.toc-link\.active::before/)
  assert.match(source, /resume-reading-dialog/)
  assert.match(source, /theme-glow-color/)
})
