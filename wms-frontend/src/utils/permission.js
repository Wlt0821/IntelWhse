const menuRouteMap = {
  'HOME': '/home',
  'CUSTOMER': '/base/customer',
  'SUPPLIER': '/base/supplier',
  'CONTAINER': '/base/container',
  'GOODS': '/base/goods',
  'ZONE': '/base/zone',
  'LOCATION': '/base/location',
  'CUSTOMER_ORDER': '/order/customer-order',
  'ORDER_PROCESS': '/order/order-process',
  'PICKING_PLAN': '/order/picking-plan',
  'INBOUND_PLAN': '/inbound/inbound-plan',
  'INBOUND_TASK': '/inbound/inbound-task',
  'PICKING_TASK': '/outbound/picking-task',
  'PACKING': '/outbound/packing-list',
  'REPLENISH_MANAGE': '/replenish/replenish-manage',
  'TRANSFER_MANAGE': '/transfer/transfer-manage',
  'STOCKTAKE_MANAGE': '/stocktake/stocktake-manage',
  'STOCKTAKE_DATA': '/stocktake/stocktake-data',
  'INVENTORY_REALTIME': '/inventory/realtime',
  'DATA_QUERY': '/query/data-query',
  'USER': '/system/user',
  'ROLE': '/system/role',
  'MENU': '/system/menu',
  'OPER_LOG': '/system/log'
}

export function getUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo')) || {}
  } catch {
    return {}
  }
}

export function isAdmin() {
  const userInfo = getUserInfo()
  return userInfo.id === 1 || userInfo.username === 'admin'
}

export function getUserRoleIds() {
  const userInfo = getUserInfo()
  return userInfo.roleIds || []
}

export function getUserMenus() {
  const userInfo = getUserInfo()
  return userInfo.menuIds || []
}

export function hasPermission(route) {
  if (isAdmin()) {
    return true
  }
  const userMenus = getUserMenus()
  const menuCodes = Object.keys(menuRouteMap)
  for (const code of menuCodes) {
    if (menuRouteMap[code] === route) {
      return userMenus.includes(code)
    }
  }
  return false
}

export function canAccessRoute(path) {
  if (isAdmin()) {
    return true
  }
  const userMenus = getUserMenus()
  const menuCodes = Object.keys(menuRouteMap)
  for (const code of menuCodes) {
    if (path.startsWith(menuRouteMap[code])) {
      return userMenus.includes(code)
    }
  }
  return false
}

export function getAccessibleMenus() {
  if (isAdmin()) {
    return Object.keys(menuRouteMap)
  }
  return getUserMenus()
}
