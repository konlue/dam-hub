<template>
  <div class="batch-edit-picture-modal">
    <el-dialog v-model="visible" title="批量编辑图片" @close="closeModal">
      <p style="color: #909399; margin-bottom: 16px">* 只对当前页面的图片生效</p>
      <!-- 批量创建表单 -->
      <el-form :model="formData" label-position="top">
        <el-form-item label="分类">
          <el-autocomplete
            v-model="formData.category"
            placeholder="请输入分类"
            :fetch-suggestions="queryCategorySearch"
            clearable
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="formData.tags"
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
        <el-form-item label="命名规则">
          <el-input
            v-model="formData.nameRule"
            placeholder="请输入命名规则，输入 {序号} 可动态生成"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleSubmit">提交</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureByBatchUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { ElMessage } from 'element-plus'

interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}

const props = withDefaults(defineProps<Props>(), {})

const visible = ref(false)

const openModal = () => {
  visible.value = true
}

const closeModal = () => {
  visible.value = false
}

defineExpose({
  openModal,
})

const formData = reactive<API.PictureEditByBatchRequest>({
  category: '',
  tags: [],
  nameRule: '',
})

const handleSubmit = async () => {
  if (!props.pictureList) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...formData,
  })
  if (res.data.code === 0 && res.data.data) {
    ElMessage.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    ElMessage.error('操作失败，' + res.data.message)
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
</script>
