<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量创建</h2>
    <!-- 图片信息表单 -->
    <el-form :model="formData" label-position="top" @submit.prevent="handleSubmit">
      <el-form-item label="关键词">
        <el-input v-model="formData.searchText" placeholder="请输入关键词" clearable />
      </el-form-item>
      <el-form-item label="抓取数量">
        <el-input-number
          v-model="formData.count"
          placeholder="请输入数量"
          style="min-width: 180px"
          :min="1"
          :max="30"
        />
      </el-form-item>
      <el-form-item label="名称前缀">
        <el-input
          v-model="formData.namePrefix"
          placeholder="请输入名称前缀，会自动补充序号"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" style="width: 100%" :loading="loading">
          执行任务
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'
import { useRouter } from 'vue-router'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
const loading = ref(false)

const router = useRouter()

const handleSubmit = async () => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData,
  })
  if (res.data.code === 0 && res.data.data) {
    ElMessage.success(`创建成功，共 ${res.data.data} 条`)
    router.push({ path: `/` })
  } else {
    ElMessage.error('创建失败，' + res.data.message)
  }
  loading.value = false
}
</script>

<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
