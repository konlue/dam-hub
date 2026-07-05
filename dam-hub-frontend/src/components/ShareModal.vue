<template>
  <div>
    <el-dialog v-model="visible" :title="title" width="400px">
      <h4>复制分享链接</h4>
      <div class="share-link-row">
        <el-input :model-value="link" readonly>
          <template #append>
            <el-button @click="copyLink">复制</el-button>
          </template>
        </el-input>
      </div>
      <div style="margin-bottom: 16px" />
      <h4>手机扫码查看</h4>
      <div style="text-align: center">
        <QrcodeVue :value="link" :size="160" />
      </div>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import QrcodeVue from 'qrcode.vue'
import { ElMessage } from 'element-plus'

interface Props {
  title: string;
  link: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: "分享图片",
  link: 'https://www.codefather.cn'
})

const visible = ref(false)

const openModal = () => {
  visible.value = true
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.link)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

defineExpose({
  openModal,
})
</script>

<style scoped>
.share-link-row {
  margin-top: 8px;
}
</style>
