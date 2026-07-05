<template>
  <div id="mySpacePage">
    <p>正在跳转，请稍后。。。</p>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { ElMessage } from 'element-plus'
import { onMounted, onActivated } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const checkUserSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    router.replace('/user/login')
    return
  }
  try {
    const res = await listSpaceVoByPageUsingPost({
      userId: loginUser.id,
      current: 1,
      pageSize: 1,
      spaceType: SPACE_TYPE_ENUM.PRIVATE,
    })
    if (res.data.code === 0) {
      if (res.data.data?.records?.length > 0) {
        const space = res.data.data.records[0]
        router.replace(`/space/${space.id}`)
      } else {
        router.replace('/add_space')
        ElMessage.warning('请先创建空间')
      }
    } else {
      ElMessage.error('加载我的空间失败，' + res.data.message)
    }
  } catch (e) {
    ElMessage.error('加载我的空间失败，请检查网络连接')
  }
}

onMounted(() => {
  checkUserSpace()
})

onActivated(() => {
  checkUserSpace()
})
</script>
