export function getHeadingLevelFromNode(node) {
  if (node?.type !== 'heading') return 0
  const level = Number(node?.attrs?.level)
  return [2, 3, 4].includes(level) ? level : 0
}

export function shouldAppendParagraphAfterCodeBlock(currentNode, nextNode) {
  return currentNode?.type?.name === 'codeBlock' && !nextNode
}
