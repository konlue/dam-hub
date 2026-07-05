<template>
  <div class="picture-search-form">
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchParams" @submit.prevent="doSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="searchParams.searchText"
          placeholder="从名称和简介搜索"
          clearable
        />
      </el-form-item>
      <el-form-item label="分类">
        <el-autocomplete
          v-model="searchParams.category"
          style="min-width: 180px"
          placeholder="请输入分类"
          :fetch-suggestions="queryCategorySearch"
          clearable
        />
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="searchParams.tags"
          style="min-width: 180px"
          multiple
          filterable
          allow-create
          placeholder="请输入标签"
          clearable
        >
          <el-option
            v-for="item in tagOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          start-placeholder="编辑开始时间"
          end-placeholder="编辑结束时间"
          format="YYYY/MM/DD"
          :shortcuts="rangeShortcuts"
          @change="onRangeChange"
        />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="searchParams.name" placeholder="请输入名称" clearable />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="searchParams.introduction" placeholder="请输入简介" clearable />
      </el-form-item>
      <el-form-item label="宽度">
        <el-input-number v-model="searchParams.picWidth" :min="0" />
      </el-form-item>
      <el-form-item label="高度">
        <el-input-number v-model="searchParams.picHeight" :min="0" />
      </el-form-item>
      <el-form-item label="格式">
        <el-input v-model="searchParams.picFormat" placeholder="请输入格式" clearable />
      </el-form-item>
      <el-form-item>
        <div style="display: flex; gap: 8px">
          <el-button type="primary" native-type="submit" style="width: 96px">搜索</el-button>
          <el-button @click="doClear">重置</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {listPictureTagCategoryUsingGet} from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

const searchParams = reactive<API.PictureQueryRequest>({})

const doSearch = () => {
  props.onSearch?.(searchParams)
}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])

const queryCategorySearch = (queryString: string, cb: (results: any[]) => void) => {
  const results = queryString
    ? categoryOptions.value.filter((item) => item.toLowerCase().includes(queryString.toLowerCase()))
    : categoryOptions.value
  cb(results.map((item) => ({ value: item })))
}

const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => ({
      value: data,
      label: data,
    }))
    categoryOptions.value = res.data.data.categoryList ?? []
  } else {
    ElMessage.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const dateRange = ref<any[]>([])

const onRangeChange = (dates: any) => {
  if (dates?.length >= 2) {
    searchParams.startEditTime = dates[0]
    searchParams.endEditTime = dates[1]
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}

const rangeShortcuts = [
  {
    text: '过去 7 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 7 * 24 * 3600 * 1000)
      return [start, end]
    },
  },
  {
    text: '过去 14 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 14 * 24 * 3600 * 1000)
      return [start, end]
    },
  },
  {
    text: '过去 30 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 30 * 24 * 3600 * 1000)
      return [start, end]
    },
  },
  {
    text: '过去 90 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 90 * 24 * 3600 * 1000)
      return [start, end]
    },
  },
]

const doClear = () => {
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  dateRange.value = []
  props.onSearch?.(searchParams)
}
</script>

<style scoped>
.picture-search-form .el-form-item {
  margin-top: 16px;
}
</style>
