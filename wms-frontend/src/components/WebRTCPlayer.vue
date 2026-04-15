<template>
  <div class="web-rtc-player">
    <div class="player-header">
      <h3>无人机视频监控 (WebRTC)</h3>
      <div class="status-indicator" :class="{ active: streamStatus }">
        <span class="dot"></span>
        <span>{{ streamStatus ? '直播中' : '未连接' }}</span>
      </div>
    </div>

    <div class="video-container">
      <video
        ref="videoRef"
        class="video-player"
        autoplay
        muted
        playsinline
        controlsList="nodownload"
        controls
      ></video>
      <div v-if="!streamStatus && !error" class="placeholder">
        <el-icon><VideoCamera /></el-icon>
        <p>点击"开始直播"连接无人机视频</p>
      </div>
      <div v-if="error" class="error-message">
        <el-icon><WarningFilled /></el-icon>
        <p>{{ error }}</p>
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
        开始直播
      </el-button>
      <el-button
        type="danger"
        :disabled="!streamStatus"
        @click="stopStream"
      >
        <el-icon><VideoPause /></el-icon>
        停止直播
      </el-button>
      <el-button @click="refreshStream">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="stream-info" v-if="streamStatus">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="视频流地址">
          {{ currentStreamUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="播放模式">
          WebRTC播放
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoCamera,
  VideoPlay,
  VideoPause,
  Refresh,
  WarningFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  streamUrl: {
    type: String,
    default: 'http://127.0.0.1:8889/live/test/'
  }
})

const videoRef = ref(null)
const streamStatus = ref(false)
const loading = ref(false)
const error = ref('')
const currentStreamUrl = ref(props.streamUrl)

// 开始直播
async function startStream() {
  loading.value = true
  error.value = ''
  
  try {
    console.log('开始播放WebRTC流:', currentStreamUrl.value)
    
    // 检查浏览器是否支持WebRTC
    if (!window.RTCPeerConnection) {
      throw new Error('您的浏览器不支持WebRTC')
    }
    
    if (!videoRef.value) {
      throw new Error('视频元素未准备就绪')
    }
    
    // 解析WebRTC URL
    const parsedUrl = new URL(currentStreamUrl.value)
    const wsUrl = `ws://${parsedUrl.host}${parsedUrl.pathname}ws`
    
    console.log('尝试连接WebRTC WebSocket:', wsUrl)
    
    // 创建WebSocket连接获取SDP
    const ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log('WebSocket连接成功')
    }
    
    ws.onmessage = (event) => {
      try {
        console.log('收到WebSocket消息:', event.data)
        const data = JSON.parse(event.data)
        if (data.type === 'offer') {
          handleWebRtcOffer(data.offer, ws)
        }
      } catch (e) {
        console.error('WebSocket消息解析失败:', e)
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      error.value = 'WebRTC连接失败'
      streamStatus.value = false
    }
    
    ws.onclose = () => {
      console.log('WebSocket连接关闭')
    }
    
    // 处理WebRTC offer
    async function handleWebRtcOffer(offer, ws) {
      try {
        console.log('处理WebRTC offer')
        const pc = new RTCPeerConnection()
        
        // 处理ICE候选
        pc.onicecandidate = (event) => {
          if (event.candidate) {
            ws.send(JSON.stringify({ type: 'ice', candidate: event.candidate }))
          }
        }
        
        // 处理媒体流
        pc.ontrack = (event) => {
          if (event.streams && event.streams[0]) {
            console.log('收到媒体流')
            videoRef.value.srcObject = event.streams[0]
            videoRef.value.play().catch(e => {
              console.warn('自动播放失败，需用户交互:', e)
            })
            streamStatus.value = true
            ElMessage.success('实时直播已开始（WebRTC播放）')
          }
        }
        
        // 处理连接状态
        pc.onconnectionstatechange = () => {
          console.log('WebRTC连接状态:', pc.connectionState)
          if (pc.connectionState === 'failed') {
            error.value = 'WebRTC连接失败'
            streamStatus.value = false
          }
        }
        
        // 设置远程描述
        await pc.setRemoteDescription(new RTCSessionDescription(offer))
        
        // 创建应答
        const answer = await pc.createAnswer()
        await pc.setLocalDescription(answer)
        
        // 发送应答
        ws.send(JSON.stringify({ type: 'answer', answer: answer }))
        
        // 存储peer connection
        if (videoRef.value) {
          videoRef.value._rtcPeerConnection = pc
        }
        
      } catch (e) {
        console.error('WebRTC处理失败:', e)
        error.value = 'WebRTC处理失败: ' + e.message
        streamStatus.value = false
      }
    }
    
  } catch (err) {
    console.error('启动直播失败:', err)
    error.value = err.message || '启动失败'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 停止直播
function stopStream() {
  try {
    // 停止WebRTC
    if (videoRef.value) {
      const pc = videoRef.value._rtcPeerConnection
      if (pc) {
        pc.close()
        videoRef.value._rtcPeerConnection = null
      }
    }
    
    // 停止video
    if (videoRef.value) {
      videoRef.value.pause()
      videoRef.value.src = ''
      videoRef.value.srcObject = null
    }
    
    streamStatus.value = false
    error.value = ''
    ElMessage.success('直播已停止')
  } catch (err) {
    console.error('停止直播失败:', err)
    ElMessage.error('停止失败')
  }
}

// 刷新流
function refreshStream() {
  if (streamStatus.value) {
    stopStream()
    setTimeout(() => startStream(), 1000)
  } else {
    startStream()
  }
}

// 监听流地址变化
watch(() => props.streamUrl, (newUrl) => {
  currentStreamUrl.value = newUrl
  if (streamStatus.value) {
    // 如果正在播放，重新开始
    refreshStream()
  }
})

// 清理
onUnmounted(() => {
  if (videoRef.value) {
    const pc = videoRef.value._rtcPeerConnection
    if (pc) {
      pc.close()
    }
    videoRef.value.pause()
    videoRef.value.src = ''
    videoRef.value.srcObject = null
  }
})
</script>

<style scoped>
.web-rtc-player {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.player-header h3 {
  margin: 0;
  color: #303133;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
  transition: all 0.3s;
}

.status-indicator.active .dot {
  background: #67c23a;
  box-shadow: 0 0 10px #67c23a;
}

.video-container {
  position: relative;
  width: 100%;
  height: 480px;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.placeholder el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #f56c6c;
}

.error-message el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.control-panel {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.stream-info {
  margin-top: 20px;
}
</style>