<template>
  <div class="uploader">
    <el-upload :http-request="handleUpload" :show-file-list="false" accept="image/*" class="upload-btn">
      <el-button type="primary">上传图片</el-button>
    </el-upload>
    <div v-if="modelValue" class="preview">
      <img :src="modelValue" />
      <el-button size="small" type="danger" @click="$emit('update:modelValue', '')" circle>×</el-button>
    </div>
  </div>
</template>

<script setup>
import { uploadImage } from '../../api/image'
import { ElMessage } from 'element-plus'

defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue', 'success'])

async function handleUpload(opt) {
  try {
    const r = await uploadImage(opt.file)
    emit('update:modelValue', r.data.url)
    emit('success')
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.uploader {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.preview {
  position: relative;
}

.preview img {
  max-width: 200px;
  max-height: 120px;
  border: 1px solid var(--soft-border-color);
  border-radius: var(--radius-md);
  object-fit: cover;
}

.preview .el-button {
  position: absolute;
  top: -8px;
  right: -8px;
}
</style>
