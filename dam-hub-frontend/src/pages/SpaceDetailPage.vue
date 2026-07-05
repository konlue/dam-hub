<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <div style="display: flex; justify-content: space-between; align-items: center">
      <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType] }}）</h2>
      <div style="display: flex; gap: 12px; align-items: center">
        <el-button
          v-if="canUploadPicture"
          type="primary"
          tag="a"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          + 创建图片
        </el-button>
        <el-button
          v-if="canManageSpaceUser"
          type="primary"
          plain
          tag="a"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          <el-icon><User /></el-icon>
          成员管理
        </el-button>
        <el-button
          v-if="canManageSpaceUser"
          type="primary"
          plain
          tag="a"
          :href="`/space_analyze?spaceId=${id}`"
          target="_blank"
        >
          <el-icon><DataAnalysis /></el-icon>
          空间分析
        </el-button>
        <el-button v-if="canEditPicture" @click="doBatchEdit">
          <el-icon><Edit /></el-icon>
          批量编辑
        </el-button>
        <el-tooltip
          :content="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <el-progress
            type="circle"
            :width="42"
            :percentage="Number(((space.totalSize * 100) / space.maxSize).toFixed(1))"
          />
        </el-tooltip>
      </div>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />
    <!-- 按颜色搜索 -->
    <el-form-item label="按颜色搜索">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </el-form-item>
    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
      :onReload="fetchData"
    />
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
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onActivated, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { ElMessage } from 'element-plus'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { DataAnalysis, Edit, User } from '@element-plus/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '../constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})

function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      ElMessage.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    ElMessage.error('获取空间详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const fetchData = async () => {
  loading.value = true
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
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
  searchParams.value.current = page
  fetchData()
}

const onSizeChange = (size: number) => {
  searchParams.value.pageSize = size
  searchParams.value.current = 1
  fetchData()
}

const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    ElMessage.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

const batchEditPictureModalRef = ref()

const onBatchEditPictureSuccess = () => {
  fetchData()
}

const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

watch(
  () => props.id,
  () => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
