<template>
  <div class="simple-hls-player">
    <div class="player-header">
      <h3>智能盘点</h3>
      <div class="header-right">
        <div class="status-indicator" :class="{ active: streamStatus }">
          <span class="dot"></span>
          <span>{{ streamStatus ? '直播中' : '未连接' }}</span>
        </div>
        <el-button type="primary" link @click="openConfigDialog">
          <el-icon><Setting /></el-icon>
          配置
        </el-button>
      </div>
    </div>

    <div class="drone-selector">
      <el-radio-group v-model="selectedDrone" size="default" @change="switchDrone">
        <el-radio-button 
          v-for="drone in drones" 
          :key="drone.id" 
          :value="drone.id"
        >
          {{ drone.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="video-container">
      <div class="video-wrapper">
        <iframe
          v-if="streamStatus"
          ref="iframeRef"
          :src="currentStreamUrl"
          class="video-iframe"
          allowfullscreen
        ></iframe>
        
        <div v-if="!streamStatus" class="placeholder">
          <el-icon><VideoCamera /></el-icon>
          <p>点击"开始盘点"连接无人机</p>
        </div>
      </div>
    </div>

    <div class="control-panel">
      <el-button
        type="primary"
        :loading="loading"
        :disabled="streamStatus"
        @click="startStream"
      >
        <el-icon><VideoPlay /></el-icon>
        开始盘点
      </el-button>
      <el-button
        type="danger"
        :disabled="!streamStatus"
        @click="stopStream"
      >
        <el-icon><VideoPause /></el-icon>
        停止盘点
      </el-button>
      <el-button @click="refreshStream">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="stream-info" v-if="streamStatus">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="当前无人机">
          {{ currentDroneName }}
        </el-descriptions-item>
        <el-descriptions-item label="视频流地址">
          {{ currentStreamUrl }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-dialog
      v-model="configDialogVisible"
      title="无人机配置"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="configForm" label-width="100px">
        <el-form-item 
          v-for="drone in drones" 
          :key="drone.id"
          :label="drone.name"
        >
          <el-input 
            v-model="configForm[drone.id]" 
            :placeholder="'请输入' + drone.name + '的流地址'"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">
          保存配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCamera, VideoPlay, VideoPause, Refresh, Setting } from '@element-plus/icons-vue'

const defaultDrones = [
  { id: 1, name: '无人机1', url: 'http://127.0.0.1:8889/live/test/' },
  { id: 2, name: '无人机2', url: 'http://127.0.0.1:8889/live/drone2/' },
  { id: 3, name: '无人机3', url: 'http://127.0.0.1:8889/live/drone3/' },
  { id: 4, name: '无人机4', url: 'http://127.0.0.1:8889/live/drone4/' },
  { id: 5, name: '无人机5', url: 'http://127.0.0.1:8889/live/drone5/' }
]

const drones = ref([...defaultDrones])
const selectedDrone = ref(1)
const streamStatus = ref(false)
const loading = ref(false)
const configDialogVisible = ref(false)

const configForm = ref({})

const currentDrone = computed(() => {
  return drones.value.find(d => d.id === selectedDrone.value) || drones.value[0]
})

const currentDroneName = computed(() => currentDrone.value.name)
const currentStreamUrl = computed(() => currentDrone.value.url)

function initConfigForm() {
  configForm.value = {}
  drones.value.forEach(drone => {
    configForm.value[drone.id] = drone.url
  })
}

function openConfigDialog() {
  initConfigForm()
  configDialogVisible.value = true
}

function saveConfig() {
  drones.value.forEach(drone => {
    if (configForm.value[drone.id]) {
      drone.url = configForm.value[drone.id]
    }
  })
  
  localStorage.setItem('droneConfigs', JSON.stringify(drones.value))
  configDialogVisible.value = false
  ElMessage.success('配置已保存')
}

function switchDrone() {
  if (streamStatus.value) {
    refreshStream()
  }
  ElMessage.success(`已切换到${currentDroneName.value}`)
}

function startStream() {
  loading.value = true
  try {
    streamStatus.value = true
    ElMessage.success('直播已开始')
  } catch (err) {
    console.error('启动直播失败:', err)
    ElMessage.error('启动失败')
  } finally {
    loading.value = false
  }
}

function stopStream() {
  try {
    streamStatus.value = false
    ElMessage.success('直播已停止')
  } catch (err) {
    console.error('停止直播失败:', err)
    ElMessage.error('停止失败')
  }
}

function refreshStream() {
  if (streamStatus.value) {
    stopStream()
    setTimeout(() => startStream(), 500)
  } else {
    startStream()
  }
}

onMounted(() => {
  const savedConfigs = localStorage.getItem('droneConfigs')
  if (savedConfigs) {
    try {
      const parsed = JSON.parse(savedConfigs)
      if (Array.isArray(parsed) && parsed.length === 5) {
        drones.value = parsed
      }
    } catch (e) {
      console.error('加载配置失败:', e)
    }
  }
})
</script>

<style scoped>
.simple-hls-player {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 8px 24px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-shrink: 0;
  width: 100%;
}

.player-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #909399;
  transition: all 0.3s;
}

.status-indicator.active .dot {
  background: #67c23a;
  box-shadow: 0 0 10px #67c23a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.drone-selector {
  margin-bottom: 10px;
  flex-shrink: 0;
}

.video-container {
  width: 100%;
  margin-bottom: 12px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

.video-wrapper {
  position: relative;
  width: 100%;
  max-width: 900px;
  height: 450px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.video-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.placeholder :deep(.el-icon) {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.placeholder p {
  margin: 0;
  font-size: 16px;
}

.control-panel {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.stream-info {
  flex-shrink: 0;
}
</style>
