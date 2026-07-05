<template>
  <div id="vipExchangePage">
    <h2 style="margin-bottom: 16px">会员码兑换</h2>
    <!-- 兑换码表单 -->
    <el-form :model="formData" label-position="top" @submit.prevent="handleSubmit">
      <el-form-item label="兑换码">
        <el-input
          v-model="formData.vipCode"
          placeholder="请输入会员兑换码"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" style="width: 100%" :loading="loading">
          兑换
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exchangeVipUsingPost } from '@/api/userController.ts'
import { useRouter } from 'vue-router'

const formData = reactive<API.VipExchangeRequest>({
  vipCode: '',
})

const loading = ref(false)

const router = useRouter()

const handleSubmit = async () => {
  if (!formData.vipCode) {
    ElMessage.error('请输入兑换码')
    return
  }

  loading.value = true

  try {
    const res = await exchangeVipUsingPost({
      vipCode: formData.vipCode,
    })

    if (res.data.code === 0 && res.data.data) {
      ElMessage.success('兑换成功！')
      router.push({ path: `/` })
    } else {
      ElMessage.error('兑换失败：' + res.data.message)
    }
  } catch {
    ElMessage.error('兑换失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#vipExchangePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
