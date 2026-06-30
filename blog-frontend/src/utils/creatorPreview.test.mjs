import test from 'node:test'
import assert from 'node:assert/strict'
import { getCreatorEditRoute, getCreatorPreviewRoute } from './creatorPreview.js'

test('getCreatorPreviewRoute always routes my article title clicks to creator preview', () => {
  assert.equal(getCreatorPreviewRoute({ id: 12, status: 'draft' }), '/creator/articles/preview/12')
  assert.equal(getCreatorPreviewRoute({ id: 13, status: 'published', visibility: 'public' }), '/creator/articles/preview/13')
  assert.equal(getCreatorPreviewRoute({ id: 14, status: 'rejected' }), '/creator/articles/preview/14')
})

test('getCreatorPreviewRoute returns empty string when article id is missing', () => {
  assert.equal(getCreatorPreviewRoute({ title: 'Untitled' }), '')
  assert.equal(getCreatorPreviewRoute(null), '')
})

test('getCreatorEditRoute routes preview edit action to the creator editor', () => {
  assert.equal(getCreatorEditRoute({ id: 12 }), '/creator/articles/edit/12')
  assert.equal(getCreatorEditRoute({ id: 13, status: 'rejected' }), '/creator/articles/edit/13')
})

test('getCreatorEditRoute returns empty string when article id is missing', () => {
  assert.equal(getCreatorEditRoute({ title: 'Untitled' }), '')
  assert.equal(getCreatorEditRoute(null), '')
})
