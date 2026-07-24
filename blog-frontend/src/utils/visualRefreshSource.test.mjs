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

test('app header brand mark uses the visual refresh wordmark system', () => {
  const source = read('../components/AppHeader.vue')

  assert.match(source, /class="logo-copy"/)
  assert.match(source, /class="logo-kicker"/)
  assert.match(source, /\.logo::before/)
  assert.match(source, /\.logo-mark::after/)
  assert.match(source, /\.logo-text\s*\{[^}]*font-family:\s*Georgia/)
  assert.match(source, /\.logo-mark\s*\{[^}]*background:\s*color-mix\(in srgb,\s*var\(--primary-color\)\s*10%,\s*var\(--panel-bg\)\)/)
  assert.match(source, /\.logo-kicker\s*\{[^}]*color:\s*var\(--muted-text-color\)/)
  assert.doesNotMatch(source, /<span class="logo-mark">B<\/span>/)
  assert.doesNotMatch(source, /\.logo-mark\s*\{[^}]*background:\s*var\(--primary-color\)/)
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
  assert.match(source, /class="detail-actions"/)
  assert.match(source, /class="detail-meta"/)
  assert.match(source, /class="reading-primary"/)
  assert.match(source, /class="reading-canvas"/)
  assert.match(source, /class="article-body"/)
  assert.doesNotMatch(source, /class="article-hero"/)
  assert.doesNotMatch(source, /class="reading-summary"/)
  assert.doesNotMatch(source, /class="detail-side"/)
  assert.doesNotMatch(source, /class="section-meter"/)
  assert.doesNotMatch(source, /class="meter-dot"/)
})

test('article detail gives the article body a quiet primary reading surface', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /\.article-body\s*\{[^}]*min-height:\s*clamp\(360px,\s*42vh,\s*560px\);/)
  assert.match(source, /\.article-body\s*\{[^}]*border:\s*1px solid var\(--border-color\);/)
  assert.match(source, /\.article-body\s*\{[^}]*border-radius:\s*var\(--radius-md\);/)
  assert.match(source, /\.article-body\s*\{[^}]*background:\s*color-mix\(in srgb,\s*var\(--panel-bg\)\s*92%,\s*transparent\);/)
  assert.match(source, /\.article-body\s*\{[^}]*box-shadow:\s*0 18px 46px color-mix\(in srgb,\s*var\(--text-color\)\s*7%,\s*transparent\);/)
  assert.match(source, /\.note-panel\s*\{[^}]*background:\s*transparent;/)
  assert.match(source, /\.note-panel\s*\{[^}]*box-shadow:\s*none;/)
  assert.doesNotMatch(source, /\.article-body\s*\{[^}]*border-top:/)
  assert.doesNotMatch(source, /\.article-body\s*\{[^}]*border-bottom:/)
})

test('article detail hero follows the open prototype briefing layout', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /<div class="reading-canvas">[\s\S]*?<div class="reading-primary">[\s\S]*?<header class="detail-head"[\s\S]*?<\/header>[\s\S]*?<main class="article-body">[\s\S]*?<\/main>[\s\S]*?<\/div>[\s\S]*?<aside class="reading-sidebar">/)
  assert.match(source, /\.detail-head\s*\{[^}]*position:\s*relative;/)
  assert.match(source, /\.detail-head\s*\{[^}]*margin-bottom:\s*30px;/)
  assert.match(source, /\.detail-head\s*\{[^}]*padding-bottom:\s*24px;/)
  assert.match(source, /\.detail-head\s*\{[^}]*border-bottom:\s*1px solid color-mix\(in srgb,\s*var\(--border-color\)\s*72%,\s*transparent\);/)
  assert.match(source, /\.detail-head\s*\{[^}]*background:\s*transparent;/)
  assert.match(source, /\.detail-head\s*\{[^}]*box-shadow:\s*none;/)
  assert.match(source, /\.detail-actions\s*\{[^}]*display:\s*flex;/)
  assert.match(source, /\.detail-actions\s*\{[^}]*margin-top:\s*20px;/)
  assert.doesNotMatch(source, /\.detail-actions\s*\{[^}]*border-bottom:/)
  assert.match(source, /\.favorite-button\s*\{[^}]*background:\s*color-mix\(in srgb,\s*var\(--panel-bg\)\s*78%,\s*transparent\);/)
  assert.doesNotMatch(source, /\.detail-head::before/)
  assert.doesNotMatch(source, /\.detail-head::after/)
  assert.doesNotMatch(source, /\.detail-head\s*\{[^}]*border-radius:/)
})

test('article detail summary reads as inline metadata instead of a card', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /class="detail-meta"[\s\S]*?字数 \{\{\s*readingStats\.wordCount\s*\}\}/)
  assert.match(source, /\.detail-meta\s*\{[^}]*display:\s*flex;/)
  assert.match(source, /\.detail-meta\s*\{[^}]*flex-wrap:\s*wrap;/)
  assert.match(source, /\.detail-meta\s*\{[^}]*gap:\s*8px 14px;/)
  assert.match(source, /\.detail-meta\s*\{[^}]*font-size:\s*13px;/)
  assert.doesNotMatch(source, /aria-label="文章阅读摘要"/)
  assert.doesNotMatch(source, /class="meta-line"/)
  assert.doesNotMatch(source, /\.detail-meta::before/)
  assert.doesNotMatch(source, /\.detail-meta\s*\{[^}]*border-radius:/)
  assert.doesNotMatch(source, /\.detail-meta\s*\{[^}]*border-left:/)
})

test('article detail keeps the article summary as a quiet title-area deck', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /<h1>\{\{\s*article\.title\s*\}\}<\/h1>[\s\S]*?<p v-if="article\.summary" class="detail-summary">\{\{\s*article\.summary\s*\}\}<\/p>[\s\S]*?<div class="detail-meta">/)
  assert.match(source, /\.detail-summary\s*\{[^}]*max-width:\s*680px;/)
  assert.match(source, /\.detail-summary\s*\{[^}]*margin:\s*0 0 20px;/)
  assert.match(source, /\.detail-summary\s*\{[^}]*font-size:\s*clamp\(16px,\s*1\.7vw,\s*18px\);/)
  assert.match(source, /\.detail-summary\s*\{[^}]*line-height:\s*1\.75;/)
  assert.doesNotMatch(source, /\.detail-summary\s*\{[^}]*background:/)
  assert.doesNotMatch(source, /\.detail-summary\s*\{[^}]*border:/)
  assert.doesNotMatch(source, /\.detail-summary\s*\{[^}]*border-radius:/)
})

test('article detail toc uses the prototype rail instead of card panels', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /\.reading-sidebar\s*\{[^}]*border-left:\s*1px solid color-mix\(in srgb,\s*var\(--border-color\)\s*72%,\s*transparent\);/)
  assert.match(source, /\.reading-sidebar\s*\{[^}]*padding-left:\s*18px;/)
  assert.match(source, /\.toc-panel\s*\{[^}]*background:\s*transparent;/)
  assert.match(source, /\.toc-panel\s*\{[^}]*box-shadow:\s*none;/)
  assert.match(source, /\.toc-link\.active\s*\{[^}]*background:\s*transparent;/)
  assert.doesNotMatch(source, /\.toc-panel\s*\{[^}]*border:\s*1px/)
  assert.doesNotMatch(source, /\.section-meter\b/)
  assert.doesNotMatch(source, /\.meter-dot\b/)
})

test('article detail continuation reads as an article epilogue instead of cards', () => {
  const source = read('../views/ArticleDetail.vue')

  assert.match(source, /class="article-epilogue"/)
  assert.match(source, /class="neighbor-direction"/)
  assert.match(source, /class="neighbor-link previous"/)
  assert.match(source, /class="neighbor-link next"/)
  assert.match(source, /class="related-index"/)
  assert.match(source, /class="related-link"/)
  assert.match(source, /\.article-epilogue\s*\{[^}]*background:\s*linear-gradient\(/)
  assert.match(source, /\.article-epilogue\s*\{[^}]*border-top:\s*1px solid color-mix\(in srgb,\s*var\(--border-color\)\s*70%,\s*transparent\);/)
  assert.match(source, /\.neighbor-direction\s*\{[^}]*font-family:\s*var\(--font-mono\);/)
  assert.match(source, /\.neighbor-link,\s*\.related-link\s*\{[^}]*border:\s*0;/)
  assert.match(source, /\.neighbor-link,\s*\.related-link\s*\{[^}]*border-radius:\s*var\(--radius-sm\);/)
  assert.match(source, /\.neighbor-link,\s*\.related-link\s*\{[^}]*background:\s*transparent;/)
  assert.match(source, /\.neighbor-link,\s*\.related-link\s*\{[^}]*box-shadow:\s*none;/)
  assert.match(source, /\.related-index\s*\{[^}]*font-family:\s*var\(--font-mono\);/)
  assert.match(source, /\.related-list\s*\{[^}]*border-top:\s*1px solid color-mix\(in srgb,\s*var\(--border-color\)\s*58%,\s*transparent\);/)
  assert.doesNotMatch(source, /class="neighbor-card/)
  assert.doesNotMatch(source, /class="related-card/)
  assert.doesNotMatch(source, /\.neighbor-card\b/)
  assert.doesNotMatch(source, /\.related-card\b/)
})

test('reading space uses continuation-focused visual classes', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /reading-main::before/)
  assert.match(source, /\.continue-panel::after/)
  assert.match(source, /progress-track/)
  assert.match(source, /theme-glow-color/)
  assert.doesNotMatch(source, /reading-progress-track/)
})

test('reading space turns the continuation blank area into an interactive liquid light pool', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /class="liquid-playground"/)
  assert.match(source, /class="liquid-surface"/)
  assert.match(source, /class="liquid-ripple"/)
  assert.match(source, /class="liquid-glow/)
  assert.match(source, /class="liquid-current/)
  assert.match(source, /@pointermove="updateLiquidPool"/)
  assert.match(source, /@pointerleave="resetLiquidPool"/)
  assert.match(source, /@pointerdown="stirLiquidPool"/)
  assert.match(source, /@pointerup="releaseLiquidPool"/)
  assert.match(source, /--pool-cursor-x/)
  assert.match(source, /--pool-flow-x/)
  assert.match(source, /\.liquid-playground\s*\{[^}]*bottom:\s*20px;[^}]*height:\s*118px;[^}]*pointer-events:\s*none;/)
  assert.match(source, /\.liquid-playground::before\s*\{[^}]*animation:\s*liquidFlow/)
  assert.match(source, /\.liquid-playground::after\s*\{[^}]*animation:\s*liquidTide/)
  assert.match(source, /\.liquid-glow\s*\{[^}]*animation:\s*liquidWander/)
  assert.match(source, /\.continue-panel\.is-pool-rippling\s+\.liquid-ripple\s*\{[^}]*animation:\s*liquidRipple/)
  assert.match(source, /\.continue-panel\.is-pool-stirring\s+\.liquid-current\s*\{[^}]*animation-duration:\s*1\.8s;/)
  assert.match(source, /@keyframes liquidFlow/)
  assert.match(source, /@keyframes liquidTide/)
  assert.match(source, /@keyframes liquidWander/)
  assert.match(source, /@keyframes liquidRipple/)
  assert.match(source, /@media \(prefers-reduced-motion: reduce\)[\s\S]*\.liquid-playground::before,\s*\.liquid-playground::after,\s*\.liquid-surface,\s*\.liquid-glow,\s*\.liquid-current,\s*\.liquid-ripple[\s\S]*?\{[^}]*animation:\s*none;/)
  assert.doesNotMatch(source, /magnetic-field/)
  assert.doesNotMatch(source, /magnetic-particle/)
  assert.doesNotMatch(source, /fieldBreath/)
  assert.doesNotMatch(source, /magneticFloat/)
  assert.doesNotMatch(source, /reading-pulse/)
  assert.doesNotMatch(source, /pulse-track/)
  assert.doesNotMatch(source, /--pulse-progress/)
  assert.doesNotMatch(source, /overview\.historyTotal[\s\S]{0,700}liquid-playground/)
  assert.doesNotMatch(source, /overview\.favoriteTotal[\s\S]{0,700}liquid-playground/)
})

test('reading space uses the private reading prototype skeleton', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /class="page-head"/)
  assert.match(source, /class="head-meta"/)
  assert.match(source, /Reading desk/)
  assert.match(source, /我的阅读更像一个安静的续接台/)
  assert.match(source, /class="reading-layout"/)
  assert.match(source, /class="continue-panel"/)
  assert.match(source, /class="progress-block"/)
  assert.match(source, /class="progress-label"/)
  assert.match(source, /class="progress-track"/)
  assert.match(source, /class="continue-actions"/)
  assert.match(source, /to="\/reading\/history"/)
  assert.match(source, /class="reading-side"/)
  assert.match(source, /class="timeline"/)
  assert.match(source, /class="timeline-item/)
  assert.match(source, /class="favorite-index"/)
  assert.match(source, /\.head-meta\s*\{[^}]*background:\s*var\(--panel-bg\)/)
  assert.match(source, /\.head-meta\s*\{[^}]*border:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.head-meta\s*\{[^}]*border-radius:\s*var\(--radius-md\)/)
  assert.match(source, /\.head-meta\s*\{[^}]*display:\s*grid;/)
  assert.match(source, /\.meta-line\s*\{[^}]*grid-template-columns:\s*minmax\(0,\s*1fr\) auto;/)
  assert.match(source, /\.meta-label\s*\{[^}]*color:\s*var\(--muted-text-color\)/)
  assert.match(source, /\.meta-value\s*\{[^}]*font-size:\s*18px;/)
  assert.match(source, /\.meta-value\s*\{[^}]*font-weight:\s*800;/)
  assert.match(source, /\.meta-value\s*\{[^}]*text-align:\s*right;/)
  assert.doesNotMatch(source, /\.meta-line\s*\{[^}]*border-bottom:/)
  assert.match(source, /\.reading-side\s*\{[\s\S]*border:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.timeline\s*\{[\s\S]*border-left:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.timeline-item::before/)
  assert.match(source, /\.favorite-index\s*\{[\s\S]*border-top:\s*1px solid var\(--border-color\)/)
  assert.match(source, /\.favorite-line\s*\{[\s\S]*padding:\s*14px 0/)
  assert.match(source, /@media\s*\(max-width:\s*980px\)[\s\S]*?\.page-head,\s*\.reading-layout\s*\{[\s\S]*?grid-template-columns:\s*minmax\(0,\s*1fr\);/)
  assert.match(source, /@media\s*\(max-width:\s*480px\)[\s\S]*?\.meta-line\s*\{[\s\S]*?grid-template-columns:\s*1fr;/)
  assert.doesNotMatch(source, /class="page-heading"/)
  assert.doesNotMatch(source, /class="section-heading"/)
  assert.doesNotMatch(source, /last-read-section/)
  assert.doesNotMatch(source, /class="continue-cover"/)
  assert.doesNotMatch(source, /class="favorite-cover"/)
  assert.doesNotMatch(source, /class="reading-section"/)
  assert.doesNotMatch(source, /continue-panel::before/)
  assert.doesNotMatch(source, /favorite-line::before/)
})

test('reading history and favorites share the private index skeleton', () => {
  const history = read('../views/ReadingHistory.vue')
  const favorites = read('../views/Favorites.vue')

  assert.match(history, /class="page-head"/)
  assert.match(history, /class="head-meta"/)
  assert.match(history, /class="history-timeline"/)
  assert.match(history, /class="timeline-group"/)
  assert.match(history, /class="clear-button"/)
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

test('reading history summary uses a solid action card', () => {
  const history = read('../views/ReadingHistory.vue')

  assert.match(history, /\.head-meta\s*\{[^}]*display:\s*grid;/)
  assert.match(history, /\.head-meta\s*\{[^}]*background:\s*var\(--panel-bg\)/)
  assert.match(history, /\.head-meta\s*\{[^}]*border:\s*1px solid var\(--border-color\)/)
  assert.match(history, /\.head-meta\s*\{[^}]*border-radius:\s*var\(--radius-md\)/)
  assert.match(history, /\.head-meta\s*\{[^}]*box-shadow:\s*var\(--shadow-soft\)/)
  assert.match(history, /\.meta-line\s*\{[^}]*grid-template-columns:\s*minmax\(0,\s*1fr\) auto;/)
  assert.match(history, /\.meta-label\s*\{[^}]*color:\s*var\(--muted-text-color\)/)
  assert.match(history, /\.meta-value\s*\{[^}]*font-size:\s*18px;/)
  assert.match(history, /\.meta-value\s*\{[^}]*font-weight:\s*800;/)
  assert.match(history, /\.clear-button\s*\{[^}]*margin-top:\s*6px;/)
  assert.doesNotMatch(history, /\.head-meta\s*\{[^}]*border-left:/)
  assert.doesNotMatch(history, /\.meta-line\s*\{[^}]*border-bottom:/)
})

test('reading space isolates its fixed continuation backdrop', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /\.reading-main\s*\{(?=[^}]*position:\s*relative;)(?=[^}]*isolation:\s*isolate;)[^}]*\}/)
})

test('reading space decorative continuation line ignores pointer events', () => {
  const source = read('../views/ReadingSpace.vue')

  assert.match(source, /\.continue-panel::after\s*\{[\s\S]*?pointer-events:\s*none;/)
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
    read('../views/Favorites.vue')
  ]
  const readingSpace = read('../views/ReadingSpace.vue')
  const readingHistory = read('../views/ReadingHistory.vue')

  for (const source of indexedPages) {
    assert.match(source, /\.head-meta\s*\{[\s\S]*border-left:\s*1px solid var\(--border-color\)/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*border-radius/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*box-shadow/)
    assert.doesNotMatch(source, /\.head-meta\s*\{[^}]*background:\s*color-mix/)
  }

  assert.match(readingSpace, /\.head-meta\s*\{[^}]*background:\s*var\(--panel-bg\)/)
  assert.match(readingSpace, /\.head-meta\s*\{[^}]*border-radius:\s*var\(--radius-md\)/)
  assert.match(readingSpace, /\.head-meta\s*\{[^}]*box-shadow:\s*var\(--shadow-soft\)/)
  assert.match(readingHistory, /\.head-meta\s*\{[^}]*background:\s*var\(--panel-bg\)/)
  assert.match(readingHistory, /\.head-meta\s*\{[^}]*border-radius:\s*var\(--radius-md\)/)
  assert.match(readingHistory, /\.head-meta\s*\{[^}]*box-shadow:\s*var\(--shadow-soft\)/)
})
