<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <span class="system-title">智慧物流仓储</span>
      </div>
      <div class="header-right">
        <el-dropdown>
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userInfo.realName || userInfo.username }}
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
      <el-aside width="220px" class="layout-aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="aside-menu"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/home" v-if="hasMenuAccess('HOME')">
            <el-icon><House /></el-icon>
            <span>首页</span>
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
              <el-icon><ListCheck /></el-icon>
              <span>盘点管理</span>
            </template>
            <el-menu-item index="/stocktake/stocktake-manage" v-if="hasMenuAccess('STOCKTAKE_MANAGE')">盘点管理</el-menu-item>
          </el-sub-menu>
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
        <router-view />
      </el-main>
    </el-container>
    <CopawChatbot />
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
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
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100%;
}

.layout-header {
  background: #1890ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
}

.system-title {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.header-right {
  color: white;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.layout-aside {
  background: #001529;
  overflow-y: auto;
}

.aside-menu {
  border: none;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
