<template>
  <div class="home-container">
    <h2 class="page-title">智慧物流仓储</h2>
    
    <div class="card-row">
      <el-card class="info-card personal-card">
        <template #header>
          <div class="card-header">
            <el-icon><UserFilled /></el-icon>
            <span>个人信息</span>
          </div>
        </template>
        <div class="card-content">
          <div class="info-item">
            <span class="label">姓名：</span>
            <span class="value">{{ userInfo.realName || userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">账号：</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">角色：</span>
            <span class="value">信息录入员</span>
          </div>
          <div class="info-item">
            <span class="label">队伍：</span>
            <span class="value">{{ userInfo.team || '-' }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="info-card operation-card">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>操作信息</span>
          </div>
        </template>
        <div class="card-content">
          <div class="operation-stats">
            <div class="stat-item">
              <span class="stat-label">业务数据</span>
              <span class="stat-value">{{ statistics.businessDataCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">作业数据</span>
              <span class="stat-value">{{ statistics.workDataCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="info-card partner-card">
        <template #header>
          <div class="card-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>合作伙伴</span>
          </div>
        </template>
        <div class="card-content">
          <div class="partner-stats">
            <div class="stat-item">
              <span class="stat-label">客户数量</span>
              <span class="stat-value">{{ statistics.customerCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">供应商数量</span>
              <span class="stat-value">{{ statistics.supplierCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>数据统计表</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button label="inventory">商品库存量</el-radio-button>
            <el-radio-button label="inbound">商品入库</el-radio-button>
            <el-radio-button label="outbound">商品出库</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="chart-content">
        <div v-if="chartType === 'inventory'" class="inventory-chart">
          <div class="custom-chart">
            <div class="chart-title">商品库存统计</div>
            <div class="chart-bars">
              <div v-for="(item, index) in inventoryChartData" :key="index" class="bar-item">
                <div class="bar-label">{{ item.name }}</div>
                <div class="bar-wrapper">
                  <div 
                  class="bar" 
                  :style="{ 
                    height: (item.value / maxInventoryValue * 200) + 'px',
                    backgroundColor: barColors[index % barColors.length]
                  }"
                ></div>
                </div>
                <div class="bar-value">{{ item.value }}</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="chartType === 'inbound'" class="inbound-chart">
          <div class="custom-chart">
            <div class="chart-title">入库计划状态统计</div>
            <div class="pie-chart-wrapper">
              <div class="pie-chart" :style="{ background: getInboundPieBackground() }">
                <div class="pie-center"></div>
              </div>
              <div class="pie-legend">
                <div v-for="(item, index) in inboundChartData" :key="index" class="legend-item">
                  <div class="legend-color" :style="{ backgroundColor: pieColors[index] }"></div>
                  <span class="legend-label">{{ item.name }}</span>
                  <span class="legend-value">{{ item.value }} ({{ item.percent }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="chartType === 'outbound'" class="outbound-chart">
          <div class="custom-chart">
            <div class="chart-title">订单状态统计</div>
            <div class="pie-chart-wrapper">
              <div class="pie-chart" :style="{ background: getOrderPieBackground() }">
                <div class="pie-center"></div>
              </div>
              <div class="pie-legend">
                <div v-for="(item, index) in orderChartData" :key="index" class="legend-item">
                  <div class="legend-color" :style="{ backgroundColor: orderPieColors[index] }"></div>
                  <span class="legend-label">{{ item.name }}</span>
                  <span class="legend-value">{{ item.value }} ({{ item.percent }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'

const chartType = ref('inventory')
const statistics = ref({})
const inventoryChartData = ref([])
const inboundChartData = ref([])
const orderChartData = ref([])

const barColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00CED1', '#FF6347', '#9370DB']
const pieColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00CED1']
const orderPieColors = ['#909399', '#409EFF', '#E6A23C', '#67C23A', '#F56C6C']

const maxInventoryValue = computed(() => {
  if (inventoryChartData.value.length === 0) return 1
  return Math.max(...inventoryChartData.value.map(item => item.value))
})

const getInboundPieBackground = () => {
  if (inboundChartData.value.length === 0) {
    return 'conic-gradient(#E5E7EB 0deg 360deg)'
  }
  let currentDegree = 0
  const gradients = []
  inboundChartData.value.forEach((item, index) => {
    const endDegree = currentDegree + (item.percent * 3.6)
    gradients.push(`${pieColors[index]} ${currentDegree}deg ${endDegree}deg`)
    currentDegree = endDegree
  })
  return `conic-gradient(${gradients.join(', ')})`
}

const getOrderPieBackground = () => {
  if (orderChartData.value.length === 0) {
    return 'conic-gradient(#E5E7EB 0deg 360deg)'
  }
  let currentDegree = 0
  const gradients = []
  orderChartData.value.forEach((item, index) => {
    const endDegree = currentDegree + (item.percent * 3.6)
    gradients.push(`${orderPieColors[index]} ${currentDegree}deg ${endDegree}deg`)
    currentDegree = endDegree
  })
  return `conic-gradient(${gradients.join(', ')})`
}

const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo')) || {}
  } catch {
    return {}
  }
})

const loadStatistics = async () => {
  try {
    console.log('正在加载统计数据...')
    const res = await request.get('/home/statistics')
    console.log('统计数据响应:', res)
    statistics.value = res.data
  } catch (e) {
    console.error('加载统计数据失败:', e)
    console.error('错误详情:', e.response?.data || e.message)
    statistics.value = {
      customerCount: 0,
      supplierCount: 0,
      businessDataCount: 0,
      workDataCount: 0
    }
  }
}

const loadInventoryChart = async () => {
  try {
    console.log('正在加载库存图表数据...')
    const res = await request.get('/home/inventory-chart')
    console.log('库存图表数据响应:', res)
    inventoryChartData.value = res.data || []
  } catch (e) {
    console.error('加载库存图表失败:', e)
    console.error('错误详情:', e.response?.data || e.message)
    inventoryChartData.value = []
  }
}

const loadInboundChart = async () => {
  try {
    console.log('正在加载入库图表数据...')
    const res = await request.get('/home/inbound-chart')
    console.log('入库图表数据响应:', res)
    inboundChartData.value = res.data || []
  } catch (e) {
    console.error('加载入库图表失败:', e)
    console.error('错误详情:', e.response?.data || e.message)
    inboundChartData.value = []
  }
}

const loadOrderChart = async () => {
  try {
    console.log('正在加载出库/订单图表数据...')
    const res = await request.get('/home/order-chart')
    console.log('出库/订单图表数据响应:', res)
    orderChartData.value = res.data || []
  } catch (e) {
    console.error('加载出库/订单图表失败:', e)
    console.error('错误详情:', e.response?.data || e.message)
    orderChartData.value = []
  }
}

watch(chartType, (newType) => {
  if (newType === 'inventory') {
    loadInventoryChart()
  } else if (newType === 'inbound') {
    loadInboundChart()
  } else if (newType === 'outbound') {
    loadOrderChart()
  }
})

onMounted(() => {
  loadStatistics()
  loadInventoryChart()
})
</script>

<style scoped>
.home-container {
  padding: 10px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.card-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.info-card {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: white;
}

.card-content {
  padding: 10px 0;
}

.personal-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.personal-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.personal-card :deep(.el-card__body) {
  color: white;
}

.operation-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.operation-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.operation-card :deep(.el-card__body) {
  color: white;
}

.partner-card {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
}

.partner-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.partner-card :deep(.el-card__body) {
  color: white;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.info-item .label {
  opacity: 0.9;
  min-width: 60px;
}

.info-item .value {
  font-weight: 500;
}

.operation-stats,
.partner-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.stat-value {
  display: block;
  font-size: 32px;
  font-weight: bold;
}

.chart-card {
  margin-top: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.chart-content {
  min-height: 350px;
  padding: 20px 0;
}

.custom-chart {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.chart-title {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #303133;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 280px;
  padding: 0 20px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 60px;
}

.bar-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 10px;
  text-align: center;
  word-break: break-all;
  max-width: 70px;
}

.bar-wrapper {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 30px;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s;
  background-color: #409EFF;
}

.bar:hover {
  opacity: 0.8;
  transform: scaleY(1.02);
  transform-origin: bottom;
}

.bar-value {
  font-size: 12px;
  color: #409EFF;
  margin-top: 8px;
  font-weight: bold;
}

.pie-chart-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
}

.pie-chart {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  position: relative;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background-color: white;
  border-radius: 50%;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.legend-label {
  font-size: 14px;
  color: #606266;
  min-width: 70px;
}

.legend-value {
  font-size: 14px;
  color: #303133;
  font-weight: bold;
}
</style>
