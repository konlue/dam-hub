<template>
  <div class="space-user-analyze">
    <div style="display: flex; gap: 16px">
      <el-card header="存储空间" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ formatSize(data.usedSize) }} /
            {{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}
          </h3>
          <el-progress type="dashboard" :percentage="data.sizeUsageRatio ?? 0" />
        </div>
      </el-card>
      <el-card header="图片数量" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ data.usedCount }} / {{ data.maxCount ?? '无限制' }}
          </h3>
          <el-progress type="dashboard" :percentage="data.countUsageRatio ?? 0" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { ElMessage } from 'element-plus'
import { formatSize } from '@/utils'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

const data = ref<API.SpaceUsageAnalyzeResponse>({})
const loading = ref(true)

const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUsageAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

watchEffect(() => {
  fetchData()
})
</script>

<style scoped></style>
