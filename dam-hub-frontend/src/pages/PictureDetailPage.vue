<template>
  <div id="pictureDetailPage">
    <el-row :gutter="[16, 16]">
      <!-- 图片预览 -->
      <el-col :sm="24" :md="16" :xl="18">
        <el-card header="图片预览">
          <el-image :src="picture.url" style="max-height: 600px; object-fit: contain" />
        </el-card>
      </el-col>
      <!-- 图片信息区域 -->
      <el-col :sm="24" :md="8" :xl="6">
        <el-card header="图片信息">
          <el-descriptions :column="1">
            <el-descriptions-item label="作者">
              <div style="display: flex; align-items: center; gap: 8px">
                <el-avatar :size="24" :src="picture.user?.userAvatar" />
                <div>{{ picture.user?.userName }}</div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="名称">
              {{ picture.name ?? '未命名' }}
            </el-descriptions-item>
            <el-descriptions-item label="简介">
              {{ picture.introduction ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              {{ picture.category ?? '默认' }}
            </el-descriptions-item>
            <el-descriptions-item label="标签">
              <el-tag v-for="tag in picture.tags" :key="tag" style="margin-right: 4px">
                {{ tag }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="格式">
              {{ picture.picFormat ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="宽度">
              {{ picture.picWidth ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="高度">
              {{ picture.picHeight ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="宽高比">
              {{ picture.picScale ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="大小">
              {{ formatSize(picture.picSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="主色调">
              <div style="display: flex; align-items: center; gap: 8px">
                {{ picture.picColor ?? '-' }}
                <div
                  v-if="picture.picColor"
                  :style="{
                    width: '16px',
                    height: '16px',
                    backgroundColor: toHexColor(picture.picColor),
                  }"
                />
              </div>
            </el-descriptions-item>
          </el-descriptions>
          <!-- 图片操作 -->
          <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px">
            <el-button type="primary" @click="doDownload">
              <el-icon><Download /></el-icon>
              免费下载
            </el-button>
            <el-button type="primary" plain @click="doShare">
              <el-icon><Share /></el-icon>
              分享
            </el-button>
            <el-button v-if="canEdit" @click="doEdit">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button v-if="canDelete" type="danger" @click="doDelete">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onActivated, ref } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'
import { Delete, Download, Edit, Share } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize, toHexColor } from '@/utils'
import ShareModal from '@/components/ShareModal.vue'
import { SPACE_PERMISSION_ENUM } from '@/constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})

function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

const canEdit = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDelete = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      ElMessage.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    ElMessage.error('获取图片详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchPictureDetail()
})

onActivated(() => {
  fetchPictureDetail()
})

const router = useRouter()

const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}

const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    router.back()
  } else {
    ElMessage.error('删除失败')
  }
}

const doDownload = () => {
  downloadImage(picture.value.url)
}

const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
#pictureDetailPage {
  margin-bottom: 16px;
}
</style>
