<template>
  <div id="searchPicturePage">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin-bottom: 16px">原图</h3>
    <el-card shadow="hover" style="width: 240px">
      <img
        :alt="picture.name"
        :src="picture.thumbnailUrl ?? picture.url"
        style="height: 180px; object-fit: cover; width: 100%"
      />
    </el-card>
    <h3 style="margin: 16px 0">识图结果</h3>
    <!-- 图片结果列表 -->
    <div v-loading="loading" class="picture-grid">
      <div v-for="item in dataList" :key="item.fromUrl" class="picture-grid-item">
        <a :href="item.fromUrl" target="_blank">
          <el-card shadow="hover">
            <img
              :alt="item.name"
              :src="item.thumbUrl"
              style="height: 180px; object-fit: cover; width: 100%"
            />
          </el-card>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  getPictureVoByIdUsingGet,
  searchPictureByPictureUsingPost,
} from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()

const pictureId = computed(() => {
  return route.query?.pictureId
})
const picture = ref<API.PictureVO>({})

const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
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

const dataList = ref<API.ImageSearchResult[]>([])
const loading = ref<boolean>(true)

const fetchResultData = async () => {
  loading.value = true;
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      ElMessage.error('获取数据失败，' + res.data.message)
    }
  } catch (e: any) {
    ElMessage.error('获取数据失败，' + e.message)
  }
  loading.value = false;
}

onMounted(() => {
  fetchResultData()
})
</script>

<style scoped>
#searchPicturePage {
  margin-bottom: 16px;
}

.picture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
</style>
