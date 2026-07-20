import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/article/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
  { path: '/tag/:name', name: 'TagArticles', component: () => import('../views/TagArticles.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reading',
    name: 'ReadingSpace',
    component: () => import('../views/ReadingSpace.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reading/history',
    name: 'ReadingHistory',
    component: () => import('../views/ReadingHistory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/creator/articles',
    name: 'CreatorArticles',
    component: () => import('../views/creator/MyArticles.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/creator/articles/create',
    name: 'CreatorArticleCreate',
    component: () => import('../views/creator/ArticleWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/creator/articles/edit/:id',
    name: 'CreatorArticleEdit',
    component: () => import('../views/creator/ArticleWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/creator/articles/preview/:id',
    name: 'CreatorArticlePreview',
    component: () => import('../views/creator/ArticlePreview.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('../components/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'articles', component: () => import('../views/admin/Articles.vue') },
      { path: 'articles/create', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'articles/edit/:id', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'tags', component: () => import('../views/admin/Tags.vue') },
      { path: 'images', component: () => import('../views/admin/Images.vue') },
      { path: 'users', component: () => import('../views/admin/Users.vue') },
      { path: 'profile', component: () => import('../views/admin/Profile.vue') },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const authStore = useAuthStore()
  if (token && !authStore.user) {
    try {
      await authStore.fetchUser({ silent: !to.meta.requiresAuth })
    } catch {
      // fetchUser has already cleared invalid auth state; continue with the guard below.
    }
  }
  if (to.meta.requiresAuth && !authStore.token) next({ path: '/login', query: { redirect: to.fullPath } })
  else if (to.meta.requiresAdmin && !authStore.isAdmin) next({ path: '/' })
  else next()
})

export default router
