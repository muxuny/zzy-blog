<template>
  <div>
    <div class="page-header">
      <h2>图片管理</h2>
      <ImageUploader v-model="uploadUrl" @success="load" />
    </div>

    <el-table :data="images" stripe class="admin-table">
      <el-table-column label="预览" width="100">
        <template #default="{ row }">
          <el-image :src="row.url" class="image-preview" />
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" />
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ (row.size / 1024).toFixed(1) }} KB</template>
      </el-table-column>
      <el-table-column label="上传时间" width="180">
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
import { getImages, deleteImage } from '../../api/image'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils'
import ImageUploader from '../../components/admin/ImageUploader.vue'

const images = ref([])
const uploadUrl = ref('')

onMounted(() => load())

async function load() {
  const r = await getImages({ size: 100 })
  images.value = r.data || []
}

async function handleDelete(id) {
  await deleteImage(id)
  ElMessage.success('删除成功')
  load()
}
</script>

<style scoped>
.admin-table {
  margin-top: 16px;
}

.image-preview {
  width: 60px;
  height: 60px;
  object-fit: cover;
}
</style>
