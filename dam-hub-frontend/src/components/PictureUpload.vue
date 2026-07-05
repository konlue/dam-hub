<template>
  <div class="picture-upload">
    <el-upload
      list-type="picture-card"
      :show-file-list="false"
      :http-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else class="upload-placeholder">
        <el-icon v-if="loading" class="is-loading"><Loading /></el-icon>
        <el-icon v-else><Plus /></el-icon>
        <div class="upload-text">点击或拖拽上传图片</div>
      </div>
    </el-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { Loading, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const handleUpload = async (options: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId;
    const res = await uploadPictureUsingPost(params, {}, options.file)
    if (res.data.code === 0 && res.data.data) {
      ElMessage.success('图片上传成功')
      props.onSuccess?.(res.data.data)
    } else {
      ElMessage.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    ElMessage.error('图片上传失败')
  }
  loading.value = false
}

const loading = ref<boolean>(false)

const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    ElMessage.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('不能上传超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M
}
</script>
<style scoped>
.picture-upload :deep(.el-upload--picture-card) {
  width: 100% !important;
  height: 100% !important;
  min-width: 152px;
  min-height: 152px;
}

.picture-upload img {
  max-width: 100%;
  max-height: 480px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.upload-placeholder .el-icon {
  font-size: 32px;
  color: #999;
}

.upload-text {
  color: #666;
  font-size: 12px;
}
</style>
