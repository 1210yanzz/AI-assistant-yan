<template>
  <div class="consultation-container">
    <!-- 左边 -->
    <div class="sidebar">
      <!-- AI助手信息 -->
      <div class="ai-assistant-info">
        <div class="breathing-circle">
          <el-image :src="iconUrl" style="width: 35px; height: 35px;" />
        </div>
        <h3 class="assistant-name">栖心AI</h3>
        <div class="online-status">
          <div class="status-dot"></div>
          online
        </div>
      </div>
      <!-- 情绪花园 -->
      <div class="emotion-garden">
        <div class="garden-header">
          <div class="garden-title">情绪花园</div>
        </div>
        <div class="emotion-info">
          <div class="emotion-name">中性</div>
          <div class="emotion-score">50</div>
        </div>
        <div class="warm-tips">
          <div class="emotion-status-text">
            <span class="status-label">今天感觉</span>
            <span class="status-emotion">{{ currentEmotion.isNegative ? '需要关注' : '很不错' }}</span>
          </div>
          <div class="emotion-intensity">
            <div class="intensity-dots">
              <span v-for="dot in 3" :key="dot"
                :class="{ 'active': getIntensityClass(currentEmotion.emotionScore) >= dot }" class="dot"></span>
              <span class="intensity-text">{{ getRiskText(currentEmotion.riskLevel) }}</span>
            </div>
          </div>
          <!-- 卡片 -->
          <div class="warm-suggestion" v-if="currentEmotion.suggestion">
            <div class="suggestion-icon">💖</div>
            <div class="suggestion-content">
              <div class="suggestion-title">给你的小建议</div>
              <div class="suggestion-text">{{ currentEmotion.suggestion }}</div>
            </div>
          </div>
          <!-- 治愈行动清单 -->
          <div class="healing-actions" v-if="currentEmotion.improvementSuggestions.length > 0">
            <div class="actions-title">治愈小行动</div>
            <div class="actions-list">
              <!-- 这里可以放具体的行动项 -->
              <div class="action-item" v-for="action in currentEmotion.improvementSuggestions" :key="action">
                <div class="action-icon">✨</div>
                <div class="action-text">{{ action }}</div>
              </div>
            </div>
          </div>
          <!-- 风险提醒 -->
          <div class="risk-notice" v-if="currentEmotion.isNegative && currentEmotion.riskLevel > 1">
            <div class="notice-icon">🤗</div>
            <div class="notice-content">
              <div class="notice-title">温馨提示</div>
              <div class="notice-text">{{ currentEmotion.riskDescription }}</div>
            </div>
          </div>
        </div>
      </div>
      <!-- 历史会话列表 -->
      <div class="session-history">
        <h4 class="section-title">会话列表</h4>
        <div class="session-list">
          <div class="session-item" v-for="session in sessionList" :key="session.id"
            @click="handleSessionClick(session)">
            <div class="session-info">
              <div class="session-title">
                <span>{{ session.sessionTitle }}</span>
                <div class="session-meta">
                  <span class="session-time">{{ session.startedAt }}</span>
                </div>
                <div class="session-preview">
                  {{ session.lastMessageContent }}
                </div>
                <div class="session-stats">
                  <span>
                    <el-icon>
                      <ChatRound />
                    </el-icon>
                    {{ session.messageCount || 0 }}
                  </span>
                  <span>
                    <el-icon>
                      <Clock />
                    </el-icon>
                    {{ session.durationMinutes || 0 }} 分钟
                  </span>
                </div>
              </div>
              <div class="session-actions">
                <el-button text type="danger" size="small" @click="handleDeleteSession(session.id)">
                  <el-icon>
                    <DeleteFilled />
                  </el-icon>
                </el-button>

              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 右边 -->
    <div class="chat-main">
      <!-- 会话头 -->
      <div class="chat-header">
        <div class="header-left">
          <div class="chat-avatar">
            <el-image :src="iconUrl1" style="width: 30px; height: 30px;" />
          </div>
          <div class="chat-info">
            <h2>栖心AI助手</h2>
            <p>您的贴心心理AI健康助手，时刻倾听</p>
          </div>
        </div>
        <el-button circle title="新建会话" @click="createNewFrontendSession">
          <el-icon>
            <Plus />
          </el-icon>
        </el-button>
      </div>
      <!-- 会话内容 -->
      <div class="chat-messages">
        <!-- 首次聊天出现欢迎语 -->
        <div v-if="messages.length === 0" class="message-item ai-message">
          <div class="message-avatar">
            <el-image :src="iconUrl" style="width: 23px; height: 23px;" />
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <p>欢迎来到栖心AI助手，我是一个智能的AI助手，我帮助您管理心理健康，请开始你的journey吧！今天您感觉怎么样？有什么想要分享的吗？</p>
            </div>
            <div class="message-time">
              刚刚
            </div>
          </div>
        </div>
        <!-- 不是首次聊天，显示会话内容 -->
        <div class="message-item" v-for="msg in messages" :key="msg.id"
          :class="msg.senderType === 1 ? 'user-message' : 'ai-message'">
          <!-- 消息头像 -->
          <div class="message-avatar">
            <el-image v-if="msg.senderType === 1" :src="iconUrl2" style="width: 23px; height: 23px;" />
            <el-image v-if="msg.senderType === 2" :src="iconUrl" style="width: 23px; height: 23px;" />
          </div>
          <!-- 对话 -->
          <div class="message-content">
            <div class="message-bubble">
              <!-- AL正在思考中 -->
              <!-- 用于等待后端数据返回 -->
              <div v-if="msg.senderType === 2 && isAiTyping && !msg.content" class="typing-indicator">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
              </div>
              <!-- AI错误，比如数据因为网络问题导致返回为空 -->
              <div v-else-if="msg.isError" class="error-message">
                <p>{{ msg.content }}</p>
              </div>
              <!-- AI正常情况返回内容 -->
              <MarkdownRenderer :content="msg.content" v-else-if="msg.senderType === 2 && !msg.isError"
                :is-ai-message="true" />
              <!-- 处理用户的内容 -->
              <p v-else-if="msg.senderType === 1" v-html="formatMessageContent(msg.content)"></p>
            </div>
            <div class="message-time">
              {{ msg.senderType === 2 && isAiTyping ? '正在输入中....' : msg.createdAt }}
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input">

        <div class="input-container">
          <el-input v-model="userMessage" placeholder="请输入想要分享的内容...." type="textarea" clearable :rows="3"
            :disabled="isAiTyping" @keydown="handleKeyDown" class="message-input">
          </el-input>
          <div class="input-footer">
            <span>按Enter发送，Shift+Enter换行</span>
            <span>{{ userMessage.length }}/500</span>
          </div>
        </div>
        <!-- 发送按钮 -->
        <el-button type="primary" class="send-btn" @click="sendMessage":disabled="!userMessage.trim() || userMessage.length > 500">
          <el-icon>
            <Promotion />
          </el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { startSession, getSessionList, deleteSession, getSessionDetail, getSessionEmotion } from '@/api/frontend'
import { useSameTarget } from 'element-plus'
import { ElMessage } from 'element-plus'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import { fetchEventSource } from '@microsoft/fetch-event-source'

const iconUrl = new URL('@/assets/image/ai-image-2.png', import.meta.url).href
const iconUrl1 = new URL('@/assets/image/like.png', import.meta.url).href
const iconUrl2 = new URL('@/assets/image/users.png', import.meta.url).href

// 当前会话对象
const currentSession = ref(null)
// 会话内容
const messages = ref([])
// 用户输入
const userMessage = ref('')
// 是否正在输入中
const isAiTyping = ref(false)
// 历史会话列表
const sessionList = ref([])
// 情绪花园
const currentEmotion = ref({
  primaryEmotion: '中性',
  emotionScore: 50,
  isNegative: false,
  suggestion: '情绪状态平稳',
  riskLevel: 0,
  improvementSuggestions: [],
  riskDescription: ''
})
const loadSessionEmotion = (sessionId) => {
  const id = sessionId.toString().startsWith('session_') ? sessionId : `session_${sessionId}`
  getSessionEmotion(id).then(res => {
    currentEmotion.value = res
  })
}
// 新建会话
const createNewFrontendSession = () => {
  const newSession = {
    // 每个对话对象都需要有一个唯一的标识
    sessionId: `temp_${Date.now()}`,
    // 会话状态
    // TEMP：临时会话，用户可以随时结束
    // ACTIVE：活跃会话，用户可以继续聊天
    status: 'TEMP',
    // 会话标题
    sessionTitle: '新会话',
  }
  currentSession.value = newSession
  //清空消息列表，实现点击加号，新建会话
  messages.value = []
  // 新增：重置情绪花园
  currentEmotion.value = {
    primaryEmotion: '中性',
    emotionScore: 50,
    isNegative: false,
    suggestion: '情绪状态平稳',
    riskLevel: 0,
    improvementSuggestions: [],
    riskDescription: ''
  }
}
// 发送消息
const sendMessage = () => {
  // 检查用户输入是否为空
  if (!userMessage.value.trim()) {
    return
  }
  // 检查AI助手是否正在回复
  if (isAiTyping.value) {
    ElMessage.warning('AI助手正在回复，请稍后再试')
    return
  }
  const message = userMessage.value.trim()
  userMessage.value = ''
  // 如果没有历史会话，就新建一个会话
  if (currentSession.value.status === 'TEMP') {
    // 把输入内容传给startNewSession
    startNewSession(message)
  } else {
    // 实现继续对话
    messages.value.push({
      id: Date.now(),
      senderType: 1,
      content: message,
      createAt: new Date().toISOString()
    })
    // 开始流式对话
    startAIResponse(currentSession.value.sessionId, message)
  }


}
// 处理键盘事件
const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }

}
const startNewSession = (message) => {
  // 新建会话
  const sessionParams = {
    initialMessage: message
  }
  //  如果是新会话，那就要给sessionParams.sessionTitle创建一个独属于的标题
  if (currentSession.value.sessionTitle === '新会话') {
    sessionParams.sessionTitle = `栖心AI助手-${new Date().toLocaleString()}`
  }
  //    不是新会话，那就是历史会话了
  else {
    sessionParams.sessionTitle = currentSession.value.sessionTitle
  }
  // 调用接口创建会话
  startSession(sessionParams).then(res => {
    //接收后端返回来的数据
    const sessionData = {
      // 每个对话对象都需要有一个唯一的标识
      sessionId: res.sessionId,
      // 会话状态
      // TEMP：临时会话，用户可以随时结束
      // ACTIVE：活跃会话，用户可以继续聊天
      status: res.status,
      // 会话标题
      sessionTitle: sessionParams.sessionTitle,
    }
    // 如果是临时会话
    if (currentSession.value.status === 'TEMP' && currentSession.value) {
      // 将数据更新为正式会话
      Object.assign(currentSession.value, sessionData)
    } else {
      currentSession.value = sessionData
    }
    //    新建会话后，要更新历史会话列表
    getSessionPage()
    // 显示用户输入的内容
    messages.value.push({
      id: Date.now(),
      senderType: 1,
      content: message,
      createAt: new Date().toISOString()
    })
    //    开始流式对话
    startAIResponse(currentSession.value.sessionId, message)
  })

}
// 实现流式对话
const startAIResponse = (sessionId, userMessage) => {
  // 防止重复发送
  if (isAiTyping.value) {
    ElMessage.error('AI助手正在回复，请稍后再试')
    return
  }
  isAiTyping.value = true
  const aiMessage = {
    id: `ai_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    senderType: 2,
    content: '',
    createAt: new Date().toISOString()
  }
  messages.value.push(aiMessage)
  //    调用流式接口
  const ctrl = new AbortController() //用于终止fetchEventSource请求
  fetchEventSource('/api/psychological-chat/stream', {
    // 流式接口需要使用POST方法
    method: 'POST',
    // 接口请求需要的header参数
    headers: {
      'Content-Type': 'application/json',
      'Token': localStorage.getItem('token'),
      'Accept': 'text/event-stream'
    },
    // 接口请求需要的body参数
    body: JSON.stringify({
      sessionId,
      userMessage
    }),
    // 接口请求需要的signal参数
    signal: ctrl.signal, // 用于终止fetchEventSource请求
    onopen: (response) => {
      if (response.headers.get('Content-Type') !== 'text/event-stream') {
        ElMessage.error('流式接口返回的不是text/event-stream类型')
      }
    },
    // 处理流式接口返回的消息
    onmessage: (event) => {
      const raw = event.data.trim()
      if (!raw) return
      const eventName = event.event
      // 当前会话的AI消息
      const aiMessage = messages.value[messages.value.length - 1]

      if (eventName === 'done') {
        isAiTyping.value = false
        ctrl.abort()
        // 调用接口获取会话情绪分析结果
        loadSessionEmotion(currentSession.value.sessionId)
        return
      }
      const payload = JSON.parse(raw)
      const ok = String(payload.code) === '200'
      if (ok && payload.data && payload.data.content) {
        aiMessage.content += payload.data.content
      } else if (!ok) {
        // 错误回复的显示
        handleError(payload.message || 'AI回复失败')
      }
    },
    onerror: (err) => {
      handleError(err || 'AI回复失败')
      throw err
    },
    //    流式接口关闭事件
    onclose: () => {
      loadSessionEmotion(currentSession.value.sessionId)
    }
  })

}
// 错误回复的显示
const handleError = (error) => {
  // 当前会话的AI消息
  const aiMessage = messages.value[messages.value.length - 1]
  if (aiMessage) {
    aiMessage.content = 'AI回复失败,请重试'
  }
  isAiTyping.value = false
  ElMessage.error('AI回复失败,请重试')
}
// 历史会话列表
const getSessionPage = () => {
  // 调用接口查询会话列表
  getSessionList({
    pageNum: 1,
    pageSize: 10
  }).then(res => {
    // 接收后端返回来的数据
    sessionList.value = res.records
  })

}
// 处理点击会话事件
const handleSessionClick = (session) => {
  // 调用接口获取会话消息列表
  getSessionDetail(session.id).then(res => {
    // 接收后端返回来的数据
    messages.value = res
  })
  // 调用接口获取会话情绪分析结果
  loadSessionEmotion(session.id)
  // 更新历史会话内容
  const sessionData = {
    sessionId: "session_" + session.id,
    status: 'ACTIVE',
    // 会话标题
    sessionTitle: session.sessionTitle,
  }
  currentSession.value = sessionData
}
// 处理删除会话事件
const handleDeleteSession = (sessionId) => {
  // 调用接口删除会话
  deleteSession(sessionId).then(res => {
    ElMessage.success('删除成功')
    // 如果删除的是当前正在查看的会话，重置聊天界面
    if (currentSession.value?.sessionId === 'session_' + sessionId) {
      createNewFrontendSession()
    }
    // 删除成功后，要更新历史会话列表
    getSessionPage()
  })
}
// 处理用户内容的格式
const formatMessageContent = (content) => {
  // 替换换行符为<br>
  return content.replace(/\n/g, '<br>')
}
// dot数量函数
// 根据分数来决定dot数量
const getIntensityClass = (score) => {
  if (score >= 61) {
    return 3
  }
  if (score >= 31) {
    return 2
  }
  return 1
}
// 风险等级
const getRiskText = (level) => {
  switch (level) {
    case 0:
      return '正常'
    case 1:
      return '关注'
    case 2:
      return '预警'
    case 3:
      return '危机'
    default:
      return '未知风险'
  }
}
onMounted(() => {
  // 每次进入ai咨询页面，都要新建一个会话，就像你打开豆包一样，页面都会进入到一个新的会话
  createNewFrontendSession()
  // 调用接口查询会话列表,因为每次加载页面都要查询一次历史会话列表
  getSessionPage()

})
</script>

<style lang="scss" scoped>
.consultation-container {
  margin: 0 auto;
  width: 1200px;
  display: flex;
  gap: 20px;
  padding: 20px;

  .sidebar {
    width: 320px;

    .ai-assistant-info {
      margin-bottom: 20px;
      background: linear-gradient(135deg, rgba(72, 212, 67, 0.9) 0%, rgba(87, 232, 29, 0.95) 100%);
      border-radius: 16px;
      padding: 16px;
      box-shadow: 0 8px 32px rgba(251, 146, 60, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
      border: 1px solid rgba(251, 146, 60, 0.08);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;

      .breathing-circle {
        width: 60px;
        height: 60px;
        background: rgba(34,163,107,1);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 12px;
        animation: breathing 4s ease-in-out infinite;
        box-shadow: 0 6px 24px rgba(251, 146, 60, 0.25);
        position: relative;
      }

      .assistant-name {
        font-size: 16px;
        font-weight: 700;
        background: linear-gradient(135deg, #fb923c, #f59e0b);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        text-align: center;
        background-clip: text;
        margin: 0 0 12px;
      }

      .online-status {
        display: flex;
        align-items: center;
        justify-content: center;
        color: #059669;
        font-size: 12px;
        font-weight: 600;

        .status-dot {
          width: 8px;
          height: 8px;
          background: #059669;
          border-radius: 50%;
          margin-right: 8px;
          animation: pulse 2s infinite;
          box-shadow: 0 0 8px rgba(5, 150, 105, 0.4);
        }
      }
    }

    .session-history {
      background: white;
      border-radius: 16px;
      padding: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      margin-bottom: 20px;
      min-height: 250px;
      display: flex;
      flex-direction: column;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0 0 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;

      }

      .session-list {
        overflow-y: auto;
        max-height: 200px;
        scrollbar-width: thin;
        scrollbar-color: rgba(64, 150, 255, 0.3) transparent;

        .session-item {
          position: relative;
          display: flex;
          align-items: flex-start;
          gap: 12px;
          padding: 12px;
          margin-bottom: 8px;
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.3s ease;
          border: 2px solid transparent;

          &:hover {
            background: #f8f9ff;
            border-color: #e6f0ff;
          }

          &.active {
            background: #e6f0ff;
            border-color: #4096ff;
          }

          .session-info {
            flex: 1;

            .session-title {
              font-weight: 500;
              font-size: 14px;
              color: #333;
              margin-bottom: 4px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

              .session-meta {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 6px;

                .session-time {
                  font-size: 12px;
                  color: #999;
                }
              }

              .session-preview {
                width: 200px;
                font-size: 12px;
                color: #666;
                margin-bottom: 6px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              }

              .session-stats {
                display: flex;
                align-items: center;
                gap: 12px;

                span {
                  font-size: 12px;
                  color: #999;
                  display: flex;
                  align-items: center;
                  gap: 4px;
                }
              }
            }

            .session-actions {
              position: absolute;
              top: 10px;
              right: 12px;
            }
          }
        }

        .no-sessions-text {
          text-align: center;
          font-size: 14px;
          color: #999;
        }
      }
    }

    .emotion-garden {
      background: linear-gradient(135deg, #fef9e7 0%, #fcf4e6 50%, #f6f0e8 100%);
      border-radius: 20px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 8px 32px rgba(252, 244, 230, 0.8);
      border: 1px solid rgba(255, 255, 255, 0.2);
      position: relative;
      overflow: hidden;
      min-height: 300px;

      .garden-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
        position: relative;
        z-index: 2;

        .garden-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 16px;
          font-weight: 600;
          color: #8b4513;
        }
      }

      .emotion-info {
        margin: 0 auto;
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        z-index: 10;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
        border: 2px solid rgba(255, 255, 255, 0.8);
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
        color: #fff;

        .emotion-name {
          font-size: 15px;
          font-weight: 600;
          line-height: 1;
          margin-bottom: 2px;
        }

        .emotion-score {
          font-size: 14px;
          font-weight: 700;
          opacity: 0.9;
        }
      }

      .warm-tips {
        text-align: center;
        margin-bottom: 16px;

        .emotion-status-text {
          margin-bottom: 12px;

          .status-label {
            font-size: 14px;
            color: #8b7355;
            margin-right: 8px;
          }

          .status-emotion {
            font-size: 16px;
            font-weight: 600;
            padding: 4px 12px;
            border-radius: 16px;
            display: inline-block;
          }
        }

        .emotion-intensity {
          margin-bottom: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;

          .intensity-dots {
            display: flex;
            gap: 4px;

            .dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background: #e0e0e0;
              transition: all 0.3s ease;

              &.active {
                background: linear-gradient(135deg, #ff9a9e, #fecfef);
                transform: scale(1.2);
                box-shadow: 0 2px 8px rgba(255, 154, 158, 0.4);
              }
            }
          }

          .intensity-text {
            font-size: 12px;
            color: #8b7355;
            font-weight: 500;
          }
        }

        .warm-suggestion {
          background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.8));
          border-radius: 16px;
          padding: 12px;
          margin-bottom: 16px;
          display: flex;
          align-items: flex-start;
          gap: 10px;
          border: 1px solid rgba(255, 255, 255, 0.6);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);

          .suggestion-icon {
            font-size: 20px;
            flex-shrink: 0;
            margin-top: 2px;
          }

          .suggestion-content {
            text-align: left;
            flex: 1;

            .suggestion-title {
              font-size: 14px;
              font-weight: 600;
              color: #8b7355;
              margin-bottom: 6px;
            }

            .suggestion-text {
              font-size: 13px;
              color: #6b5b47;
              line-height: 1.5;
            }
          }
        }

        .healing-actions {
          margin-bottom: 16px;

          .actions-title {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 600;
            color: #8b7355;
            margin-bottom: 16px;
          }

          .actions-list {
            display: flex;
            flex-direction: column;
            gap: 10px;

            .action-item {
              background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
              border-radius: 12px;
              padding: 12px;
              display: flex;
              align-items: center;
              gap: 10px;
              border: 1px solid rgba(255, 255, 255, 0.5);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
              text-align: left;

              .action-icon {
                font-size: 14px;
                color: #ffd700;
                flex-shrink: 0;
              }

              .action-text {
                font-size: 12px;
                color: #6b5b47;
                line-height: 1.4;
                flex: 1;
              }
            }
          }
        }

        .risk-notice {
          background: linear-gradient(135deg, #fff9e6, #ffeaa7);
          border-radius: 16px;
          padding: 16px;
          display: flex;
          align-items: flex-start;
          gap: 12px;
          border: 1px solid rgba(255, 234, 167, 0.6);
          box-shadow: 0 6px 20px rgba(255, 234, 167, 0.3);

          .notice-icon {
            font-size: 20px;
            flex-shrink: 0;
            margin-top: 2px;
          }

          .notice-content {
            flex: 1;

            .notice-title {
              font-size: 14px;
              font-weight: 600;
              color: #d4840f;
              margin-bottom: 6px;
            }

            .notice-text {
              font-size: 13px;
              color: #b8740c;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }

  .chat-main {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 252, 250, 0.98) 100%);
    border-radius: 20px;
    box-shadow: 0 12px 40px rgba(251, 146, 60, 0.08), 0 4px 16px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(251, 146, 60, 0.1);
    backdrop-filter: blur(10px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    flex: 1;

    .chat-header {
      background: linear-gradient(135deg, #fb923c 0%, #f59e0b 100%);
      color: white;
      padding: 20px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: relative;
      flex-shrink: 0;

      .header-left {
        display: flex;
        align-items: center;

        .chat-avatar {
          width: 48px;
          height: 48px;
          background: rgba(255, 255, 255, 0.25);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          position: relative;
          z-index: 1;
        }

        .chat-info {
          h2 {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 4px;
          }

          p {
            font-size: 14px;
          }
        }
      }
    }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.02) 0%, rgba(255, 252, 248, 0.05) 100%);
      min-height: 0;
      max-height: calc(100vh - 200px);
      scrollbar-width: thin;
      scrollbar-color: rgba(251, 146, 60, 0.3) transparent;

      .message-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;

        .message-avatar {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          color: white;
          flex-shrink: 0;
        }

        &.ai-message {
          .message-avatar {
            background: rgba(34,163,107,1);
            box-shadow: 0 4px 12px rgba(251, 146, 60, 0.3);
          }
        }

        &.user-message {
          .message-avatar {
            background: linear-gradient(135deg, #6b7280, #4b5563);
            box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
          }
        }

        .message-content {
          max-width: 70%;

          .message-bubble {
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 252, 248, 0.95) 100%);
            border-radius: 16px;
            padding: 12px 16px;
            position: relative;
            animation: fadeInUp 0.4s ease-out;
            border: 1px solid rgba(251, 146, 60, 0.1);
            box-shadow: 0 4px 16px rgba(251, 146, 60, 0.05);

            .typing-indicator {
              display: flex;
              gap: 4px;
              padding: 8px 0;

              .typing-dot {
                width: 8px;
                height: 8px;
                background: #ccc;
                border-radius: 50%;
                animation: typing 1.5s ease-in-out infinite;

                &:nth-child(2) {
                  animation-delay: 0.2s;
                }

                &:nth-child(3) {
                  animation-delay: 0.4s;
                }
              }
            }

            /* 错误消息样式 */
            .error-message {
              background: linear-gradient(135deg, #FEF2F2 0%, #FECACA 100%);
              border: 1px solid #F87171;
              border-radius: 12px;
              padding: 12px 16px;
              color: #991B1B;
              font-weight: 500;
              display: flex;
              align-items: center;
              gap: 8px;
            }
          }

          .message-time {
            font-size: 12px;
            color: #999;
            margin-top: 4px;
          }
        }
      }
    }

    .chat-input {
      border-top: 1px solid rgba(251, 146, 60, 0.1);
      padding: 20px 24px;
      display: flex;
      gap: 12px;
      align-items: flex-end;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 252, 248, 0.7) 100%);
      backdrop-filter: blur(10px);
      flex-shrink: 0;

      .input-container {
        flex: 1;
      }

      .input-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #78716c;
        font-weight: 500;
      }

      .send-btn {
        height: 60px;
        width: 60px;
        border-radius: 16px;
        background: linear-gradient(135deg, #fb923c 0%, #f59e0b 100%) !important;
        border: none !important;
        box-shadow: 0 6px 20px rgba(251, 146, 60, 0.25);
        transition: all 0.3s ease;
      }

    }

  }
}
</style>