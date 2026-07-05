<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <div v-loading="loading" class="picture-grid">
      <div v-for="picture in dataList" :key="picture.id" class="picture-grid-item">
        <el-card shadow="hover" @click="doClickPicture(picture)" class="picture-card">
          <img
            :alt="picture.name"
            :src="picture.thumbnailUrl ?? picture.url"
            class="picture-cover"
          />
          <div class="picture-info">
            <div class="picture-name">{{ picture.name }}</div>
            <div class="picture-tags">
              <el-tag type="success" size="small">
                {{ picture.category ?? '默认' }}
              </el-tag>
              <el-tag v-for="tag in picture.tags" :key="tag" size="small">
                {{ tag }}
              </el-tag>
            </div>
          </div>
          <div v-if="showOp" class="picture-actions">
            <el-button link @click="(e) => doShare(picture, e)">
              <el-icon><Share /></el-icon>
            </el-button>
            <el-button link @click="(e) => doSearch(picture, e)">
              <el-icon><Search /></el-icon>
            </el-button>
            <el-button v-if="canEdit" link @click="(e) => doEdit(picture, e)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button v-if="canDelete" link type="danger" @click="(e) => doDelete(picture, e)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Delete, Edit, Search, Share } from '@element-plus/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

const doSearch = (picture, e) => {
  e.stopPropagation()
  window.open(`/search_picture?pictureId=${picture.id}`)
}

const doEdit = (picture, e) => {
  e.stopPropagation()
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

const doDelete = async (picture, e) => {
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    props.onReload?.()
  } else {
    ElMessage.error('删除失败')
  }
}

const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = (picture, e) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
.picture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.picture-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.picture-card:hover {
  transform: translateY(-2px);
}

.picture-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.picture-info {
  padding: 8px 0;
}

.picture-name {
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picture-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.picture-actions {
  display: flex;
  justify-content: center;
  gap: 4px;
  border-top: 1px solid #eee;
  padding-top: 8px;
  margin-top: 8px;
}
</style>
