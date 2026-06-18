import fs from 'node:fs'
import path from 'node:path'

const cssPath = path.resolve('src/styles/global.css')
const css = fs.readFileSync(cssPath, 'utf8')

const requiredSelectors = [
  '[data-theme="dark"] .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell',
  '[data-theme="dark"] .el-table__body tr.hover-row > td.el-table__cell'
]

for (const selector of requiredSelectors) {
  if (!css.includes(selector)) {
    throw new Error(`Missing dark table selector: ${selector}`)
  }
}

console.log('dark table theme selectors verified')
