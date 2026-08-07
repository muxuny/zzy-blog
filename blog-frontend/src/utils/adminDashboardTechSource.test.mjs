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
