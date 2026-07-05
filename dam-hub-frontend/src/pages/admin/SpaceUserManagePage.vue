<template>
  <div id="spaceManagePage">
    <div style="display: flex; justify-content: space-between; align-items: center">
      <h2>空间成员管理</h2>
      <div style="display: flex; gap: 8px">
        <el-button type="primary" tag="a" href="/add_space" target="_blank">+ 创建空间</el-button>
        <el-button type="primary" plain tag="a" href="/space_analyze?queryPublic=1" target="_blank">分析公共图库</el-button>
        <el-button type="primary" plain tag="a" href="/space_analyze?queryAll=1" target="_blank">分析全部空间</el-button>
      </div>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 添加成员表单 -->
    <el-form :inline="true" :model="formData" @submit.prevent="handleSubmit">
      <el-form-item label="用户 id">
        <el-input v-model="formData.userId" placeholder="请输入用户 id" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">添加用户</el-button>
      </el-form-item>
    </el-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <el-table :data="dataList" stripe>
      <el-table-column label="用户">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 8px">
            <el-avatar :src="row.user?.userAvatar" :size="32" />
            {{ row.user?.userName }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="160">
        <template #default="{ row }">
          <el-select
            v-model="row.spaceRole"
            @change="(value) => editSpaceRole(value, row)"
          >
            <el-option
              v-for="item in SPACE_ROLE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { SPACE_ROLE_OPTIONS } from '../../constants/space.ts'
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost,
} from '@/api/spaceUserController.ts'
import dayjs from 'dayjs'

interface Props {
  id: string
}

const props = defineProps<Props>()

const dataList = ref<API.SpaceUserVO[]>([])

const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserUsingPost({
    spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchData()
})

const formData = reactive<API.SpaceUserAddRequest>({})

const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.data.code === 0) {
    ElMessage.success('添加成功')
    fetchData()
  } else {
    ElMessage.error('添加失败，' + res.data.message)
  }
}

const editSpaceRole = async (value, record) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value,
  })
  if (res.data.code === 0) {
    ElMessage.success('修改成功')
  } else {
    ElMessage.error('修改失败，' + res.data.message)
  }
}

const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    fetchData()
  } else {
    ElMessage.error('删除失败')
  }
}
</script>
