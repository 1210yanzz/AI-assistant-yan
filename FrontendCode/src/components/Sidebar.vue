<template>
  <el-aside :width="isCollapse ? '64px' : '254px'" class="sidebar">
    <el-menu
        :collapse="isCollapse"
        :collapse-transition="false"
        default-active="2"
        class="el-menu-vertical-demo"
      >
        <div v-show="!isCollapse" class="brand">
          <el-image style="width: 50px; height: 50px;" :src="brandLogo" alt="logo"></el-image>
          <div class="info-card">
            <h1 class="brand-title">心理健康AI助手</h1>
            <p class="brand-subtitle">后台管理</p>
          </div>
        </div>
        <el-menu-item @click="selectMenu" v-for="item in router.options.routes[0].children" :key="item.path" :index="item.path">
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <template #title>
            <span>{{ item.meta.title }}</span>
          </template>
        </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
const router = useRouter()
const adminStore = useAdminStore()

const brandLogo = new URL('../assets/image/ai-image.png', import.meta.url).href

const isCollapse = computed(() => adminStore.isCollapse)

const selectMenu = (key) => {
  console.log(key)
  const currentRoute = router.options.routes[0]
  router.push(`${currentRoute.path}/${key.index}`)
}

</script>

<style lang="scss" scoped>
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #FFFFFF;
  .info-card {
    .brand-title {
      font-size: 18px;
      font-weight: bold;
      margin: 0px;
      color: #333333;
      margin-right: 40px;
      letter-spacing: 2px;
    }
    .brand-subtitle {
      font-size: 12px;
      color: #666666;
      margin-right: 40px;
      letter-spacing: 1px;
    }
  }
}
</style>

<style scoped>
.sidebar {
  height: 100%;
  overflow: hidden;
  transition: width 0.3s;
}

.el-aside {
  height: 100%;
}

.el-menu {
  height: 100%;
  border-right: none;
}
</style>