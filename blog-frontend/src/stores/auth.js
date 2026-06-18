import { defineStore } from 'pinia'
import { getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({ user: null, token: localStorage.getItem('token') || '', userLoading: false }),
  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.user?.role === 'admin'
  },
  actions: {
    setToken(token) { this.token = token; localStorage.setItem('token', token) },
    logout() { this.user = null; this.token = ''; localStorage.removeItem('token') },
    async fetchUser(options = {}) {
      this.userLoading = true
      try {
        const r = await getMe({ skipAuthRedirect: true, skipErrorMessage: options.silent })
        this.user = r.data
        return this.user
      } catch (error) {
        this.logout()
        if (!options.silent) throw error
        return null
      } finally {
        this.userLoading = false
      }
    }
  }
})
