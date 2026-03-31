<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">智慧物流仓储</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', loginForm)
    let userInfo = res.data.userInfo
    if (userInfo.username === 'admin' || userInfo.id === 1) {
      userInfo.menuIds = ['HOME', 'CUSTOMER', 'SUPPLIER', 'CONTAINER', 'GOODS', 'ZONE', 'LOCATION', 'CUSTOMER_ORDER', 'ORDER_PROCESS', 'PICKING_PLAN', 'INBOUND_PLAN', 'INBOUND_TASK', 'PICKING_TASK', 'PACKING', 'REPLENISH_MANAGE', 'TRANSFER_MANAGE', 'STOCKTAKE_MANAGE', 'DATA_QUERY', 'USER', 'ROLE', 'MENU', 'OPER_LOG']
    } else {
      userInfo.menuIds = ['HOME', 'CUSTOMER', 'SUPPLIER', 'CONTAINER', 'GOODS', 'ZONE', 'LOCATION', 'DATA_QUERY']
    }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
}

.login-form {
  margin-top: 20px;
}
</style>
