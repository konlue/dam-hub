<template>
  <div class="space-user-analyze">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>空间图片用户分析</span>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-radio-group v-model="timeDimension">
              <el-radio-button value="day">日</el-radio-button>
              <el-radio-button value="week">周</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
            </el-radio-group>
            <el-input
              v-model="searchUserId"
              placeholder="请输入用户 id"
              style="width: 200px"
              @keyup.enter="doSearch"
            >
              <template #append>
                <el-button @click="doSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { ElMessage } from 'element-plus'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

const timeDimension = ref<'day' | 'week' | 'month'>('day')

const searchUserId = ref<string>()
const userId = ref<string>()
const doSearch = () => {
  userId.value = searchUserId.value
}

const dataList = ref<API.SpaceCategoryAnalyzeResponse>([])
const loading = ref(true)

const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUserAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

watchEffect(() => {
  fetchData()
})

const options = computed(() => {
  const periods = dataList.value.map((item) => item.period)
  const counts = dataList.value.map((item) => item.count)

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true,
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})
</script>

<style scoped></style>
