<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <el-input
        v-model="searchParams.searchText"
        placeholder="从海量图片中搜索"
        size="large"
        @keyup.enter="doSearch"
      >
        <template #append>
          <el-button @click="doSearch">搜索</el-button>
        </template>
      </el-input>
    </div>
    <!-- 分类和标签筛选 -->
    <el-tabs v-model="selectedCategory" @tab-change="doSearch">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane v-for="category in categoryList" :label="category" :name="category" :key="category" />
    </el-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <div style="display: flex; flex-wrap: wrap; gap: 8px">
        <el-check-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          :checked="selectedTagList[index]"
          @change="(checked: boolean) => { selectedTagList[index] = checked; doSearch() }"
        >
          {{ tag }}
        </el-check-tag>
      </div>
    </div>
    <!-- 图片列表 -->
    <PictureList :dataList="dataList" :loading="loading" />
    <!-- 分页 -->
    <el-pagination
      style="text-align: right; margin-top: 16px"
      v-model:current-page="searchParams.current"
      v-model:page-size="searchParams.pageSize"
      :total="total"
      :page-sizes="[12, 24, 36]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onActivated, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'
import PictureList from '@/components/PictureList.vue'

const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const fetchData = async () => {
  loading.value = true
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

onActivated(() => {
  fetchData()
})

const onPageChange = (page: number) => {
  searchParams.current = page
  fetchData()
}

const onSizeChange = (size: number) => {
  searchParams.pageSize = size
  searchParams.current = 1
  fetchData()
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    ElMessage.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})
</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 16px;
}
</style>
