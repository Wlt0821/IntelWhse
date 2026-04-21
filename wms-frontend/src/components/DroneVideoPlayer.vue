<template>
  <div class="drone-video-player">
    <div class="player-header">
      <h3>智能盘点</h3>
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
        <p>点击"开始盘点"连接无人机</p>
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
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="视频流地址">
          {{ currentStreamUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="播放模式">
          {{ directPlay ? '直接播放' : '转码播放' }}
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
    default: 'http://127.0.0.1:8888/live/test/'
  }
})

const videoRef = ref(null)
const streamStatus = ref(false)
const loading = ref(false)
const error = ref('')
const hls = ref(null)
const directPlay = ref(true) // 默认直接播放
const currentStreamUrl = ref(props.streamUrl)

const serverUrl = 'http://localhost:8085'
const hlsStreamUrl = `${serverUrl}/stream.m3u8`

// 开始直播
async function startStream() {
  loading.value = true
  error.value = ''
  
  try {
    // 检查流地址格式
    const streamUrl = currentStreamUrl.value
    console.log('开始播放流:', streamUrl)
    
    // 强制使用WebRTC播放（8889端口）
    console.log('使用WebRTC播放')
    
    // 直接使用WebRTC API
    if (!window.RTCPeerConnection) {
      throw new Error('您的浏览器不支持WebRTC')
    }
    
    if (videoRef.value) {
      try {
        // 解析WebRTC URL
        const parsedUrl = new URL(streamUrl)
        const wsUrl = `ws://${parsedUrl.host}${parsedUrl.pathname}ws`
        
        console.log('尝试连接WebRTC:', wsUrl)
        
        // 创建WebSocket连接获取SDP
        const ws = new WebSocket(wsUrl)
        
        ws.onopen = () => {
          console.log('WebSocket连接成功')
        }
        
        ws.onmessage = (event) => {
          try {
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
        
      } catch (e) {
        console.error('WebRTC播放失败:', e)
        error.value = 'WebRTC播放失败: ' + e.message
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

// 直接播放
async function playDirectStream(url) {
  if (videoRef.value) {
    videoRef.value.src = url
    videoRef.value.load()
    
    // 监听播放事件
    videoRef.value.addEventListener('loadedmetadata', () => {
      videoRef.value.play().catch(e => {
        console.warn('自动播放失败，需用户交互:', e)
      })
      streamStatus.value = true
      ElMessage.success('实时直播已开始（直接播放）')
    })
    
    // 监听错误事件
    videoRef.value.addEventListener('error', (e) => {
      console.error('视频播放错误:', e)
      error.value = '视频播放失败，请检查流地址'
      streamStatus.value = false
    })
  }
}

// 播放HLS流
async function playHlsStream(url) {
  // 动态加载hls.js
  const Hls = await loadHls()
  
  // 检查HLS支持
  if (Hls.isSupported()) {
    hls.value = new Hls({
      enableWorker: true,
      lowLatencyMode: true,
      maxBufferLength: 3,
      maxMaxBufferLength: 5,
      startLevel: -1,
      backBufferLength: 0,
      maxBufferSize: 0
    })
    
    hls.value.loadSource(url)
    hls.value.attachMedia(videoRef.value)
    
    hls.value.on(Hls.Events.MANIFEST_PARSED, () => {
      videoRef.value.play().catch(e => {
        console.warn('自动播放失败，需用户交互:', e)
      })
      streamStatus.value = true
      ElMessage.success('实时直播已开始（HLS播放）')
    })
    
    hls.value.on(Hls.Events.ERROR, (event, data) => {
      console.log('HLS Error:', data)
      if (data.fatal) {
        switch (data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            console.error('网络错误，尝试恢复...')
            hls.value.startLoad()
            break
          case Hls.ErrorTypes.MEDIA_ERROR:
            console.error('媒体错误，尝试恢复...')
            hls.value.recoverMediaError()
            break
          default:
            if (data.details === 'internalException') {
              error.value = '直播已结束，请重新开始'
            } else {
              error.value = '播放错误: ' + data.details
            }
            hls.value.destroy()
            streamStatus.value = false
            break
        }
      }
    })
  } else if (videoRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari原生支持HLS
    videoRef.value.src = url
    videoRef.value.addEventListener('loadedmetadata', () => {
      videoRef.value.play().catch(e => {
        console.warn('自动播放失败:', e)
      })
      streamStatus.value = true
      ElMessage.success('实时直播已开始（HLS播放）')
    })
  } else {
    throw new Error('您的浏览器不支持HLS播放')
  }
}

// 播放FLV流
async function playFlvStream(url) {
  // 动态加载flv.js
  const flvjs = await loadFlvJs()
  
  if (flvjs.isSupported()) {
    const flvPlayer = flvjs.createPlayer({
      type: 'flv',
      url: url
    })
    
    // 存储播放器实例到video元素
    if (videoRef.value) {
      videoRef.value._flvPlayer = flvPlayer
    }
    
    flvPlayer.attachMediaElement(videoRef.value)
    flvPlayer.load()
    flvPlayer.play().catch(e => {
      console.warn('自动播放失败，需用户交互:', e)
    })
    
    flvPlayer.on(flvjs.Events.PLAYBACK_STARTED, () => {
      streamStatus.value = true
      ElMessage.success('实时直播已开始（FLV播放）')
    })
    
    flvPlayer.on(flvjs.Events.ERROR, (err) => {
      console.error('FLV播放错误:', err)
      error.value = 'FLV播放失败: ' + err
      streamStatus.value = false
    })
  } else {
    throw new Error('您的浏览器不支持FLV播放')
  }
}

// 动态加载flv.js
async function loadFlvJs() {
  if (window.flvjs) {
    return window.flvjs
  }
  
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/flv.js@latest'
    script.onload = () => resolve(window.flvjs)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// 播放WebRTC流
async function playWebRtcStream(url) {
  if (!window.RTCPeerConnection) {
    throw new Error('您的浏览器不支持WebRTC')
  }
  
  if (videoRef.value) {
    try {
      // 解析WebRTC URL
      const parsedUrl = new URL(url)
      const wsUrl = `ws://${parsedUrl.host}${parsedUrl.pathname}ws`
      
      console.log('尝试连接WebRTC:', wsUrl)
      
      // 创建WebSocket连接获取SDP
      const ws = new WebSocket(wsUrl)
      
      ws.onopen = () => {
        console.log('WebSocket连接成功')
      }
      
      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'offer') {
            handleWebRtcOffer(data.offer)
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
      async function handleWebRtcOffer(offer) {
        try {
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
      
    } catch (e) {
      console.error('WebRTC播放失败:', e)
      error.value = 'WebRTC播放失败: ' + e.message
      streamStatus.value = false
    }
  }
}

// 停止直播
function stopStream() {
  try {
    // 停止hls.js
    if (hls.value) {
      hls.value.destroy()
      hls.value = null
    }
    
    // 停止flv.js
    if (window.flvjs && window.flvjs.Player) {
      // 清理所有flv播放器实例
      if (videoRef.value) {
        const player = videoRef.value._flvPlayer
        if (player) {
          player.destroy()
          videoRef.value._flvPlayer = null
        }
      }
    }
    
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

// 动态加载hls.js
async function loadHls() {
  if (window.Hls) {
    return window.Hls
  }
  
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest'
    script.onload = () => resolve(window.Hls)
    script.onerror = reject
    document.head.appendChild(script)
  })
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
  if (hls.value) {
    hls.value.destroy()
  }
  
  if (videoRef.value) {
    videoRef.value.pause()
    videoRef.value.src = ''
  }
})
</script>

<style scoped>
.drone-video-player {
  background: #f5f7fa;
  border-radius: 8px;
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
  padding: 6px 12px;
  background: #fff;
  border-radius: 20px;
  font-size: 14px;
  color: #909399;
}

.status-indicator.active {
  color: #67c23a;
}

.status-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}

.status-indicator.active .dot {
  background: #67c23a;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.video-container {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.video-player {
  width: 100%;
  height: 100%;
  display: block;
}

.placeholder,
.error-message {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.placeholder .el-icon,
.error-message .el-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-message {
  color: #f56c6c;
}

.control-panel {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.stream-info {
  margin-top: 16px;
}
</style>
