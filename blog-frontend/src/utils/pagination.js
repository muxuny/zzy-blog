function positiveNumber(value, fallback) {
  const number = Number(value)
  return Number.isFinite(number) && number > 0 ? number : fallback
}

function nonNegativeNumber(value, fallback = 0) {
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : fallback
}

export function normalizePageResult(result, fallbackSize = 10) {
  return {
    records: Array.isArray(result?.data) ? result.data : [],
    total: nonNegativeNumber(result?.total),
    page: positiveNumber(result?.page, 1),
    size: positiveNumber(result?.size, fallbackSize)
  }
}

export function getPageAfterSingleDeletion({ page, size, total }) {
  const currentPage = positiveNumber(page, 1)
  const pageSize = positiveNumber(size, 1)
  const nextTotal = Math.max(0, nonNegativeNumber(total) - 1)
  const maxPage = Math.max(1, Math.ceil(nextTotal / pageSize))
  return Math.min(currentPage, maxPage)
}

export function shouldShowPagination(total, size) {
  return nonNegativeNumber(total) > positiveNumber(size, 1)
}
