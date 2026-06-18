<template>
  <div class="admin-page dashboard-page">
    <div class="page-header">
      <div>
        <span class="page-kicker">概览</span>
        <h2>仪表盘</h2>
      </div>
    </div>
    <div class="stat-grid">
      <el-card class="stat-card">
        <span>文章总数</span>
        <strong>{{ stats.totalArticles }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>已发布</span>
        <strong>{{ stats.publishedArticles }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>标签数</span>
        <strong>{{ stats.totalTags }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>待审核</span>
        <strong>{{ stats.pendingUsers }}</strong>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '../../api/article'
import { getTags } from '../../api/tag'
import { getUsers } from '../../api/user'
const stats = ref({ totalArticles: 0, publishedArticles: 0, totalTags: 0, pendingUsers: 0 })
onMounted(async () => {
  const ar = await getArticles({ size: 1 }); stats.value.totalArticles = ar.total||0
  const tr = await getTags(); stats.value.totalTags = (tr.data||[]).length
  try { const ur = await getUsers({ size: 1 }); stats.value.pendingUsers = ur.total||0 } catch {}
})
</script>
<style scoped>
.page-kicker {
  display: inline-block;
  margin-bottom: 4px;
  color: var(--accent-color);
  font-size: 12px;
  font-weight: 800;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--accent-color);
}

.stat-card span {
  display: block;
  color: var(--muted-text-color);
  font-size: 13px;
  font-weight: 750;
}

.stat-card strong {
  display: block;
  margin-top: 8px;
  color: var(--text-color);
  font-size: 34px;
  line-height: 1;
}

@media (max-width: 980px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
