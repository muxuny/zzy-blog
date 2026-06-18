<template>
  <div>
    <div class="page-header">
      <h2>标签管理</h2>
      <div class="inline-create">
        <el-input v-model="newName" placeholder="标签名" />
        <el-button type="primary" @click="handleCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="tags" stripe class="admin-table">
      <el-table-column prop="name" label="标签名" />
      <el-table-column label="创建时间">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags, createTag, deleteTag } from '../../api/tag'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils'

const tags = ref([])
const newName = ref('')

onMounted(async () => {
  const r = await getTags()
  tags.value = r.data || []
})

async function handleCreate() {
  if (!newName.value.trim()) return
  await createTag({ name: newName.value.trim() })
  ElMessage.success('创建成功')
  newName.value = ''
  const r = await getTags()
  tags.value = r.data || []
}

async function handleDelete(id) {
  await deleteTag(id)
  ElMessage.success('删除成功')
  tags.value = tags.value.filter(t => t.id !== id)
}
</script>

<style scoped>
.inline-create {
  display: flex;
  gap: 8px;
}

.inline-create .el-input {
  width: 200px;
}

.admin-table {
  margin-top: 16px;
}
</style>
