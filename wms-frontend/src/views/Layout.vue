<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-bg-effect"></div>
      <div class="header-left">
        <div class="logo-container">
          <img src="/logo.png" alt="Logo" class="logo-img" />
          <div class="logo-pulse"></div>
        </div>
        <span class="system-title">智慧物流仓储</span>
        <span class="system-subtitle">CYBER WMS SYSTEM</span>
      </div>
      <div class="header-center">
        <div class="header-line left"></div>
        <div class="header-status">
          <span class="status-dot"></span>
          <span>SYSTEM ONLINE</span>
        </div>
        <div class="header-line right"></div>
      </div>
      <div class="header-right">
        <div class="datetime-display">
          <div class="time">{{ currentTime }}</div>
          <div class="date">{{ currentDate }}</div>
        </div>
        <el-dropdown>
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userInfo.realName || userInfo.username }}
            <span class="user-badge">ADMIN</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside width="240px" class="layout-aside">
        <el-menu :default-active="activeMenu" router class="aside-menu">
          <el-menu-item index="/home" v-if="hasMenuAccess('HOME')">
            <el-icon><House /></el-icon>
            <span>首页概览</span>
          </el-menu-item>

          <el-sub-menu index="base" v-if="hasMenuAccess('CUSTOMER') || hasMenuAccess('SUPPLIER') || hasMenuAccess('CONTAINER') || hasMenuAccess('GOODS') || hasMenuAccess('ZONE') || hasMenuAccess('LOCATION')">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/base/customer" v-if="hasMenuAccess('CUSTOMER')">客户设置</el-menu-item>
            <el-menu-item index="/base/supplier" v-if="hasMenuAccess('SUPPLIER')">供应商设置</el-menu-item>
            <el-menu-item index="/base/container" v-if="hasMenuAccess('CONTAINER')">容器设置</el-menu-item>
            <el-menu-item index="/base/goods" v-if="hasMenuAccess('GOODS')">商品设置</el-menu-item>
            <el-menu-item index="/base/zone" v-if="hasMenuAccess('ZONE')">库区设置</el-menu-item>
            <el-menu-item index="/base/location" v-if="hasMenuAccess('LOCATION')">仓位设置</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="order" v-if="hasMenuAccess('CUSTOMER_ORDER') || hasMenuAccess('ORDER_PROCESS') || hasMenuAccess('PICKING_PLAN')">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/order/customer-order" v-if="hasMenuAccess('CUSTOMER_ORDER')">客户订单</el-menu-item>
            <el-menu-item index="/order/order-process" v-if="hasMenuAccess('ORDER_PROCESS')">订单处理</el-menu-item>
            <el-menu-item index="/order/picking-plan" v-if="hasMenuAccess('PICKING_PLAN')">拣选计划</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="inbound" v-if="hasMenuAccess('INBOUND_PLAN') || hasMenuAccess('INBOUND_TASK')">
            <template #title>
              <el-icon><UploadFilled /></el-icon>
              <span>入库管理</span>
            </template>
            <el-menu-item index="/inbound/inbound-plan" v-if="hasMenuAccess('INBOUND_PLAN')">入库计划</el-menu-item>
            <el-menu-item index="/inbound/inbound-task" v-if="hasMenuAccess('INBOUND_TASK')">入库作业</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="outbound" v-if="hasMenuAccess('PICKING_TASK') || hasMenuAccess('PACKING')">
            <template #title>
              <el-icon><Download /></el-icon>
              <span>出库管理</span>
            </template>
            <el-menu-item index="/outbound/picking-task" v-if="hasMenuAccess('PICKING_TASK')">拣货作业</el-menu-item>
            <el-menu-item index="/outbound/packing-list" v-if="hasMenuAccess('PACKING')">装箱单</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="replenish" v-if="hasMenuAccess('REPLENISH_MANAGE')">
            <template #title>
              <el-icon><RefreshLeft /></el-icon>
              <span>补货管理</span>
            </template>
            <el-menu-item index="/replenish/replenish-manage" v-if="hasMenuAccess('REPLENISH_MANAGE')">补货管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="transfer" v-if="hasMenuAccess('TRANSFER_MANAGE')">
            <template #title>
              <el-icon><Right /></el-icon>
              <span>移库管理</span>
            </template>
            <el-menu-item index="/transfer/transfer-manage" v-if="hasMenuAccess('TRANSFER_MANAGE')">移库管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="check" v-if="hasMenuAccess('STOCKTAKE_MANAGE')">
            <template #title>
              <el-icon><List /></el-icon>
              <span>盘点管理</span>
            </template>
            <el-menu-item index="/stocktake/stocktake-manage" v-if="hasMenuAccess('STOCKTAKE_MANAGE')">盘点管理</el-menu-item>
            <el-menu-item index="/stocktake/stocktake-data">盘点数据</el-menu-item>
            <el-menu-item index="/inventory/realtime">实时盘点数据</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/drone-monitor">
            <el-icon><Monitor /></el-icon>
            <span>智能盘点</span>
          </el-menu-item>

          <div class="menu-item-external" @click="openScreen">
            <el-icon><DataLine /></el-icon>
            <span>中控大屏</span>
            <span class="external-badge">LIVE</span>
          </div>

          <el-menu-item index="/query/data-query" v-if="hasMenuAccess('DATA_QUERY')">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据查询</span>
          </el-menu-item>

          <el-sub-menu index="system" v-if="isAdminUser">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user" v-if="hasMenuAccess('USER')">用户管理</el-menu-item>
            <el-menu-item index="/system/role" v-if="hasMenuAccess('ROLE')">角色管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main class="layout-main">
        <div class="main-content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>

    <CopawChatbot />
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, isAdmin, getAccessibleMenus } from '@/utils/permission'
import CopawChatbot from '@/components/CopawChatbot.vue'

const route = useRoute()
const router = useRouter()

const userInfo = computed(() => {
  return getUserInfo()
})

const isAdminUser = computed(() => {
  return isAdmin()
})

const accessibleMenus = computed(() => {
  return getAccessibleMenus()
})

const hasMenuAccess = (menuCode) => {
  return isAdmin() || accessibleMenus.value.includes(menuCode)
}

const openScreen = () => {
  const token = localStorage.getItem('token')
  window.open(`/screen?t=${token}`, '_blank')
}

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch {
  }
}

const currentTime = ref('')
const currentDate = ref('')
let timeInterval = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short'
  }).replace(/\//g, '-')
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  background: var(--cyber-bg-dark);
}

.layout-container :deep(.el-container) {
  min-height: calc(100vh - 65px);
}

.layout-container :deep(.el-main) {
  height: auto !important;
}

.layout-header {
  background: linear-gradient(135deg, rgba(10, 20, 40, 0.98) 0%, rgba(5, 15, 35, 0.98) 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 25px;
  height: 65px;
  border-bottom: 2px solid var(--cyber-border);
  position: relative;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5), 0 0 30px rgba(0, 240, 255, 0.1);
  z-index: 100;
}

.header-bg-effect {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 240, 255, 0.03) 50%, transparent 100%);
  animation: header-shimmer 5s ease-in-out infinite;
  pointer-events: none;
}
@keyframes header-shimmer {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 2;
  max-height: 65px;
  overflow: hidden;
  flex-shrink: 0;
}

.logo-container {
  position: relative;
  display: flex;
  align-items: center;
  max-height: 50px;
}

.logo-img {
  width: 42px; height: 42px;
  border-radius: 8px;
  border: 2px solid var(--cyber-primary);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.4);
  transition: all 0.3s ease;
}
.logo-img:hover {
  box-shadow: 0 0 25px rgba(0, 240, 255, 0.7);
  transform: scale(1.05);
}

.logo-pulse {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  width: 55px; height: 55px;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.2) 0%, transparent 70%);
  animation: pulse-ring 2s ease-out infinite;
  pointer-events: none;
}
@keyframes pulse-ring {
  0% { opacity: 0.8; transform: translate(-50%, -50%) scale(0.9); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(1.3); }
}

.system-title {
  font-family: 'Orbitron', sans-serif;
  color: var(--cyber-text);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
  background: linear-gradient(135deg, #00f0ff 0%, #ffffff 50%, #00f0ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  white-space: nowrap;
}
@keyframes text-shine {
  to { background-position: 200% center; }
}

.system-subtitle {
  font-family: 'Rajdhani', sans-serif;
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--cyber-text-dim);
  padding-left: 12px;
  border-left: 1px solid var(--cyber-border);
  text-transform: uppercase;
  opacity: 0.7;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 2;
  max-height: 65px;
  overflow: hidden;
  flex-shrink: 0;
}

.header-line {
  width: 120px; height: 1px;
  background: linear-gradient(90deg, transparent, var(--cyber-border), transparent);
  position: relative;
  overflow: hidden;
}
.header-line::after {
  content: '';
  position: absolute;
  top: 0; left: -100%;
  width: 50%; height: 100%;
  background: var(--cyber-primary);
  animation: line-flow 3s linear infinite;
  box-shadow: 0 0 10px var(--cyber-primary);
}
.header-line.right::after {
  animation-delay: 1.5s;
  animation-direction: reverse;
}
@keyframes line-flow {
  to { left: 150%; }
}

.header-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Rajdhani', sans-serif;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--cyber-accent);
  text-transform: uppercase;
}

.status-dot {
  width: 8px; height: 8px;
  background: var(--cyber-accent);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--cyber-accent), 0 0 20px rgba(0, 255, 136, 0.4);
  animation: status-blink 2s ease-in-out infinite;
}
@keyframes status-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 25px;
  position: relative;
  z-index: 2;
  max-height: 65px;
  overflow: hidden;
  flex-shrink: 0;
}

.datetime-display {
  text-align: right;
  font-family: 'Orbitron', monospace;
}
.time {
  font-size: 18px;
  font-weight: 700;
  color: var(--cyber-primary);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  letter-spacing: 2px;
}
.date {
  font-size: 10px;
  color: var(--cyber-text-dim);
  letter-spacing: 1px;
  margin-top: 2px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--cyber-text);
  font-weight: 500;
  padding: 8px 16px;
  background: rgba(0, 240, 255, 0.05);
  border: 1px solid var(--cyber-border);
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 14px;
}
.user-info:hover {
  background: rgba(0, 240, 255, 0.1);
  border-color: var(--cyber-primary);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
}

.user-badge {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 2px 6px;
  background: var(--cyber-primary);
  color: var(--cyber-bg-dark);
  border-radius: 3px;
  text-transform: uppercase;
}

.layout-aside {
  background: linear-gradient(180deg, rgba(8, 15, 35, 0.98) 0%, rgba(12, 22, 45, 0.98) 100%) !important;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: 2px solid var(--cyber-border);
  position: relative;
  box-shadow: 4px 0 30px rgba(0, 0, 0, 0.4), -2px 0 20px rgba(0, 240, 255, 0.05);
}

.layout-aside::after {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 2px; height: 100%;
  background: linear-gradient(180deg, transparent, var(--cyber-primary), transparent);
  opacity: 0.5;
  animation: aside-glow-move 4s ease-in-out infinite;
  pointer-events: none;
}
@keyframes aside-glow-move {
  0%, 100% { opacity: 0.3; transform: translateY(-50%); }
  50% { opacity: 0.7; transform: translateY(50%); }
}

.aside-menu {
  border: none !important;
  background: transparent !important;
  padding: 12px 8px !important;
}

.aside-menu :deep(.el-sub-menu__icon-arrow) {
  color: var(--cyber-text-dim) !important;
}

.aside-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 10, 25, 0.4) !important;
}

.aside-menu :deep(.el-menu-item),
.aside-menu :deep(.el-sub-menu__title) {
  color: var(--cyber-text-dim) !important;
  border-radius: 6px !important;
  border-left: 3px solid transparent !important;
  height: 46px !important;
  line-height: 46px !important;
  font-size: 14px !important;
}

.aside-menu :deep(.el-menu-item:hover),
.aside-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(0, 240, 255, 0.08) !important;
  color: var(--cyber-primary) !important;
  border-left-color: rgba(0, 240, 255, 0.3) !important;
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: rgba(0, 240, 255, 0.12) !important;
  color: var(--cyber-primary) !important;
  border-left: 3px solid var(--cyber-primary) !important;
  font-weight: 600 !important;
}

.aside-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  min-width: auto !important;
  height: 42px !important;
  line-height: 42px !important;
  font-size: 13px !important;
}

.aside-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(0, 240, 255, 0.1) !important;
}

.menu-item-external {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  height: 46px;
  cursor: pointer;
  color: var(--cyber-text-dim);
  transition: all 0.3s ease;
  font-size: 14px;
  margin: 2px 0;
  border-radius: 6px;
  border-left: 3px solid transparent;
}

.menu-item-external:hover {
  color: var(--cyber-secondary) !important;
  background: rgba(255, 0, 255, 0.08) !important;
  border-left-color: var(--cyber-secondary) !important;
}

.external-badge {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 2px 6px;
  background: var(--cyber-secondary);
  color: white;
  border-radius: 3px;
  text-transform: uppercase;
  animation: badge-pulse 2s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(255, 0, 255, 0.5);
}
@keyframes badge-pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px rgba(255, 0, 255, 0.5); }
  50% { opacity: 0.7; box-shadow: 0 0 15px rgba(255, 0, 255, 0.8); }
}

.layout-main {
  background: var(--cyber-bg-dark) !important;
  padding: 20px !important;
  overflow-y: auto;
  position: relative;
  height: auto !important;
}

.layout-main::before {
  content: '';
  position: fixed;
  top: 0; left: 240px; right: 0; bottom: 0;
  background:
    radial-gradient(circle at 80% 20%, rgba(0, 240, 255, 0.02) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(255, 0, 255, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.main-content-wrapper {
  position: relative;
  z-index: 1;
}
</style>
