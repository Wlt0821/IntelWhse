import { createRouter, createWebHistory } from 'vue-router'
import { isAdmin, canAccessRoute } from '@/utils/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/customer',
        name: 'Customer',
        component: () => import('@/views/base/Customer.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/supplier',
        name: 'Supplier',
        component: () => import('@/views/base/Supplier.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/goods',
        name: 'Goods',
        component: () => import('@/views/base/Goods.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/zone',
        name: 'Zone',
        component: () => import('@/views/base/Zone.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/location',
        name: 'Location',
        component: () => import('@/views/base/Location.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'base/container',
        name: 'Container',
        component: () => import('@/views/base/Container.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order/customer-order',
        name: 'CustomerOrder',
        component: () => import('@/views/order/CustomerOrder.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order/order-process',
        name: 'OrderProcess',
        component: () => import('@/views/order/OrderProcess.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order/picking-plan',
        name: 'PickingPlan',
        component: () => import('@/views/order/PickingPlan.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'inbound/inbound-plan',
        name: 'InboundPlan',
        component: () => import('@/views/inbound/InboundPlan.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'inbound/inbound-task',
        name: 'InboundTask',
        component: () => import('@/views/inbound/InboundTask.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'outbound/picking-task',
        name: 'PickingTask',
        component: () => import('@/views/outbound/PickingTask.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'outbound/packing-list',
        name: 'PackingList',
        component: () => import('@/views/outbound/PackingList.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'replenish/replenish-manage',
        name: 'ReplenishManage',
        component: () => import('@/views/replenish/ReplenishManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'transfer/transfer-manage',
        name: 'TransferManage',
        component: () => import('@/views/transfer/TransferManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'stocktake/stocktake-manage',
        name: 'StocktakeManage',
        component: () => import('@/views/stocktake/StocktakeManage.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'stocktake/stocktake-data',
        name: 'StocktakeData',
        component: () => import('@/views/stocktake/StocktakeData.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'inventory/realtime',
        name: 'InventoryRealtime',
        component: () => import('@/views/inventory/InventoryRealtime.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'drone-monitor',
        name: 'DroneMonitor',
        component: () => import('@/views/DroneMonitor.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'query/data-query',
        name: 'DataQuery',
        component: () => import('@/views/query/DataQuery.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/Role.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/User.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/screen',
    name: 'DashboardScreen',
    component: () => import('@/views/dashboard/DashboardScreen.vue'),
    meta: { requiresAuth: true, fullscreen: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  let token = localStorage.getItem('token')
  
  if (to.query.t && !token) {
    token = to.query.t
    localStorage.setItem('token', token)
  }
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.fullscreen) {
    next()
  } else if (to.meta.requiresAdmin && !isAdmin()) {
    next('/home')
  } else if (to.path !== '/login' && to.path !== '/home' && !isAdmin() && !canAccessRoute(to.path)) {
    next('/home')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
