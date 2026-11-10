<template>
  <div class="auth-layout">
    <div class="left-section"> 
      <div class="content">
        <h2 class="title">心理健康AI助手</h2>
        <p class="text">身体健康在于运动，心理健康在于倾诉</p>
        <div class="robot">
          <el-image style="width: 100px; height: 100px;" :src="inconUrl" alt="心理助手"></el-image>
        </div>
      </div>
    </div>
    <div class="right-section">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
const inconUrl = new URL('../assets/image/Mentalhealth.png', import.meta.url).href
</script>

<style scoped lang="scss">
.auth-layout {
  display: flex;
  .left-section { 
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    background: #23BE6D;
    height: 100vh;
    .content { 
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      .title { 
        margin-bottom: 20px;
        font-size: 40px;
        font-weight: bold;
        color: #fff;
      }
      .text { 
        margin-bottom: 20px;
        width: 460px;
        font-size: 20px;
        color: #fff;
        text-align: center;
      }
      .robot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 500px;
        height: 100px;
        border-radius: 50%;
        border: 2px solid #CAF8FF;
        background: #CAF8FF;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }
    }
  }
  .right-section { 
    display: flex;
    justify-content: center;
    align-items: center;
    flex: 1;
    background: #fff;
    height: 100vh;
  }
}
</style>