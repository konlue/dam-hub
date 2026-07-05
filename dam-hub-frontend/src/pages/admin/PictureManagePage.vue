<template>
  <div id="pictureManagePage">
    <div style="display: flex; justify-content: space-between; align-items: center">
      <h2>图片管理</h2>
      <div style="display: flex; gap: 8px">
        <el-button type="primary" tag="a" href="/add_picture" target="_blank">+ 创建图片</el-button>
        <el-button type="primary" plain tag="a" href="/add_picture/batch" target="_blank">+ 批量创建图片</el-button>
      </div>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchParams" @submit.prevent="doSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="searchParams.searchText"
          placeholder="从名称和简介搜索"
          clearable
        />
      </el-form-item>
      <el-form-item label="类型">
        <el-input v-model="searchParams.category" placeholder="请输入类型" clearable />
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="searchParams.tags"
          multiple
          filterable
          allow-create
          placeholder="请输入标签"
          style="min-width: 180px"
          clearable
        />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select
          v-model="searchParams.reviewStatus"
          style="min-width: 180px"
          placeholder="请选择审核状态"
          clearable
        >
          <el-option
            v-for="item in PIC_REVIEW_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <el-table :data="dataList" stripe>
      <el-table-column prop="id" label="id" width="80" />
      <el-table-column label="图片" width="140">
        <template #default="{ row }">
          <el-image :src="row.url" style="width: 120px" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="introduction" label="简介" show-overflow-tooltip />
      <el-table-column prop="category" label="类型" />
      <el-table-column label="标签">
        <template #default="{ row }">
          <div style="display: flex; flex-wrap: wrap; gap: 4px">
            <el-tag v-for="tag in JSON.parse(row.tags || '[]')" :key="tag" size="small">
              {{ tag }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="图片信息" width="160">
        <template #default="{ row }">
          <div>格式：{{ row.picFormat }}</div>
          <div>宽度：{{ row.picWidth }}</div>
          <div>高度：{{ row.picHeight }}</div>
          <div>宽高比：{{ row.picScale }}</div>
          <div>大小：{{ (row.picSize / 1024).toFixed(2) }}KB</div>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户 id" width="80" />
      <el-table-column prop="spaceId" label="空间 id" width="80" />
      <el-table-column label="审核信息" width="180">
        <template #default="{ row }">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[row.reviewStatus] }}</div>
          <div>审核信息：{{ row.reviewMessage }}</div>
          <div>审核人：{{ row.reviewerId }}</div>
          <div v-if="row.reviewTime">
            审核时间：{{ dayjs(row.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">
          {{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="编辑时间" width="160">
        <template #default="{ row }">
          {{ dayjs(row.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div style="display: flex; gap: 4px">
            <el-button
              v-if="row.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              type="primary"
              link
              size="small"
              @click="handleReview(row, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              type="danger"
              link
              size="small"
              @click="handleReview(row, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              拒绝
            </el-button>
            <el-button type="primary" link size="small" tag="a" :href="`/add_picture?id=${row.id}`" target="_blank">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="doDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px; text-align: right">
      <el-pagination
        v-model:current-page="searchParams.current"
        v-model:page-size="searchParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '../../constants/picture.ts'
import dayjs from 'dayjs'

const dataList = ref<API.Picture[]>([])
const total = ref(0)

const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
    nullSpaceId: true,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
}

onMounted(() => {
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

const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error('删除失败')
  }
}

const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    ElMessage.success('审核操作成功')
    fetchData()
  } else {
    ElMessage.error('审核操作失败，' + res.data.message)
  }
}
</script>
