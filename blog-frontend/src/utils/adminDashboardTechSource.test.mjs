import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const srcRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

function read(relativePath) {
  return fs.readFileSync(path.resolve(srcRoot, relativePath), 'utf8')
}

test('admin dashboard exposes the tech control monitoring panels', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /class="tech-board"/)
  assert.match(source, /class="signal-row"/)
  assert.equal(source.match(/class="signal-card(?: is-actionable)?"/g)?.length, 5)
  assert.equal(source.match(/class="dash-panel [^"]+-panel"/g)?.length, 6)
  assert.match(source, /class="dashboard-core"/)
  assert.match(source, /class="lower-grid"/)
  assert.match(source, /class="[^"]*content-structure-panel"/)
  assert.match(source, /class="[^"]*reading-activity-panel"/)
  assert.match(source, /class="[^"]*heat-panel"/)
  assert.match(source, /class="heat-summary"/)
  assert.match(source, /class="[^"]*favorite-feedback-panel"/)
  assert.match(source, /class="[^"]*resource-health-panel"/)
  assert.match(source, /class="[^"]*priority-queue-panel"/)
  assert.match(source, /getAdminDashboardOverview/)
  assert.doesNotMatch(source, /getAdminArticles/)
  assert.doesNotMatch(source, /getUsers/)
  assert.doesNotMatch(source, /getTags/)
})

test('admin dashboard palette derives from theme tokens instead of fixed light colors', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /--dash-steel:\s*var\(--accent-color\);/)
  assert.match(source, /--dash-teal:\s*color-mix\(in srgb,\s*var\(--primary-color\)/)
  assert.match(source, /--dash-leaf:\s*var\(--primary-color\);/)
  assert.match(source, /--dash-amber:\s*var\(--warning-color\);/)
  assert.match(source, /--dash-rose:\s*var\(--danger-color\);/)
  assert.match(source, /--dash-plum:\s*color-mix\(in srgb,\s*var\(--accent-color\)/)
  assert.doesNotMatch(source, /--dash-steel:\s*#/)
  assert.doesNotMatch(source, /--dash-teal:\s*#/)
  assert.doesNotMatch(source, /--dash-amber:\s*#/)
  assert.doesNotMatch(source, /--dash-rose:\s*#/)
  assert.doesNotMatch(source, /--dash-plum:\s*#/)
  assert.doesNotMatch(source, /linear-gradient\(180deg,\s*#f8fafc/)
  assert.doesNotMatch(source, /rgba\(255,\s*255,\s*255/)
  assert.doesNotMatch(source, /rgba\(47,\s*65,\s*88/)
})

test('admin dashboard grids adapt to the admin container width', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /\.tech-board\s*\{[^}]*container-type:\s*inline-size;/)
  assert.match(source, /\.dashboard-core\s*\{[^}]*grid-template-columns:\s*minmax\(0,\s*1\.0[0-9]fr\)/)
  assert.doesNotMatch(source, /grid-template-columns:\s*minmax\(230px/)
  assert.match(source, /@container\s*\(max-width:\s*840px\)/)
  assert.doesNotMatch(source, /@container\s*\(max-width:\s*1180px\)/)
})

test('admin dashboard reading chart follows the prototype bar rhythm', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /sampleRows/)
  assert.match(source, /\.chart-bars\s*\{[^}]*grid-template-columns:\s*repeat\(var\(--bar-count\),\s*minmax\(0,\s*1fr\)\)/)
  assert.match(source, /\.chart-bars \.bar\s*\{[^}]*min-height:\s*16px;/)
  assert.doesNotMatch(source, /\.chart-bars\s*\{[^}]*grid-template-columns:\s*repeat\(14,\s*minmax\(0,\s*1fr\)\)/)
  assert.doesNotMatch(source, /\.chart-bars\s*\{[^}]*grid-template-columns:\s*repeat\(30/)
})

test('admin dashboard reading chart uses per-bar hover tooltips instead of a fixed peak note', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.doesNotMatch(source, /peak-note/)
  assert.doesNotMatch(source, /readingPeak/)
  assert.match(source, /bar-cell/)
  assert.match(source, /bar-tip/)
  assert.match(source, /\.bar-cell:hover \.bar-tip/)
  assert.match(source, /\.bar-cell:focus-visible \.bar-tip/)
  assert.match(source, /\.chart-box\s*\{[^}]*overflow:\s*visible;/)
})

test('admin dashboard reading chart does not keep the decorative trend line', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.doesNotMatch(source, /trend-line/)
})

test('admin dashboard favorite sparkline shows the empty state when every daily count is zero', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /hasFavoriteTrend/)
  assert.match(source, /favoriteSpark\.value\.some\(item => item\.count > 0\)/)
  assert.match(source, /v-if="hasFavoriteTrend"/)
})

test('admin dashboard signal cards expose shortcut navigation without changing card inventory', () => {
  const source = read('views/admin/Dashboard.vue')
  const r04Card = source.match(
    /<section class="signal-card">[\s\S]*?<span class="signal-code">R-04<\/span>[\s\S]*?<\/section>/
  )

  assert.match(source, /<button\s+class="signal-card is-actionable"\s+type="button"\s+@click="goAdminArticles"/)
  assert.match(source, /<button\s+class="signal-card is-actionable"\s+type="button"\s+@click="goPendingQueue"/)
  assert.match(source, /<button\s+class="signal-card is-actionable"\s+type="button"\s+@click="goPublishedPublicArticles"/)
  assert.match(source, /<button\s+class="signal-card is-actionable"\s+type="button"\s+@click="goResources"/)
  assert.ok(r04Card)
  assert.doesNotMatch(r04Card[0], /is-actionable|@click/)
})

test('admin dashboard route helpers target existing admin routes and pending queues', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /function goAdminArticles\(\)\s*\{[\s\S]*router\.push\(\{\s*path:\s*'\/admin\/articles'\s*\}\)/)
  assert.match(source, /function goPendingQueue\(\)\s*\{[\s\S]*stats\.value\.metrics\.pendingArticles > 0 \|\| stats\.value\.metrics\.pendingUsers === 0/)
  assert.match(source, /router\.push\(\{\s*path:\s*'\/admin\/articles',\s*query:\s*\{\s*status:\s*'pending'\s*\}\s*\}\)/)
  assert.match(source, /router\.push\(\{\s*path:\s*'\/admin\/users',\s*query:\s*\{\s*status:\s*'pending'\s*\}\s*\}\)/)
  assert.match(source, /function goPublishedPublicArticles\(\)\s*\{[\s\S]*router\.push\(\{\s*path:\s*'\/admin\/articles',\s*query:\s*\{\s*status:\s*'published',\s*visibility:\s*'public'\s*\}\s*\}\)/)
  assert.match(source, /function goResources\(\)\s*\{[\s\S]*router\.push\(\{\s*path:\s*'\/admin\/resources'\s*\}\)/)
  assert.match(source, /function goArticle\(article\)\s*\{[\s\S]*if \(!article\?\.id\) return[\s\S]*router\.push\(`\/admin\/articles\/edit\/\$\{article\.id\}`\)/)
  assert.match(source, /function goUsers\(\)\s*\{[\s\S]*router\.push\(\{\s*path:\s*'\/admin\/users',\s*query:\s*\{\s*status:\s*'pending'\s*\}\s*\}\)/)
})

test('admin dashboard reading range switch uses existing daily reads only', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /const readingRange = ref\(14\)/)
  assert.match(source, /const readingRangeOptions = \[[\s\S]*\{ value: 7, label: '7 天' \}[\s\S]*\{ value: 14, label: '14 天' \}[\s\S]*\{ value: 30, label: '30 天' \}[\s\S]*\]/)
  assert.match(source, /class="reading-head-tools"/)
  assert.match(source, /class="range-tabs"\s+aria-label="阅读范围"/)
  assert.match(source, /v-for="option in readingRangeOptions"/)
  assert.match(source, /:class="\{ 'is-active': readingRange === option\.value \}"/)
  assert.match(source, /:aria-pressed="readingRange === option\.value"/)
  assert.match(source, /:aria-label="`查看近 \$\{option\.value\} 天阅读记录`"/)
  assert.match(source, /@click="readingRange = option\.value"/)
  assert.match(source, /stats\.value\.readingSummary\.dailyReads\.slice\(-readingRange\.value\)/)
  assert.match(source, /:style="\{ '--bar-count': sampleRows\.length \|\| readingRange \}"/)
  assert.doesNotMatch(source, /dailyReads\.slice\(-14\)/)
})

test('admin dashboard reading and favorite bars use custom focusable tooltips', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /class="bar-cell"[\s\S]*tabindex="0"[\s\S]*:aria-label="`\$\{bar\.date\}：\$\{bar\.count\} 次阅读`"/)
  assert.match(source, /class="spark-cell"[\s\S]*tabindex="0"[\s\S]*:aria-label="`\$\{item\.date\}：\$\{item\.count\} 次收藏`"/)
  assert.match(source, /class="spark-bar"[\s\S]*<span class="spark-tip" aria-hidden="true">/)
  assert.match(source, /\.spark-cell:hover \.spark-tip/)
  assert.match(source, /\.spark-cell:focus-visible \.spark-tip/)
  assert.doesNotMatch(source, /:title="`\$\{bar\.date\}：\$\{bar\.count\}/)
  assert.doesNotMatch(source, /:title="`\$\{item\.date\}：\$\{item\.count\}/)
})

test('admin dashboard heat rows route only when the article id exists', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /<button[\s\S]*v-if="item\.id"[\s\S]*class="rank-item is-actionable"[\s\S]*type="button"[\s\S]*@click="goArticle\(item\)"/)
  assert.match(source, /<div[\s\S]*v-else[\s\S]*class="rank-item"/)
  assert.match(source, /function goArticle\(article\)\s*\{[\s\S]*if \(!article\?\.id\) return/)
})

test('admin dashboard passive legends and queue items have keyboard-friendly feedback contracts', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /class="legend-row"[\s\S]*tabindex="0"[\s\S]*:aria-label="`\$\{item\.label\}：\$\{item\.percent\}%`"/)
  assert.match(source, /class="legend-row"[\s\S]*tabindex="0"[\s\S]*:aria-label="`公开文章/)
  assert.match(source, /\.legend-row:hover/)
  assert.match(source, /\.legend-row:focus-visible/)
  assert.match(source, /\.queue-item:hover/)
  assert.match(source, /\.queue-item:focus-visible/)
  assert.match(source, /@media \(prefers-reduced-motion: reduce\)\s*\{[\s\S]*transform:\s*none;/)
})

test('admin dashboard interaction styles stay theme-token based', () => {
  const source = read('views/admin/Dashboard.vue')

  assert.match(source, /button\.signal-card/)
  assert.match(source, /\.is-actionable:hover/)
  assert.match(source, /\.is-actionable:focus-visible/)
  assert.match(source, /\.range-tabs button:hover/)
  assert.match(source, /\.range-tabs button:focus-visible/)
  assert.doesNotMatch(source, /#[0-9a-fA-F]{3,8}\b/)
  assert.doesNotMatch(source, /rgba\(255,\s*255,\s*255/)
})
