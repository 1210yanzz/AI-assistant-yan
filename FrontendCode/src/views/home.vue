<template>
  <div class="home-container">
    <div class="content">
      <div class="text">
        <h2 class="title">
          欢迎来到心理健康AI助手<br/>
          <span class="highlight-text">时刻关心您的心理健康</span>
        </h2>
        <p class="description">为您的心灵打开一扇窗户，为我们的沟通搭起一座桥</p>
        <div class="hero-actions">
          <el-button size="large">开始倾诉，获得陪伴</el-button>
          <el-button size="large" style="border-color: #fff;" color="transparent">记录心情，释放感情</el-button>
        </div>
      </div>
      <div class="robot">
        <el-image :style="{ width: '220px', height: '220px', marginTop: '30px' }" :src="iconUrl" alt="机器人"></el-image>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const iconUrl = new URL('@/assets/image/ai-image-heart-sticker.png', import.meta.url).href
</script>

<style lang="scss" scoped>
.home-container {
  background: linear-gradient(90deg, rgb(8, 206, 37) 0%, rgb(17, 127, 125) 100%) rgba(175, 190, 187, 0.95);
  color: white;
  padding: 5rem 0;
  height: calc(100vh - 285px);
  display: flex;
  align-items: center;
  justify-content: center;

  .content {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 30px;

    .text {
      width: 500px;

      .title {
        font-size: 45px;
        font-weight: bold;
        // line-height: 1.2;
        margin-bottom: 15px;

        .highlight-text {
          color: #ffd700;
        }
      }

      .hero-actions {
        margin-top: 30px;
      }
    }

    .robot {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 260px;
      height: 260px;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.2);
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }
  }
}
</style>