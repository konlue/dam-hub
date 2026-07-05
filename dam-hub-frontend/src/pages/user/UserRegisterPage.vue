<template>
  <div id="userRegisterPage">
    <h2 class="title">企业图素中台 - 用户注册</h2>
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
      <el-form-item
        prop="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码', trigger: 'blur' },
          { min: 8, message: '确认密码长度不能小于 8 位', trigger: 'blur' },
        ]"
      >
        <el-input v-model="formState.checkPassword" type="password" show-password placeholder="请输入确认密码" />
      </el-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <el-form-item>
        <el-button type="primary" native-type="submit" style="width: 100%">注册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { ElMessage } from 'element-plus'
import router from '@/router'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const formRef = ref()

const handleSubmit = async () => {
  if (formState.userPassword !== formState.checkPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  const res = await userRegisterUsingPost(formState)
  if (res.data.code === 0 && res.data.data) {
    ElMessage.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    ElMessage.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
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
