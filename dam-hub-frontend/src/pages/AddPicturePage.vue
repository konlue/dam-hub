<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <p v-if="spaceId" style="color: #909399; margin-bottom: 16px">
      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </p>
    <!-- 选择上传方式 -->
    <el-tabs v-model="uploadType">
      <el-tab-pane label="文件上传" name="file">
        <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </el-tab-pane>
      <el-tab-pane label="URL 上传" name="url">
        <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </el-tab-pane>
    </el-tabs>
    <!-- 图片编辑 -->
    <div v-if="picture" class="edit-bar">
      <div style="display: flex; gap: 16px; justify-content: center">
        <el-button :icon="Edit" @click="doEditPicture">编辑图片</el-button>
        <el-button type="primary" :icon="FullScreen" @click="doImagePainting">
          AI 扩图
        </el-button>
      </div>
      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="picture?.url"
        :picture="picture"
        :spaceId="spaceId"
        :space="space"
        :onSuccess="onCropSuccess"
      />
      <ImageOutPainting
        ref="imageOutPaintingRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
    </div>
    <!-- 图片信息表单 -->
    <el-form
      v-if="picture"
      :model="pictureForm"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="名称">
        <el-input v-model="pictureForm.name" placeholder="请输入名称" clearable />
      </el-form-item>
      <el-form-item label="简介">
        <el-input
          v-model="pictureForm.introduction"
          type="textarea"
          placeholder="请输入简介"
          :rows="3"
        />
      </el-form-item>
      <el-form-item label="分类">
        <el-autocomplete
          v-model="pictureForm.category"
          placeholder="请输入分类"
          :fetch-suggestions="queryCategorySearch"
          clearable
        />
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="pictureForm.tags"
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
      <el-form-item>
        <el-button type="primary" native-type="submit" style="width: 100%">创建</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, onMounted, reactive, ref, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { Edit, FullScreen } from '@element-plus/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'

const router = useRouter()
const route = useRoute()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadType = ref<'file' | 'url'>('file')
const spaceId = computed(() => {
  return route.query?.spaceId
})

const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const handleSubmit = async () => {
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    spaceId: spaceId.value,
    ...pictureForm,
  })
  if (res.data.code === 0 && res.data.data) {
    ElMessage.success('创建成功')
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    ElMessage.error('创建失败，' + res.data.message)
  }
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

const getOldPicture = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({ id })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

const imageCropperRef = ref()

const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

const imageOutPaintingRef = ref()

const doImagePainting = async () => {
  imageOutPaintingRef.value?.openModal()
}

const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

const space = ref<API.SpaceVO>()

const fetchSpace = async () => {
  if (spaceId.value) {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}

#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
