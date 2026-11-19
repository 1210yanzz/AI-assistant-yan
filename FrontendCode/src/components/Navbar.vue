<template>
  <div class="navbar">
    <div class="flex-box">
      <el-button @click="handleCollapse">
        <el-icon><Expand /></el-icon>
      </el-button>
      <p class="page-title">{{ route.meta.title }}</p>
    </div>
    <div class="flex-box">
      <el-dropdown @command="handleCommand">
        <div class="flex-box">
          <el-avatar src="https://java-yzz-web.oss-cn-beijing.aliyuncs.com/1045d5f2-7c07-4b11-bb1d-b847f4c6d8db.png"/>
          <p class="user-name">admin</p>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAdminStore } from '@/stores/admin';
import { useRouter, useRoute } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { logout } from '@/api/admin';

const router = useRouter();
const route = useRoute();

const handleCommand = (command) => {
  console.log(command)
  if(command === 'logout') {
    // 处理退出登录
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      // 退出登录
      logout().then(res => {
        // 退出登录成功，清理登录信息token和用户信息
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 跳转到登录页面
        router.push('/auth/login')
      })
    })
  }
}

const handleCollapse = () => {
  useAdminStore().toggleCollapse();
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background-color: #FFFFFF;
  box-shadow: 0px 2px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #E0E0E0;
  .flex-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  }
  .page-title {
    font-size: 18px;
    font-weight: bold;
    margin-left: 20px;
    color: #1f2937;
  }
}
</style>