<template>
  <div id="basicLayout">
    <div class="animated-bg"></div>
    <el-container style="min-height: 100vh">
      <el-header class="header">
        <GlobalHeader />
      </el-header>
      <el-container>
        <el-aside class="sider" width="auto">
          <GlobalSider />
        </el-aside>
        <el-main class="content">
          <router-view v-slot="{ Component }">
            <transition name="page-fade" mode="out-in">
              <keep-alive>
                <component :is="Component" />
              </keep-alive>
            </transition>
          </router-view>
        </el-main>
      </el-container>
      <el-footer class="footer">
        <p>© 2026 企业图素中台 - 汇聚海量灵感，驱动企业品牌增长</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<style scoped>
#basicLayout {
  position: relative;
  overflow: hidden;
}

.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 25%, #f0e6ff 50%, #e8f4f8 75%, #f5f3ff 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

#basicLayout .header {
  padding-inline: 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  color: unset;
  height: auto;
  line-height: normal;
  border-bottom: 1px solid rgba(107, 70, 193, 0.1);
  box-shadow: 0 1px 8px rgba(107, 70, 193, 0.06);
}

#basicLayout .sider {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-right: 1px solid rgba(107, 70, 193, 0.08);
  padding-top: 16px;
  overflow-y: auto;
}

#basicLayout .content {
  padding: 28px;
  background: transparent;
  margin-bottom: 28px;
  min-height: calc(100vh - 160px);
}

#basicLayout .footer {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  padding: 16px;
  text-align: center;
  height: auto;
  color: #9b8ec4;
  font-size: 13px;
}

/* 页面过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
