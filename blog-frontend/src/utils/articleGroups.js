export const GROUP_FILTER_ALL = 'all'
export const GROUP_FILTER_UNGROUPED = 'ungrouped'
const GROUP_FILTER_PREFIX = 'group:'

export function createGroupFilterKey(groupId) {
  return `${GROUP_FILTER_PREFIX}${groupId}`
}

export function parseGroupFilterKey(filterKey) {
  if (!filterKey || filterKey === GROUP_FILTER_ALL) {
    return { type: GROUP_FILTER_ALL }
  }
  if (filterKey === GROUP_FILTER_UNGROUPED) {
    return { type: GROUP_FILTER_UNGROUPED }
  }
  if (String(filterKey).startsWith(GROUP_FILTER_PREFIX)) {
    const groupId = String(filterKey).slice(GROUP_FILTER_PREFIX.length)
    if (!groupId) {
      return { type: GROUP_FILTER_ALL }
    }
    return { type: 'group', groupId }
  }
  return { type: GROUP_FILTER_ALL }
}

export function buildMyArticleGroupParams(baseParams, filterKey) {
  const params = { ...baseParams }
  const parsed = parseGroupFilterKey(filterKey)
  if (parsed.type === GROUP_FILTER_UNGROUPED) {
    params.ungrouped = true
  }
  if (parsed.type === 'group') {
    params.groupId = parsed.groupId
  }
  return params
}

export function buildArticleGroupIds(selectedGroupId) {
  if (selectedGroupId === '' || selectedGroupId === null || selectedGroupId === undefined) {
    return []
  }
  return [selectedGroupId]
}

export function buildArticleGroupIdsForSave(selectedGroupId, existingGroups = []) {
  const selectedIds = buildArticleGroupIds(selectedGroupId)
  if (!selectedIds.length) {
    return []
  }
  const existingIds = Array.isArray(existingGroups)
    ? existingGroups.map(group => group?.id).filter(id => id !== null && id !== undefined)
    : []
  const [, ...hiddenExistingIds] = existingIds
  return Array.from(new Set([...selectedIds, ...hiddenExistingIds]))
}

export function getFirstArticleGroupId(groups) {
  if (!Array.isArray(groups) || !groups.length) {
    return ''
  }
  return groups[0]?.id ?? ''
}

export function formatArticleGroupNames(groups) {
  if (!Array.isArray(groups) || !groups.length) {
    return '未分组'
  }
  return groups.map(group => group.name).filter(Boolean).join(' / ') || '未分组'
}
