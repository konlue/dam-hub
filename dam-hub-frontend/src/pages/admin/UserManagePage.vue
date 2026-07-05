<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchParams" @submit.prevent="doSearch">
      <el-form-item label="账号">
        <el-input v-model="searchParams.userAccount" placeholder="输入账号" clearable />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="searchParams.userName" placeholder="输入用户名" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <el-table :data="dataList" stripe>
      <el-table-column prop="id" label="id" width="80" />
      <el-table-column prop="userAccount" label="账号" />
      <el-table-column prop="userName" label="用户名" />
      <el-table-column label="头像" width="120">
        <template #default="{ row }">
          <el-image :src="row.userAvatar" style="width: 80px" />
        </template>
      </el-table-column>
      <el-table-column prop="userProfile" label="简介" show-overflow-tooltip />
      <el-table-column label="用户角色">
        <template #default="{ row }">
          <el-tag v-if="row.userRole === 'admin'" type="success">管理员</el-tag>
          <el-tag v-else type="primary">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间">
        <template #default="{ row }">
          {{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="doDelete(row.id)">删除</el-button>
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
import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController.ts'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const dataList = ref<API.UserVO[]>([])
const total = ref(0)

const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({
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
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error('删除失败')
  }
}
</script>
