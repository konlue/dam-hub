<template>
  <div class="user-profile-page">
    <h2 class="page-title">个人中心</h2>
    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="14" :lg="14">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <el-form :model="profileForm" label-width="80px" label-position="left">
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar :src="profileForm.userAvatar" :size="96" class="avatar-preview" />
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </el-upload>
            </div>
            <el-form-item label="用户名">
              <el-input v-model="profileForm.userName" placeholder="请输入用户名" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="简介">
              <el-input
                v-model="profileForm.userProfile"
                type="textarea"
                placeholder="介绍一下自己吧"
                :rows="4"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10" :lg="10">
        <el-card class="password-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px" label-position="left">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="不少于 8 位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="checkPassword">
              <el-input v-model="passwordForm.checkPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordSaving" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import {
  updateMyUserUsingPost,
  uploadAvatarUsingPost,
  changePasswordUsingPost,
} from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()

// ---- 个人信息 ----
const profileForm = ref({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const profileSaving = ref(false)

const initProfileForm = () => {
  const user = loginUserStore.loginUser
  profileForm.value.userName = user.userName ?? ''
  profileForm.value.userAvatar = user.userAvatar ?? ''
  profileForm.value.userProfile = user.userProfile ?? ''
}

onMounted(() => {
  initProfileForm()
})

const beforeAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('请上传图片文件（jpg/png/gif/webp）')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像图片不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: any) => {
  try {
    const res = await uploadAvatarUsingPost({}, options.file)
    if (res.data.code === 0 && res.data.data) {
      profileForm.value.userAvatar = res.data.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传失败，' + res.data.message)
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const saveProfile = async () => {
  if (!profileForm.value.userName?.trim()) {
    ElMessage.warning('用户名不能为空')
    return
  }
  profileSaving.value = true
  try {
    const res = await updateMyUserUsingPost({
      userName: profileForm.value.userName,
      userAvatar: profileForm.value.userAvatar,
      userProfile: profileForm.value.userProfile,
    })
    if (res.data.code === 0) {
      ElMessage.success('保存成功')
      await loginUserStore.fetchLoginUser()
    } else {
      ElMessage.error('保存失败，' + res.data.message)
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
  profileSaving.value = false
}

// ---- 修改密码 ----
const passwordFormRef = ref<FormInstance>()
const passwordSaving = ref(false)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  checkPassword: '',
})

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于 8 位', trigger: 'blur' },
  ],
  checkPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    passwordSaving.value = true
    try {
      const res = await changePasswordUsingPost({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword,
        checkPassword: passwordForm.value.checkPassword,
      })
      if (res.data.code === 0) {
        ElMessage.success('密码修改成功，请重新登录')
        passwordForm.value = { oldPassword: '', newPassword: '', checkPassword: '' }
        passwordFormRef.value?.resetFields()
      } else {
        ElMessage.error(res.data.message || '修改失败')
      }
    } catch (e) {
      ElMessage.error('修改密码失败')
    }
    passwordSaving.value = false
  })
}
</script>

<style scoped>
.user-profile-page {
  max-width: 960px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #2d1b69;
  margin-bottom: 24px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #2d1b69;
}

.profile-card,
.password-card {
  border-radius: 12px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 20px;
}
</style>
