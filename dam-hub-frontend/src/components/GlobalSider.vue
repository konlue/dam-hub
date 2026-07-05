<template>
  <div id="globalSider">
    <div v-if="loginUserStore.loginUser.id" class="sider-container">
      <el-menu
        :default-active="current"
        mode="vertical"
        @select="doMenuClick"
        class="sider-menu"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>主页</span>
        </el-menu-item>
        <el-menu-item index="/add_picture">
          <el-icon><Plus /></el-icon>
          <span>创建图片</span>
        </el-menu-item>
        <el-menu-item index="/ai_search">
          <el-icon><Search /></el-icon>
          <span>AI 搜图</span>
        </el-menu-item>

        <div class="menu-divider"></div>

        <el-menu-item index="/my_space">
          <el-icon><User /></el-icon>
          <span>我的空间</span>
        </el-menu-item>
        <el-menu-item :index="'/add_space?type=' + SPACE_TYPE_ENUM.TEAM">
          <el-icon><UserFilled /></el-icon>
          <span>创建团队</span>
        </el-menu-item>

        <template v-if="teamSpaceList.length > 0">
          <div class="menu-group-title">我的团队</div>
          <el-menu-item
            v-for="spaceUser in teamSpaceList"
            :key="spaceUser.spaceId"
            :index="'/space/' + spaceUser.spaceId"
          >
            <el-icon><Folder /></el-icon>
            <span>{{ spaceUser.space?.spaceName }}</span>
          </el-menu-item>
        </template>

        <template v-if="loginUserStore.loginUser.userRole === 'admin'">
          <div class="menu-divider"></div>
          <div class="menu-group-title">管理</div>
          <el-menu-item index="/admin/userManage">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/pictureManage">
            <el-icon><Picture /></el-icon>
            <span>图片管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/spaceManage">
            <el-icon><Grid /></el-icon>
            <span>空间管理</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref, watchEffect } from 'vue'
import { Picture, User, UserFilled, HomeFilled, Plus, Search, Folder, Grid } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { ElMessage } from 'element-plus'

const loginUserStore = useLoginUserStore()

const teamSpaceList = ref<API.SpaceUserVO[]>([])

const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    ElMessage.error('加载我的团队空间失败，' + res.data.message)
  }
}

watchEffect(() => {
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
const current = ref<string>('/')
router.afterEach((to) => {
  current.value = to.path
})

const doMenuClick = (key: string) => {
  router.push(key)
}
</script>

<style scoped>
#globalSider .sider-container {
  min-width: 200px;
  padding: 0 8px;
}

#globalSider .sider-menu {
  border-right: none;
  background: transparent;
}

.menu-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #d0c4f0, transparent);
  margin: 8px 16px;
}

.menu-group-title {
  font-size: 11px;
  color: #9b8ec4;
  padding: 8px 20px 4px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: rgba(107, 70, 193, 0.08);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(107, 70, 193, 0.12), rgba(139, 92, 246, 0.08));
  color: #6b46c1;
  font-weight: 600;
}
</style>
