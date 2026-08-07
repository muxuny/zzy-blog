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
  assert.match(source, /\.chart-bars\s*\{[^}]*grid-template-columns:\s*repeat\(14,\s*minmax\(0,\s*1fr\)\)/)
  assert.match(source, /\.chart-bars span\s*\{[^}]*min-height:\s*16px;/)
  assert.doesNotMatch(source, /\.chart-bars\s*\{[^}]*grid-template-columns:\s*repeat\(30/)
})
