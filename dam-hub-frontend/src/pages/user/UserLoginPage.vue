<template>
  <div id="userLoginPage">
    <h2 class="title">企业图素中台 - 用户登录</h2>
    <div class="desc">企业图素中台</div>
    <el-form :model="formState" ref="formRef" autocomplete="off" @submit.prevent="handleSubmit">
      <el-form-item
        prop="userAccount"
        :rules="[{ required: true, message: '请输入账号', trigger: 'blur' }]"
      >
        <el-input v-model="formState.userAccount" placeholder="请输入账号" />
      </el-form-item>
      <el-form-item
        prop="userPassword"
        :rules="[
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 8, message: '密码长度不能小于 8 位', trigger: 'blur' },
        ]"
      >
        <el-input v-model="formState.userPassword" type="password" show-password placeholder="请输入密码" />
      </el-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <el-form-item>
        <el-button type="primary" native-type="submit" style="width: 100%">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { ElMessage } from 'element-plus'
import router from '@/router'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const formRef = ref()
const loginUserStore = useLoginUserStore()

const handleSubmit = async () => {
  const res = await userLoginUsingPost(formState)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    ElMessage.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    ElMessage.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
