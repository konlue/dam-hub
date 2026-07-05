<template>
  <div id="aiSearchPage">
    <h2 style="margin-bottom: 16px">智能素材策展助手</h2>
    <!-- 搜索输入区 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder='描述你需要的图片，如"科技感的横幅背景"'
        size="large"
        :disabled="loading"
        @keyup.enter="doSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="doSearch" :loading="loading">搜索</el-button>
        </template>
      </el-input>
      <!-- 过滤条件 -->
      <div class="filter-bar">
        <el-select
          v-model="form.category"
          placeholder="分类过滤（可选）"
          style="width: 160px"
          clearable
          filterable
        >
          <el-option
            v-for="cat in categoryOptions"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-select
          v-model="form.color"
          placeholder="颜色过滤（可选）"
          style="width: 160px"
          clearable
          filterable
          allow-create
        >
          <el-option
            v-for="c in colorOptions"
            :key="c.value"
            :label="c.label"
            :value="c.value"
          >
            <div style="display: flex; align-items: center; gap: 8px">
              <div
                :style="{
                  width: '14px',
                  height: '14px',
                  borderRadius: '3px',
                  backgroundColor: c.hex,
                  border: '1px solid #dcdfe6',
                }"
              />
              <span>{{ c.label }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
    </div>

    <!-- 搜索结果区域 -->
    <div v-if="hasSearched" v-loading="loading" element-loading-text="AI 正在分析您的需求，请稍候...">
      <!-- 意图分析 -->
      <div v-if="result?.intent" class="section">
        <h3>意图分析</h3>
        <div class="intent-row">
          <div class="intent-tags">
            <el-tag v-if="result.intent.industry" type="primary" effect="plain">
              行业：{{ result.intent.industry }}
            </el-tag>
            <el-tag v-if="result.intent.purpose" type="success" effect="plain">
              用途：{{ result.intent.purpose }}
            </el-tag>
            <el-tag v-if="result.intent.scene" type="warning" effect="plain">
              场景：{{ result.intent.scene }}
            </el-tag>
            <el-tag v-if="result.intent.style" type="danger" effect="plain">
              风格：{{ result.intent.style }}
            </el-tag>
          </div>
          <div v-if="result?.recommendations?.length" class="intent-preview">
            <div
              v-for="item in result.recommendations.slice(0, 3)"
              :key="item.pictureId"
              class="intent-preview-item"
              @click="goToDetail(item.pictureId)"
            >
              <el-image
                v-if="item.pictureUrl"
                :src="item.pictureThumbnailUrl || item.pictureUrl"
                fit="cover"
                class="intent-preview-img"
              />
              <div v-else class="intent-preview-placeholder">
                <el-icon :size="20"><Picture /></el-icon>
              </div>
              <span class="intent-preview-title">{{ item.title }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 策展方案 -->
      <div v-if="result?.answer" class="section">
        <h3>策展方案</h3>
        <el-card shadow="never" class="answer-card">
          <div class="markdown-body" v-html="renderedAnswer" @click="onAnswerClick"></div>
        </el-card>
      </div>

      <!-- 推荐结果列表 -->
      <div v-if="result?.recommendations?.length" class="section">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
          <h3 style="margin: 0">推荐结果</h3>
          <el-button :icon="Refresh" @click="doSearch" :loading="loading">重新生成</el-button>
        </div>
        <div class="recommendation-grid">
          <el-card
            v-for="item in result.recommendations"
            :key="item.pictureId"
            shadow="hover"
            class="recommendation-card"
            @click="goToDetail(item.pictureId)"
          >
            <el-image
              v-if="item.pictureUrl"
              :src="item.pictureThumbnailUrl || item.pictureUrl"
              fit="cover"
              class="recommendation-image"
            />
            <div v-else class="recommendation-image-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <div class="recommendation-info">
              <div class="recommendation-title">{{ item.title }}</div>
              <div class="recommendation-meta">
                <el-tag v-if="item.category" size="small" type="info">{{ item.category }}</el-tag>
                <el-tag v-if="item.taskType" size="small">{{ item.taskType }}</el-tag>
                <span v-if="item.score" class="score">
                  {{ Math.round(item.score * 100) }}% 匹配
                </span>
              </div>
              <div v-if="item.reason" class="recommendation-reason">
                {{ item.reason }}
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空结果提示 -->
      <div v-if="!loading && result && (!result.recommendations || result.recommendations.length === 0)" class="empty-tip">
        <el-empty description="未找到匹配的图片，请尝试换个描述" />
      </div>
    </div>

    <!-- 初始状态提示 -->
    <div v-if="!hasSearched" class="initial-tip">
      <el-empty description="输入自然语言描述，AI 帮你找到最合适的图片">
        <template #image>
          <el-icon :size="80" color="#c0c4cc"><Search /></el-icon>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiSearchUsingPost } from '@/api/aiController.ts'
import { getPictureVoByIdUsingGet, listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { marked } from 'marked'

const router = useRouter()

const searchQuery = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const result = ref<API.AiSearchResponse | null>(null)

const form = reactive({
  category: '',
  color: '',
})

const categoryOptions = ref<string[]>([])

const colorOptions = [
  { value: '0x000000', label: '黑色', hex: '#000000' },
  { value: '0xFFFFFF', label: '白色', hex: '#ffffff' },
  { value: '0xFF0000', label: '红色', hex: '#ff0000' },
  { value: '0x00FF00', label: '绿色', hex: '#00ff00' },
  { value: '0x0000FF', label: '蓝色', hex: '#0000ff' },
  { value: '0xFFFF00', label: '黄色', hex: '#ffff00' },
  { value: '0xFFA500', label: '橙色', hex: '#ffa500' },
  { value: '0x800080', label: '紫色', hex: '#800080' },
  { value: '0xFFC0CB', label: '粉色', hex: '#ffc0cb' },
  { value: '0x808080', label: '灰色', hex: '#808080' },
  { value: '0x00FFFF', label: '青色', hex: '#00ffff' },
  { value: '0x8B4513', label: '棕色', hex: '#8b4513' },
]

const fetchCategoryOptions = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      categoryOptions.value = res.data.data.categoryList ?? []
    }
  } catch {
    // 分类加载失败不影响搜索
  }
}

onMounted(() => {
  fetchCategoryOptions()
})

const renderedAnswer = computed(() => {
  if (!result.value?.answer) return ''
  let text = result.value.answer
  // 把图片ID引用替换为缩略图链接
  const recs = result.value.recommendations || []
  const urlMap = new Map<number, { url?: string; title?: string }>()
  for (const r of recs) {
    if (r.pictureId) urlMap.set(r.pictureId, { url: r.pictureThumbnailUrl || r.pictureUrl, title: r.title })
  }
  text = text.replace(/图片ID[：:]\s*(\d+)/g, (_, idStr) => {
    const id = Number(idStr)
    const info = urlMap.get(id)
    if (info?.url) {
      return `[![${info.title || '图片'}](${info.url})](/picture/${id})`
    }
    return `[图片 #${id}](/picture/${id})`
  })
  return marked(text)
})

const doSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索描述')
    return
  }

  loading.value = true
  hasSearched.value = true
  result.value = null

  try {
    const params: API.AiSearchRequest = {
      query: searchQuery.value.trim(),
      topK: 5,
    }
    if (form.category) {
      params.category = form.category
    }
    if (form.color) {
      params.color = form.color
    }

    const res = await aiSearchUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      result.value = res.data.data
      // 加载推荐图片的真实 URL
      await loadPictureUrls()
    } else {
      ElMessage.error('AI 搜索失败：' + res.data.message)
    }
  } catch (e: any) {
    ElMessage.error('AI 搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载推荐图片的真实 URL
const loadPictureUrls = async () => {
  if (!result.value?.recommendations) {
    return
  }
  const promises = result.value.recommendations.map(async (item) => {
    if (!item.pictureId) return
    try {
      const res = await getPictureVoByIdUsingGet({ id: item.pictureId })
      if (res.data.code === 0 && res.data.data) {
        item.pictureUrl = res.data.data.url
        item.pictureThumbnailUrl = res.data.data.thumbnailUrl
      }
    } catch {
      // 图片加载失败不影响整体展示
    }
  })
  await Promise.all(promises)
}

const goToDetail = (pictureId?: number) => {
  if (pictureId) {
    router.push(`/picture/${pictureId}`)
  }
}

const onAnswerClick = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  const link = target.closest('a')
  if (link) {
    const href = link.getAttribute('href')
    if (href && href.startsWith('/')) {
      e.preventDefault()
      router.push(href)
    }
  }
}
</script>

<style scoped>
#aiSearchPage {
  max-width: 960px;
  margin: 0 auto;
}

.search-bar {
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.section {
  margin-bottom: 24px;
}

.section h3 {
  margin-bottom: 12px;
  font-size: 16px;
}

.intent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.intent-row {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  flex-wrap: wrap;
}

.intent-preview {
  display: flex;
  gap: 10px;
}

.intent-preview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.intent-preview-item:hover {
  transform: translateY(-2px);
}

.intent-preview-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  display: block;
}

.intent-preview-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

.intent-preview-title {
  font-size: 11px;
  color: #909399;
  max-width: 64px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.answer-card {
  background: #f8f9fa;
}

.markdown-body {
  line-height: 1.8;
  color: #606266;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 16px 0 8px;
  font-weight: 600;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(p) {
  margin: 8px 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}

.markdown-body :deep(img) {
  max-width: 120px;
  max-height: 80px;
  border-radius: 4px;
  vertical-align: middle;
  margin: 0 4px;
}

.markdown-body :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.markdown-body :deep(a):hover {
  text-decoration: underline;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.recommendation-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.recommendation-card:hover {
  transform: translateY(-2px);
}

.recommendation-image {
  width: 100%;
  height: 180px;
  display: block;
}

.recommendation-image-placeholder {
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.recommendation-info {
  padding: 12px 0 0;
}

.recommendation-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 15px;
}

.recommendation-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.score {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}

.recommendation-reason {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.initial-tip,
.empty-tip {
  margin-top: 48px;
}
</style>
