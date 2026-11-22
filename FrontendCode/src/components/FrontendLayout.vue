<template>
  <div class="frontend-layout">
    <div class="navbar-container">
      <div class="brand-section"> 
        <el-image style="width: 50px; height: 50px;" :src="iconUrl" alt="logo" class="brand-logo"></el-image>
        <h1 class="brand-name">心理健康AI助手</h1>
      </div>
      <div class="nav-section"> 
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/consultation" class="nav-link" v-if="isLogin">AI咨询</router-link>
        <router-link to="/emotional-diary" class="nav-link" v-if="isLogin">情感日志</router-link>
        <router-link to="/knowledge" class="nav-link">知识库</router-link>
        <el-button class="logout-btn" v-if="isLogin" @click="handleLogout">退出登录</el-button>
        <template v-else>
          <router-link to="/auth/login" class="nav-link">登录</router-link>
          <router-link to="/auth/register" class="nav-link">
            <el-button type="primary">注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
    <div class="main-content">
      <router-view></router-view>
    </div>
    <div class="footer-container">
      <div class="footer-bottom">
        <p>&copy; 2026 心理健康AI助手.yanzhongzgeng-ai-item</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { logout } from '@/api/admin'
import { useRouter } from 'vue-router'

const router = useRouter()

const iconUrl = new URL('@/assets/image/ai-image.png', import.meta.url).href

// 退出登录
const handleLogout = () => {
  // JWT 无状态，退出登录以清理本地登录态为准；即使接口失败（如 token 已失效）也要退出
  logout().catch(() => {}).finally(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/auth/login')
  })
}

const isLogin = ref(false)
onMounted(() => { 
  isLogin.value = localStorage.getItem('token') !== null
})

</script>

<style lang="scss" scoped>
.frontend-layout {
  background-color: #fff;

  .navbar-container {
    max-width: 1200px;
    height: 100%;
    margin: 0 auto;
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .brand-section {
      display: flex;
      align-items: center;

      .brand-name {
        margin-left: 10px;
        font-size: 24px;
        font-weight: 600;
        color: #333;
      }
    }

    .nav-section {
      display: flex;
      align-items: center;
      gap: 40px;

      .nav-link {
        color: #4b5563;
        font-size: 16px;
        font-weight: 500;
        text-decoration: none;    // ← 去掉下划线
        &:hover {
          color: #4A90E2;
        }
      }
    }
  }

  .footer-container {
    background: #1f2937;
    color: white;
    padding: 15px 0;
    margin-top: auto;

    .footer-bottom {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 10px;
      text-align: center;
    }
  }
}
</style>