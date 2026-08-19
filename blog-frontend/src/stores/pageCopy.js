import { defineStore } from 'pinia'
import {
  getAdminPageCopies,
  getPageCopies,
  resetAdminPageCopies,
  updateAdminPageCopy
} from '../api/pageCopy'
import {
  PAGE_COPY_DEFAULTS,
  groupPageCopies,
  mergePageCopies,
  resolvePageCopy
} from '../utils/pageCopy'

export const usePageCopyStore = defineStore('pageCopy', {
  state: () => ({
    copies: mergePageCopies(),
    adminCopies: mergePageCopies(),
    publicLoaded: false,
    adminLoaded: false,
    loading: false,
    adminLoading: false
  }),
  getters: {
    groupedAdminCopies(state) {
      return groupPageCopies(state.adminCopies)
    }
  },
  actions: {
    resolveCopy(copyKey, params = {}) {
      return resolvePageCopy(copyKey, params, this.copies)
    },
    resolveAdminCopy(copyKey, params = {}) {
      return resolvePageCopy(copyKey, params, this.adminCopies)
    },
    async loadPublicCopies(options = {}) {
      if (this.publicLoaded && !options.force) return this.copies
      this.loading = true
      try {
        const result = await getPageCopies()
        this.copies = mergePageCopies(result.data)
        this.publicLoaded = true
      } catch {
        this.copies = mergePageCopies(this.copies || PAGE_COPY_DEFAULTS)
        this.publicLoaded = true
      } finally {
        this.loading = false
      }
      return this.copies
    },
    async loadAdminCopies(options = {}) {
      if (this.adminLoaded && !options.force) return this.adminCopies
      this.adminLoading = true
      try {
        const result = await getAdminPageCopies()
        this.adminCopies = mergePageCopies(result.data)
        this.copies = mergePageCopies(result.data)
        this.adminLoaded = true
        this.publicLoaded = true
      } catch (error) {
        this.adminCopies = mergePageCopies(this.adminCopies || PAGE_COPY_DEFAULTS)
        if (options.throwOnError) throw error
        this.adminLoaded = true
      } finally {
        this.adminLoading = false
      }
      return this.adminCopies
    },
    async saveAdminCopy(copyKey, payload) {
      const result = await updateAdminPageCopy(copyKey, payload)
      this.applyCopyItem(result.data)
      return result.data
    },
    async resetAdminCopies() {
      const result = await resetAdminPageCopies()
      this.adminCopies = mergePageCopies(result.data)
      this.copies = mergePageCopies(result.data)
      this.adminLoaded = true
      this.publicLoaded = true
      return this.adminCopies
    },
    applyCopyItem(item) {
      this.adminCopies = mergePageCopies(replaceCopy(this.adminCopies, item))
      this.copies = mergePageCopies(replaceCopy(this.copies, item))
    }
  }
})

function replaceCopy(copies, item) {
  if (!item?.copyKey) return copies
  const exists = copies.some(copy => copy.copyKey === item.copyKey)
  if (!exists) return copies
  return copies.map(copy => (copy.copyKey === item.copyKey ? { ...copy, ...item } : copy))
}
