import test from 'node:test'
import assert from 'node:assert/strict'
import {
  getHeadingLevelFromNode,
  shouldAppendParagraphAfterCodeBlock
} from './editorState.js'

test('getHeadingLevelFromNode maps heading nodes to their level', () => {
  assert.equal(getHeadingLevelFromNode({ type: 'heading', attrs: { level: 3 } }), 3)
})

test('getHeadingLevelFromNode maps paragraphs, unsupported headings, and unknown nodes to normal paragraph', () => {
  assert.equal(getHeadingLevelFromNode({ type: 'paragraph' }), 0)
  assert.equal(getHeadingLevelFromNode({ type: 'heading', attrs: { level: 1 } }), 0)
  assert.equal(getHeadingLevelFromNode({ type: 'heading', attrs: { level: 5 } }), 0)
  assert.equal(getHeadingLevelFromNode(null), 0)
  assert.equal(getHeadingLevelFromNode({ type: 'codeBlock' }), 0)
})

test('shouldAppendParagraphAfterCodeBlock detects trailing code blocks without a following paragraph', () => {
  const codeBlock = { type: { name: 'codeBlock' } }
  const paragraph = { type: { name: 'paragraph' } }

  assert.equal(shouldAppendParagraphAfterCodeBlock(codeBlock, null), true)
  assert.equal(shouldAppendParagraphAfterCodeBlock(codeBlock, paragraph), false)
  assert.equal(shouldAppendParagraphAfterCodeBlock(paragraph, null), false)
})
