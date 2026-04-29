<template>
  <div class="simple-hls-player">
    <div class="player-topbar">
      <div class="topbar-left">
        <span class="title-icon">◈</span>
        <span>智能盘点</span>
      </div>
      <div class="topbar-right">
        <div class="status-indicator" :class="{ active: streamStatus }">
          <span class="dot"></span>
          <span>{{ streamStatus ? '直播中' : '未连接' }}</span>
        </div>
        <el-button type="primary" link @click="openConfigDialog" class="config-btn">
          <el-icon><Setting /></el-icon>
          配置
        </el-button>
      </div>
    </div>

    <div class="drone-selector">
      <div
        v-for="drone in drones"
        :key="drone.id"
        class="drone-tab"
        :class="{ active: selectedDrone === drone.id }"
        @click="switchDrone(drone.id)"
      >
        <span class="drone-name">{{ drone.name }}</span>
        <span v-if="selectedDrone === drone.id" class="active-line"></span>
      </div>
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
          <div class="placeholder-icon">📹</div>
          <p>点击"开始盘点"连接无人机</p>
          <span class="placeholder-sub">DRONE CONNECTION STANDBY</span>
        </div>
      </div>
    </div>

    <div class="control-panel">
      <el-button
        type="primary"
        :loading="loading"
        :disabled="streamStatus"
        @click="startStream"
        size="large"
        class="start-btn"
      >
        <el-icon><VideoPlay /></el-icon>
        开始盘点
      </el-button>
      <el-button
        type="danger"
        :disabled="!streamStatus"
        @click="stopStream"
        size="large"
        class="stop-btn"
      >
        <el-icon><VideoPause /></el-icon>
        停止盘点
      </el-button>
      <el-button @click="refreshStream" size="large" class="refresh-btn">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="stream-info" v-if="streamStatus">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="当前无人机">{{ currentDroneName }}</el-descriptions-item>
        <el-descriptions-item label="视频流地址">{{ currentStreamUrl }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <el-dialog v-model="configDialogVisible" title="无人机配置" width="600px" :close-on-click-modal="false">
      <el-form :model="configForm" label-width="100px">
        <el-form-item v-for="drone in drones" :key="drone.id" :label="drone.name">
          <el-input v-model="configForm[drone.id]" :placeholder="'请输入' + drone.name + '的流地址'" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, VideoPause, Refresh, Setting } from '@element-plus/icons-vue'

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

const currentDrone = computed(() => drones.value.find(d => d.id === selectedDrone.value) || drones.value[0])
const currentDroneName = computed(() => currentDrone.value.name)
const currentStreamUrl = computed(() => currentDrone.value.url)

function initConfigForm() {
  configForm.value = {}
  drones.value.forEach(drone => { configForm.value[drone.id] = drone.url })
}

function openConfigDialog() {
  initConfigForm()
  configDialogVisible.value = true
}

function saveConfig() {
  drones.value.forEach(drone => { if (configForm.value[drone.id]) drone.url = configForm.value[drone.id] })
  localStorage.setItem('droneConfigs', JSON.stringify(drones.value))
  configDialogVisible.value = false
  ElMessage.success('配置已保存')
}

function switchDrone(id) {
  selectedDrone.value = id
  if (streamStatus.value) refreshStream()
}

function startStream() {
  loading.value = true
  try {
    streamStatus.value = true
    ElMessage.success('直播已开始')
  } catch (err) {
    console.error('启动失败:', err)
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
    ElMessage.error('停止失败')
  }
}

function refreshStream() {
  if (streamStatus.value) { stopStream(); setTimeout(() => startStream(), 500) }
  else startStream()
}

onMounted(() => {
  const saved = localStorage.getItem('droneConfigs')
  if (saved) {
    try {
      const p = JSON.parse(saved)
      if (Array.isArray(p) && p.length === 5) drones.value = p
    } catch (e) {}
  }
})
</script>

<style scoped>
.simple-hls-player {
  background: rgba(10, 20, 40, 0.85);
  border-radius: 10px;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.3), 0 0 50px rgba(0, 240, 255, 0.05);
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: auto !important;
  max-height: none !important;
  min-height: auto !important;
  position: relative;
}

/* ====== 顶部栏（合并标题+状态）====== */
.player-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  width: 100%;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--cyber-border);
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family:'Orbitron',sans-serif;
  color: var(--cyber-primary);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  text-shadow: 0 0 10px rgba(0,240,255,.4);
}

.title-icon{font-size:15px;filter:drop-shadow(0 0 6px currentColor)}

.topbar-right{display:flex;align-items:center;gap:16px}

.status-indicator{
  display:flex;align-items:center;gap:7px;
  padding:4px 12px;background:rgba(0,20,40,.6);
  border:1px solid var(--cyber-border);border-radius:4px;
  font-family:'Rajdhani',sans-serif;font-size:11px;
  letter-spacing:1px;text-transform:uppercase;color:var(--cyber-text-dim);
}
.status-indicator .dot{width:8px;height:8px;border-radius:50%;background:#909399;transition:all .3s}
.status-indicator.active .dot{
  background:var(--cyber-accent);box-shadow:0 0 10px var(--cyber-accent),0 0 20px rgba(0,255,136,.3);
  animation:pulse 2s infinite;
}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.4}}
.status-indicator.active{color:var(--cyber-accent);border-color:rgba(0,255,136,.25)}
.config-btn{color:var(--cyber-text-dim)!important}
.config-btn:hover{color:var(--cyber-primary)!important}

/* ====== 无人机标签栏 ====== */
.drone-selector {
  display: flex;
  gap: 4px;
  margin-bottom: 10px;
  flex-shrink: 0;
  width: 100%;
  justify-content: center;
  padding: 5px;
  background: rgba(0, 20, 40, 0.35);
  border: 1px solid var(--cyber-border);
  border-radius: 8px;
}

.drone-tab {
  position: relative;
  padding: 8px 22px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s ease;
  user-select: none;
  border: 1px solid transparent;
  background: transparent;
}

.drone-tab .drone-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--cyber-text-dim);
  letter-spacing: 1px;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.drone-tab:hover {
  background: rgba(0, 240, 255, 0.06);
  border-color: rgba(0, 240, 255, 0.2);
}

.drone-tab:hover .drone-name {
  color: var(--cyber-primary);
}

/* 选中状态 - 赛博发光 */
.drone-tab.active {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.18) 0%, rgba(0, 102, 255, 0.08) 100%);
  border-color: var(--cyber-primary);
  box-shadow:
    0 0 15px rgba(0, 240, 255, 0.25),
    inset 0 0 15px rgba(0, 240, 255, 0.06);
}

.drone-tab.active .drone-name {
  color: var(--cyber-primary);
  font-weight: 700;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.4);
}

.active-line {
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 2px;
  background: var(--cyber-primary);
  box-shadow: 0 0 8px var(--cyber-primary);
  animation: line-glow 2s ease-in-out infinite;
}

@keyframes line-glow {
  0%, 100% { opacity: 0.5; width: 60%; left: 20%; }
  50% { opacity: 1; width: 80%; left: 10%; }
}

/* ====== 视频区 ====== */
.video-container{
  width:100%;
  margin-bottom:8px;
  flex-shrink:0;
  display:flex;
  justify-content:center;
  height:480px!important;
  max-height:480px!important;
  overflow:hidden;
}
.video-wrapper{
  position:relative;
  width:100%;
  max-width:900px;
  height:480px;
  background:#000;
  border-radius:8px;
  overflow:hidden;
  border:1px solid var(--cyber-border);
  box-shadow:0 0 25px rgba(0,0,0,.4),inset 0 0 60px rgba(0,240,255,.03);
}
.video-iframe{width:100%;height:100%;border:none}
.placeholder{
  width:100%;height:100%;display:flex;flex-direction:column;
  justify-content:center;align-items:center;color:var(--cyber-text-dim);
  background:radial-gradient(circle at center,rgba(0,240,255,.04) 0%,#000 70%);
}
.placeholder-icon{font-size:56px;margin-bottom:16px;opacity:.45;animation:icon-float 3s ease-in-out infinite}
@keyframes icon-float{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}
.placeholder p{margin:0;font-size:16px;font-weight:500;color:var(--cyber-text);letter-spacing:1px}
.placeholder-sub{
  margin-top:6px;font-size:9px;font-family:'Orbitron',monospace;
  letter-spacing:3px;color:var(--cyber-text-dim);opacity:.5;text-transform:uppercase;
}

/* ====== 控制面板 ====== */
.control-panel{display:flex;gap:14px;margin-bottom:10px;flex-shrink:0;padding:4px 0}

.start-btn{
  background:linear-gradient(135deg,rgba(0,240,255,.18),rgba(0,102,255,.12))!important;
  border-color:var(--cyber-primary)!important;
  color:var(--cyber-primary)!important;font-weight:700!important;
  letter-spacing:2px!important;
  box-shadow:0 0 20px rgba(0,240,255,.3)!important;
  clip-path:polygon(10px 0,100% 0,100% calc(100%-10px),calc(100%-10px) 100%,0 100%,0 10px)!important;
}
.start-btn:hover{
  background:var(--cyber-primary)!important;color:var(--cyber-bg-dark)!important;
  box-shadow:0 0 30px rgba(0,240,255,.55),0 0 60px rgba(0,240,255,.28)!important;
  transform:translateY(-1px)!important;
}
.stop-btn{
  background:rgba(255,51,102,.1)!important;border-color:var(--cyber-danger)!important;
  color:var(--cyber-danger)!important;font-weight:700!important;
}
.stop-btn:hover{background:var(--cyber-danger)!important;color:#fff!important}
.refresh-btn{
  background:rgba(0,20,40,.5)!important;border-color:var(--cyber-border)!important;
  color:var(--cyber-text-dim)!important;
}
.refresh-btn:hover{border-color:var(--cyber-primary)!important;color:var(--cyber-primary)!important}

.stream-info{flex-shrink:0;width:100%}
.stream-info :deep(.el-descriptions){background:transparent!important;border-color:var(--cyber-border)!important}
.stream-info :deep(.el-descriptions__label){color:var(--cyber-text-dim)!important;background:rgba(0,240,255,.04)!important}
.stream-info :deep(.el-descriptions__content){color:var(--cyber-text)!important}
</style>
