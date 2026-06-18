import { shouldUseHistoryBack } from '../src/utils/navigation.js'

const cases = [
  [{ back: '/tag/前端' }, true],
  [{ current: '/article/1' }, false],
  [null, false],
  [undefined, false]
]

for (const [state, expected] of cases) {
  const actual = shouldUseHistoryBack(state)
  if (actual !== expected) {
    throw new Error(`Expected ${expected}, got ${actual} for ${JSON.stringify(state)}`)
  }
}

console.log('article back navigation rules verified')
