<template>
  <div class="login-page">
    <div class="bg-grid"></div>
    <div class="bg-particles">
      <div v-for="i in 50" :key="'p'+i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <div class="login-wrapper">
      <div class="login-box">
        <div class="border-line top"></div>
        <div class="border-line right"></div>
        <div class="border-line bottom"></div>
        <div class="border-line left"></div>

        <div class="login-header">
          <div class="logo-wrap">
            <img src="/logo.png" alt="Logo" />
          </div>
          <h1>智慧物流仓储</h1>
          <span>INTELLIGENT LOGISTICS WAREHOUSING SYSTEM</span>
          <div class="header-divider"></div>
        </div>

        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%">
              {{ loading ? '验证中...' : '系统登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="footer-lines">
            <span></span><span></span><span></span>
          </div>
          <p>CYBER WMS v2.0 // SECURE CONNECTION</p>
        </div>
      </div>
    </div>

    <div class="corner tl"></div><div class="corner tr"></div><div class="corner bl"></div><div class="corner br"></div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getParticleStyle = (index) => {
  const r = () => Math.random()
  return {
    left: r() * 100 + '%',
    top: r() * 100 + '%',
    animationDelay: r() * 5 + 's',
    animationDuration: (3 + r() * 4) + 's',
    width: (2 + r() * 4) + 'px',
    height: (2 + r() * 4) + 'px'
  }
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
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 50%, #0a0e27 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
  box-sizing: border-box;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,240,255,.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,240,255,.03) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(500px) rotateX(60deg);
  opacity: .5;
  pointer-events: none;
}

.bg-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.particle {
  position: absolute;
  background: var(--cyber-primary);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--cyber-primary);
  opacity: .5;
  animation: pf infinite ease-in-out;
}
@keyframes pf {
  0%,100%{transform:translate(0,0);opacity:.5}25%{transform:translate(15px,-25px);opacity:.7}
  50%{transform:translate(-8px,-45px);opacity:.3}75%{transform:translate(-22px,-15px);opacity:.6}
}

/* 关键：wrapper 确保不拉伸 */
.login-wrapper {
  display: block !important;
  width: auto !important;
  height: auto !important;
  max-width: none !important;
  min-width: 0 !important;
  flex: none !important;
}

/* 登录框：固定宽度 */
.login-box {
  width: 440px !important;
  max-width: 90vw !important;
  min-width: 300px !important;
  padding: 48px 36px !important;
  background: rgba(10,20,40,.92) !important;
  border: 2px solid var(--cyber-border) !important;
  border-radius: 12px !important;
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 10;
  box-shadow:
    0 0 40px rgba(0,240,255,.15),
    0 0 80px rgba(0,240,255,.08),
    inset 0 0 30px rgba(0,240,255,.04);
}

.border-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent);
}
.border-line.top { top:-1px; left:20px; right:20px; height:2px; animation: bf 3s infinite; }
.border-line.right { right:-1px; top:20px; bottom:20px; width:2px; background:linear-gradient(180deg,transparent,var(--cyber-primary),transparent); }
.border-line.bottom { bottom:-1px; left:20px; right:20px; height:2px; background:linear-gradient(90deg,transparent,var(--cyber-secondary),transparent); }
.border-line.left { left:-1px; top:20px; bottom:20px; width:2px; background:linear-gradient(180deg,transparent,var(--cyber-secondary),transparent); }

@keyframes bf { 0%,100%{opacity:.4}50%{opacity:1} }

.login-header { text-align:center; margin-bottom:32px; }

.logo-wrap {
  display:inline-block; margin-bottom:14px; position:relative;
}
.logo-wrap img {
  width:64px; height:64px; border-radius:10px;
  border:2px solid var(--cyber-primary);
  box-shadow:0 0 20px rgba(0,240,255,.4);
  animation: lp 3s infinite;
}
@keyframes lp {
  0%,100%{box-shadow:0 0 20px rgba(0,240,255,.4)}50%{box-shadow:0 0 35px rgba(0,240,255,.6),0 0 60px rgba(0,240,255,.3)}
}

.login-header h1 {
  font-family:'Orbitron',sans-serif; font-size:26px; font-weight:800;
  letter-spacing:3px; color:#fff; text-transform:uppercase; margin:12px 0 6px;
  background:linear-gradient(135deg,#00f0ff,#fff,#00f0ff);
  -webkit-background-clip:text; -webkit-text-fill-color:transparent;
  background-size:200% auto; animation:ts 4s linear infinite;
}
@keyframes ts{to{background-position:200% center}}

.login-header span {
  font-family:'Rajdhani',sans-serif; font-size:10px; letter-spacing:3px;
  color:#8ba4b8; text-transform:uppercase; display:block; margin-bottom:12px;
}

.header-divider {
  width:70%; height:1px; margin:0 auto;
  background:linear-gradient(90deg,transparent,var(--cyber-primary),transparent);
  box-shadow:0 0 10px rgba(0,240,255,.4);
}

.login-form { margin-top:24px; }

.login-form :deep(.el-input__wrapper) {
  background:rgba(0,20,40,.65)!important; border:1px solid var(--cyber-border)!important;
  box-shadow:none!important; border-radius:6px!important; padding:4px 11px!important;
}
.login-form :deep(.el-input__wrapper:hover){border-color:rgba(0,240,255,.45)!important}
.login-form :deep(.el-input__wrapper.is-focus){
  border-color:var(--cyber-primary)!important; box-shadow:0 0 15px rgba(0,240,255,.25)!important
}
.login-form :deep(.el-input__inner){color:#e0f7ff!important;font-size:14px!important}
.login-form :deep(.el-input__inner::placeholder){color:#8ba4b8!important}
.login-form :deep(.el-input__prefix .el-icon){color:#00f0ff!important;font-size:17px!important}

.login-footer { margin-top:28px;text-align:center; }
.footer-lines { display:flex;justify-content:center;gap:8px;margin-bottom:14px; }
.footer-lines span{
  width:35px;height:2px;background:var(--cyber-border);position:relative;overflow:hidden;display:block;
}
.footer-lines span::after{
  content:'';position:absolute;top:0;left:-100%;width:100%;height:100%;
  background:#00f0ff;animation:lf 2.5s linear infinite;
}
.footer-lines span:nth-child(2){animation-delay:.3s}.footer-lines span:nth-child(3){animation-delay:.6s}
@keyframes lf{to{left:100%}}

.login-footer p {
  font-family:'Rajdhani',sans-serif;font-size:9px;letter-spacing:2px;
  color:#8ba4b8;opacity:.55;text-transform:uppercase;margin:0;
}

.corner{position:absolute;width:70px;height:70px;z-index:5;pointer-events:none}
.corner::before,.corner::after{content:'';position:absolute;background:#00f0ff;box-shadow:0 0 8px #00f0ff}
.corner.tl{top:24px;left:24px}.corner.tr{top:24px;right:24px}
.corner.bl{bottom:24px;left:24px}.corner.br{bottom:24px;right:24px}
.corner.tl::before{top:0;left:0;width:35px;height:2px}.corner.tl::after{top:0;left:0;width:2px;height:35px}
.corner.tr::before{top:0;right:0;width:35px;height:2px}.corner.tr::after{top:0;right:0;width:2px;height:35px}
.corner.bl::before{bottom:0;left:0;width:35px;height:2px}.corner.bl::after{bottom:0;left:0;width:2px;height:35px}
.corner.br::before{bottom:0;right:0;width:35px;height:2px}.corner.br::after{bottom:0;right:0;width:2px;height:35px}
</style>
