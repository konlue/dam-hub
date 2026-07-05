<template>
  <div id="globalHeader">
    <div class="header-container">
      <router-link to="/" class="title-link">
        <div class="title-bar">
          <img class="logo" src="../assets/logo.png" alt="logo" />
          <div class="title">企业图素中台</div>
        </div>
      </router-link>
      <div class="header-spacer"></div>
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown-link">
              <el-avatar :src="loginUserStore.loginUser.userAvatar" :size="32" />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/user/profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="/my_space">
                  <el-icon><User /></el-icon>
                  我的空间
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-else>
          <el-button type="primary" @click="router.push('/user/login')">登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { User, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const router = useRouter()

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await doLogout()
  } else {
    router.push(command)
  }
}

const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    ElMessage.success('退出登录成功')
    await router.push('/user/login')
  } else {
    ElMessage.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#globalHeader .header-container {
  display: flex;
  align-items: center;
  height: 100%;
}

.title-link {
  text-decoration: none;
  color: inherit;
}

.title-bar {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.title {
  color: #2d1b69;
  font-size: 18px;
  font-weight: 700;
  margin-left: 12px;
  white-space: nowrap;
}

.logo {
  height: 42px;
}

.header-spacer {
  flex: 1;
}

.user-login-status {
  flex-shrink: 0;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: #4a4a6a;
}
</style>
