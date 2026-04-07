<template>
  <div class="chatbot-container">
    <div v-if="!isOpen" class="chatbot-trigger" @click="toggleChat">
      <el-icon class="trigger-icon"><ChatDotRound /></el-icon>
    </div>
    
    <div v-else class="chatbot-window">
      <div class="chatbot-header">
        <div class="header-left">
          <el-icon class="bot-icon"><Service /></el-icon>
          <span class="bot-title">智慧仓储管理助手</span>
        </div>
        <div class="header-right">
          <el-icon class="close-icon" @click="toggleChat"><Close /></el-icon>
        </div>
      </div>
      
      <div class="chatbot-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-message">
          <el-icon class="welcome-icon"><ChatDotRound /></el-icon>
          <p>您好！我是智慧物流仓储智能助手</p>
          <p class="welcome-hint">有什么可以帮助您的？</p>
        </div>
        
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'user'"><User /></el-icon>
            <el-icon v-else><Service /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>
        
        <div v-if="isLoading" class="message-item assistant">
          <div class="message-avatar">
            <el-icon><Service /></el-icon>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chatbot-input">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题..."
          @keydown.enter.ctrl="sendMessage"
          @keydown.enter.meta="sendMessage"
        />
        <div class="input-actions">
          <span class="input-hint">Ctrl+Enter 发送</span>
          <el-button type="primary" :loading="isLoading" @click="sendMessage">
            发送
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Service, Close, User, Promotion } from '@element-plus/icons-vue'

const isOpen = ref(false)
const isLoading = ref(false)
const inputText = ref('')
const messages = ref([])
const messagesContainer = ref(null)

const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return

  const userMsg = inputText.value
  let apiMsg = userMsg
  
  if (userMsg.trim() === '执行库存检查') {
    apiMsg = '执行任务 c72c960b 并返回结果'
    console.log('检测到库存检查命令，替换为:', apiMsg)
  }
  
  messages.value.push({ role: 'user', content: userMsg })
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const COPAWCONFIG = {
      session_id: "WMS_ChatBOT",   // 自定义会话ID（不变=续聊）
      agent_id: "YOUR_AGENT_ID"      // 请替换为您的agent_id
    }
    console.log('发送请求到 CoPaw...')
    
    const response = await fetch('/api/agent/process', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        input: [
          {
            role: 'user',
            content: [{ type: 'text', text: apiMsg }]
          }
        ],
        agent_id: COPAWCONFIG.agent_id,
        session_id: COPAWCONFIG.session_id
      })
    })

    console.log('响应状态:', response.status, response.statusText)

    if (!response.ok) {
      const errorText = await response.text()
      console.error('错误响应内容:', errorText)
      throw new Error(`HTTP error! status: ${response.status}, body: ${errorText}`)
    }

    const contentType = response.headers.get('content-type') || ''
    console.log('Content-Type:', contentType)

    let finalAnswer = ''

    if (contentType.includes('text/event-stream')) {
      console.log('处理 SSE 流式响应...')
      
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmedLine = line.trim()
          console.log('收到行:', trimmedLine)
          
          if (!trimmedLine || !trimmedLine.startsWith('data:')) continue

          try {
            const jsonStr = trimmedLine.slice(5).trim()
            
            if (jsonStr === '[DONE]') continue

            const json = JSON.parse(jsonStr)
            console.log('解析的 JSON:', json)

            if (json.object === 'response' && json.status === 'completed') {
              for (const msg of json.output || []) {
                if (msg.role === 'assistant' && msg.type === 'message') {
                  for (const c of msg.content || []) {
                    finalAnswer += c.text || ''
                  }
                }
              }
            }
          } catch (e) {
            console.error('解析 JSON 失败:', e, '行内容:', trimmedLine)
          }
        }
      }
    } else {
      console.log('处理非流式响应...')
      
      const json = await response.json()
      console.log('响应 JSON:', json)
      
      if (json.output) {
        for (const msg of json.output || []) {
          if (msg.role === 'assistant' && msg.type === 'message') {
            for (const c of msg.content || []) {
              finalAnswer += c.text || ''
            }
          }
        }
      } else if (json.response) {
        finalAnswer = json.response
      } else if (json.message) {
        finalAnswer = json.message
      } else if (typeof json === 'string') {
        finalAnswer = json
      }
    }

    messages.value.push({
      role: 'assistant',
      content: finalAnswer.trim() || '抱歉，我没有收到回复内容。'
    })

  } catch (err) {
    console.error('请求失败', err)
    messages.value.push({
      role: 'assistant',
      content: `请求失败: ${err.message}。请查看浏览器控制台获取更多详细信息。`
    })
    ElMessage.error('请求失败，请稍后重试')
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chatbot-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
}

.chatbot-trigger {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  transition: all 0.3s ease;
}

.chatbot-trigger:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.5);
}

.trigger-icon {
  font-size: 28px;
  color: white;
}

.chatbot-window {
  width: 420px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chatbot-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bot-icon {
  font-size: 24px;
  color: white;
}

.bot-title {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  cursor: pointer;
}

.close-icon {
  font-size: 20px;
  color: white;
  transition: transform 0.2s ease;
}

.close-icon:hover {
  transform: rotate(90deg);
}

.chatbot-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.welcome-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 16px;
}

.welcome-message p {
  margin: 8px 0;
  font-size: 14px;
}

.welcome-hint {
  color: #999;
  font-size: 13px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-avatar .el-icon {
  font-size: 20px;
  color: white;
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-item.assistant .message-text {
  background: white;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border-top-right-radius: 4px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1890ff;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chatbot-input {
  padding: 16px;
  background: white;
  border-top: 1px solid #e8e8e8;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-hint {
  font-size: 12px;
  color: #999;
}

.chatbot-messages::-webkit-scrollbar {
  width: 6px;
}

.chatbot-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chatbot-messages::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.chatbot-messages::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>
