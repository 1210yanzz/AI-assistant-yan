<template>
  <div class="container">
    <div class="title">
      <div class="back-home" @click="goHome">
        <el-icon><Back /></el-icon>
        <span>返回首页</span>
      </div>
      <div class="title-text">
        <h2>登录您的账户</h2>
        <p>请输入您的登录信息</p>
      </div>
    </div>
    <div class="form-container">
      <el-form ref="loginForm" :model="formData" :rules="loginRules" label-position="top">
        <el-form-item label="用户名或邮箱" prop="username">
          <el-input v-model="formData.username" size="large" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" size="large" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
        <el-button class="btn" size="large" type="primary" @click="submitForm(loginForm)">登录</el-button>
      </el-form>
      <div class="footer">
        <p>还没有账户？<router-link to="/auth/register">去注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login } from '@/api/admin'
import { useRouter } from 'vue-router'

const loginForm = ref()

const formData = reactive({
  username: '',
  password: ''
})
const loginRules = reactive({
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}
  ]
})
// 提交表单,登录
const router = useRouter()

// 未登录状态下返回前台首页
const goHome = () => {
  router.push('/')
}
const submitForm = async(formEl) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      login(formData).then(data => {
        // 判断token是否存在
        if (!data.token) {  // 如果不存在
          return console.log('登录失败')
        }
        // 存在保存token和用户信息
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
        // 根据用户角色跳转不同路径页面
        if (data.userInfo.userType === 2) {   // 管理员
          router.push('/back/dashboard')
        } else {  // 普通用户
          router.push('/')
        }
      })
    }
  })
}
</script>

<style scoped lang="scss">
  .container {
    width: 364px;
    .title {
      .back-home {
        margin-bottom: 60px;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
        &:hover {
          color: #4A90E2;
        }
      }
      .title-text {
        text-align: center;
        h2 {
          font-size: 24px;
          margin-bottom: 10px;
        }
        p {
          font-size: 14px;
          color: #9b9b9b;
        }
      }
    }
    .form-container { 
      margin-top: 10px;
      .btn {
        width: 100%;
        margin-top: 20px;
      }
      .footer {
        text-align: center;
        margin-top: 10px;
        font-size: 12px;
        p {
          font-size: 15px;
          color: #9b9b9b;
        }
      }
    }
  }
</style>