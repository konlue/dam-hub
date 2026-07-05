<template>
  <div id="spaceManagePage">
    <div style="display: flex; justify-content: space-between; align-items: center">
      <h2>空间管理</h2>
      <div style="display: flex; gap: 8px">
        <el-button type="primary" tag="a" href="/add_space" target="_blank">+ 创建空间</el-button>
        <el-button type="primary" plain tag="a" href="/space_analyze?queryPublic=1" target="_blank">分析公共图库</el-button>
        <el-button type="primary" plain tag="a" href="/space_analyze?queryAll=1" target="_blank">分析全部空间</el-button>
      </div>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchParams" @submit.prevent="doSearch">
      <el-form-item label="空间名称">
        <el-input v-model="searchParams.spaceName" placeholder="请输入空间名称" clearable />
      </el-form-item>
      <el-form-item label="空间级别">
        <el-select
          v-model="searchParams.spaceLevel"
          style="min-width: 180px"
          placeholder="请选择空间级别"
          clearable
        >
          <el-option
            v-for="item in SPACE_LEVEL_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="空间类别">
        <el-select
          v-model="searchParams.spaceType"
          style="min-width: 180px"
          placeholder="请输入空间类别"
          clearable
        >
          <el-option
            v-for="item in SPACE_TYPE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="用户 id">
        <el-input v-model="searchParams.userId" placeholder="请输入用户 id" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <el-table :data="dataList" stripe>
      <el-table-column prop="id" label="id" width="80" />
      <el-table-column prop="spaceName" label="空间名称" />
      <el-table-column label="空间级别">
        <template #default="{ row }">
          {{ SPACE_LEVEL_MAP[row.spaceLevel] }}
        </template>
      </el-table-column>
      <el-table-column label="空间类别">
        <template #default="{ row }">
          <el-tag>{{ SPACE_TYPE_MAP[row.spaceType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="使用情况" width="200">
        <template #default="{ row }">
          <div>大小：{{ formatSize(row.totalSize) }} / {{ formatSize(row.maxSize) }}</div>
          <div>数量：{{ row.totalCount }} / {{ row.maxCount }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户 id" width="80" />
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div style="display: flex; gap: 4px">
            <el-button type="primary" link size="small" tag="a" :href="`/space_analyze?spaceId=${row.id}`" target="_blank">
              分析
            </el-button>
            <el-button type="primary" link size="small" tag="a" :href="`/add_space?id=${row.id}`" target="_blank">
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
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS,
} from '../../constants/space.ts'
import { formatSize } from '../../utils'

const dataList = ref<API.Space[]>([])
const total = ref(0)

const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
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
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error('删除失败')
  }
}
</script>
