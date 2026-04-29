<template>
  <div class="home-container">
    <div class="page-header">
      <div class="header-decoration left"></div>
      <h2 class="page-title">
        <span class="title-icon">◆</span>
        智慧物流仓储控制台
        <span class="title-icon">◆</span>
      </h2>
      <div class="header-decoration right"></div>
    </div>

    <div class="card-row">
      <div class="cyber-card personal-card">
        <div class="card-glow"></div>
        <div class="card-header">
          <el-icon class="header-icon"><UserFilled /></el-icon>
          <span>个人信息</span>
          <div class="header-line"></div>
        </div>
        <div class="card-content">
          <div class="info-item">
            <span class="label">
              <span class="label-dot"></span>
              姓名
            </span>
            <span class="value">{{ userInfo.realName || userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">
              <span class="label-dot"></span>
              账号
            </span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">
              <span class="label-dot"></span>
              角色
            </span>
            <span class="value role-badge">系统管理员</span>
          </div>
          <div class="info-item">
            <span class="label">
              <span class="label-dot"></span>
              队伍
            </span>
            <span class="value">{{ userInfo.team || 'N/A' }}</span>
          </div>
        </div>
        <div class="corner-tl"></div>
        <div class="corner-tr"></div>
        <div class="corner-bl"></div>
        <div class="corner-br"></div>
      </div>

      <div class="cyber-card operation-card">
        <div class="card-glow"></div>
        <div class="card-header">
          <el-icon class="header-icon"><Clock /></el-icon>
          <span>操作统计</span>
          <div class="header-line"></div>
        </div>
        <div class="card-content">
          <div class="operation-stats">
            <div class="stat-item">
              <div class="stat-icon-wrapper">
                <el-icon><Document /></el-icon>
              </div>
              <span class="stat-label">业务数据</span>
              <span class="stat-value">{{ statistics.businessDataCount || 0 }}</span>
              <div class="stat-bar">
                <div class="stat-bar-fill" :style="{ width: Math.min(statistics.businessDataCount * 10, 100) + '%' }"></div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-icon-wrapper alt">
                <el-icon><DataLine /></el-icon>
              </div>
              <span class="stat-label">作业数据</span>
              <span class="stat-value">{{ statistics.workDataCount || 0 }}</span>
              <div class="stat-bar">
                <div class="stat-bar-fill" :style="{ width: Math.min(statistics.workDataCount * 10, 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="corner-tl"></div>
        <div class="corner-tr"></div>
        <div class="corner-bl"></div>
        <div class="corner-br"></div>
      </div>

      <div class="cyber-card partner-card">
        <div class="card-glow"></div>
        <div class="card-header">
          <el-icon class="header-icon"><OfficeBuilding /></el-icon>
          <span>合作伙伴</span>
          <div class="header-line"></div>
        </div>
        <div class="card-content">
          <div class="partner-stats">
            <div class="stat-item">
              <div class="partner-icon customer">
                <el-icon><User /></el-icon>
              </div>
              <span class="stat-label">客户数量</span>
              <span class="stat-value">{{ statistics.customerCount || 0 }}</span>
              <span class="stat-unit">家</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="partner-icon supplier">
                <el-icon><Van /></el-icon>
              </div>
              <span class="stat-label">供应商数量</span>
              <span class="stat-value">{{ statistics.supplierCount || 0 }}</span>
              <span class="stat-unit">家</span>
            </div>
          </div>
        </div>
        <div class="corner-tl"></div>
        <div class="corner-tr"></div>
        <div class="corner-bl"></div>
        <div class="corner-br"></div>
      </div>
    </div>

    <div class="cyber-card chart-card">
      <div class="card-glow"></div>
      <div class="chart-header">
        <div class="chart-title-section">
          <span class="chart-icon">◈</span>
          <span>数据分析中心</span>
          <div class="live-indicator">
            <span class="live-dot"></span>
            LIVE
          </div>
        </div>
        <el-radio-group v-model="chartType" size="small" class="cyber-radio-group">
          <el-radio-button value="inventory">库存分析</el-radio-button>
          <el-radio-button value="inbound">入库统计</el-radio-button>
          <el-radio-button value="outbound">出库统计</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart-content">
        <div v-if="chartType === 'inventory'" class="inventory-chart">
          <div class="custom-chart">
            <div class="chart-title-bar">
              <span>商品库存量分布</span>
              <div class="data-info">实时更新</div>
            </div>
            <div class="chart-bars-wrapper">
              <button class="scroll-btn scroll-left" @click="scrollChart('left')" v-if="inventoryChartData.length > 8">‹</button>
              <div class="chart-bars" ref="barsRef">
                <div v-for="(item, index) in inventoryChartData" :key="index" class="bar-item">
                  <div class="bar-label">{{ item.name }}</div>
                  <div class="bar-wrapper">
                    <div
                      class="bar"
                      :style="{
                        height: (item.value / maxInventoryValue * 240) + 'px',
                        background: `linear-gradient(180deg, ${barColors[index % barColors.length]} 0%, transparent 100%)`,
                        boxShadow: `0 0 20px ${barColors[index % barColors.length]}40`
                      }"
                    ></div>
                    <div class="bar-glow" :style="{ height: (item.value / maxInventoryValue * 240) + 'px', background: barColors[index % barColors.length] }"></div>
                  </div>
                  <div class="bar-value">{{ item.value }}</div>
                </div>
              </div>
              <button class="scroll-btn scroll-right" @click="scrollChart('right')" v-if="inventoryChartData.length > 8">›</button>
            </div>
          </div>
        </div>

        <div v-if="chartType === 'inbound'" class="inbound-chart">
          <div class="custom-chart">
            <div class="chart-title-bar">
              <span>入库计划状态分布</span>
              <div class="data-info">实时更新</div>
            </div>
            <div class="pie-chart-wrapper">
              <div class="pie-chart-container">
                <div class="pie-chart" :style="{ background: getInboundPieBackground() }">
                  <div class="pie-center">
                    <div class="pie-center-text">入库</div>
                    <div class="pie-center-sub">INBOUND</div>
                  </div>
                </div>
                <div class="pie-ring"></div>
              </div>
              <div class="pie-legend">
                <div v-for="(item, index) in inboundChartData" :key="index" class="legend-item">
                  <div class="legend-color" :style="{ backgroundColor: pieColors[index], boxShadow: `0 0 10px ${pieColors[index]}` }"></div>
                  <span class="legend-label">{{ item.name }}</span>
                  <div class="legend-right">
                    <span class="legend-value">{{ item.value }}</span>
                    <span class="legend-percent">{{ item.percent }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="chartType === 'outbound'" class="outbound-chart">
          <div class="custom-chart">
            <div class="chart-title-bar">
              <span>订单出库状态分布</span>
              <div class="data-info">实时更新</div>
            </div>
            <div class="pie-chart-wrapper">
              <div class="pie-chart-container">
                <div class="pie-chart" :style="{ background: getOrderPieBackground() }">
                  <div class="pie-center">
                    <div class="pie-center-text">订单</div>
                    <div class="pie-center-sub">ORDER</div>
                  </div>
                </div>
                <div class="pie-ring"></div>
              </div>
              <div class="pie-legend">
                <div v-for="(item, index) in orderChartData" :key="index" class="legend-item">
                  <div class="legend-color" :style="{ backgroundColor: orderPieColors[index], boxShadow: `0 0 10px ${orderPieColors[index]}` }"></div>
                  <span class="legend-label">{{ item.name }}</span>
                  <div class="legend-right">
                    <span class="legend-value">{{ item.value }}</span>
                    <span class="legend-percent">{{ item.percent }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="corner-tl"></div>
      <div class="corner-tr"></div>
      <div class="corner-bl"></div>
      <div class="corner-br"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { UserFilled, Clock, Document, DataLine, OfficeBuilding, User, Van } from '@element-plus/icons-vue'
import request from '@/utils/request'

const chartType = ref('inventory')
const statistics = ref({})
const inventoryChartData = ref([])
const inboundChartData = ref([])
const orderChartData = ref([])
const barsRef = ref(null)

const barColors = ['#00f0ff', '#ff00ff', '#00ff88', '#ffff00', '#ff3366', '#9966ff', '#ff6600', '#00ffff']
const pieColors = ['#00f0ff', '#00ff88', '#ffff00', '#ff3366', '#9966ff', '#ff6600']
const orderPieColors = ['#8ba4b8', '#00f0ff', '#ffff00', '#00ff88', '#ff3366']

function scrollChart(direction) {
  if (!barsRef.value) return
  const scrollAmount = 300
  barsRef.value.scrollBy({
    left: direction === 'left' ? -scrollAmount : scrollAmount,
    behavior: 'smooth'
  })
}

const maxInventoryValue = computed(() => {
  if (inventoryChartData.value.length === 0) return 1
  return Math.max(...inventoryChartData.value.map(item => item.value))
})

const getInboundPieBackground = () => {
  if (inboundChartData.value.length === 0) {
    return 'conic-gradient(rgba(0, 240, 255, 0.1) 0deg 360deg)'
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
    return 'conic-gradient(rgba(0, 240, 255, 0.1) 0deg 360deg)'
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
    const res = await request.get('/home/statistics')
    statistics.value = res.data
  } catch (e) {
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
    const res = await request.get('/home/inventory-chart')
    inventoryChartData.value = res.data || []
  } catch (e) {
    inventoryChartData.value = []
  }
}

const loadInboundChart = async () => {
  try {
    const res = await request.get('/home/inbound-chart')
    inboundChartData.value = res.data || []
  } catch (e) {
    inboundChartData.value = []
  }
}

const loadOrderChart = async () => {
  try {
    const res = await request.get('/home/order-chart')
    orderChartData.value = res.data || []
  } catch (e) {
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
  padding: 5px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
  position: relative;
  padding: 15px 0;
}

.header-decoration {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyber-border));
  position: relative;
  overflow: hidden;
}

.header-decoration::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: var(--cyber-primary);
  animation: line-flow 4s linear infinite;
  box-shadow: 0 0 10px var(--cyber-primary);
}

.header-decoration.right::after {
  animation-delay: 2s;
  animation-direction: reverse;
}

@keyframes line-flow {
  to { left: 150%; }
}

.page-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 4px;
  color: var(--cyber-primary);
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.6), 0 0 40px rgba(0, 240, 255, 0.3);
  margin: 0 30px;
  text-transform: uppercase;
  background: linear-gradient(135deg, #00f0ff 0%, #ffffff 50%, #00f0ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% auto;
  animation: text-shine 4s linear infinite;
  white-space: nowrap;
}

@keyframes text-shine {
  to { background-position: 200% center; }
}

.title-icon {
  font-size: 16px;
  opacity: 0.7;
}

.card-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 25px;
}

.cyber-card {
  background: var(--cyber-bg-card);
  border: 1px solid var(--cyber-border);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  padding: 25px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cyber-card:hover {
  border-color: rgba(0, 240, 255, 0.5);
  box-shadow:
    0 0 30px rgba(0, 240, 255, 0.15),
    inset 0 0 30px rgba(0, 240, 255, 0.03);
  transform: translateY(-3px);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent);
  animation: card-glow-flow 3s ease-in-out infinite;
}

@keyframes card-glow-flow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.personal-card .card-glow {
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
}

.operation-card .card-glow {
  background: linear-gradient(90deg, transparent, #ff00ff, transparent);
}

.partner-card .card-glow {
  background: linear-gradient(90deg, transparent, #00ff88, transparent);
}

.corner-tl,
.corner-tr,
.corner-bl,
.corner-br {
  position: absolute;
  width: 20px;
  height: 20px;
  pointer-events: none;
}

.corner-tl {
  top: -1px;
  left: -1px;
  border-top: 2px solid var(--cyber-primary);
  border-left: 2px solid var(--cyber-primary);
  border-radius: 12px 0 0 0;
}

.corner-tr {
  top: -1px;
  right: -1px;
  border-top: 2px solid var(--cyber-primary);
  border-right: 2px solid var(--cyber-primary);
  border-radius: 0 12px 0 0;
}

.corner-bl {
  bottom: -1px;
  left: -1px;
  border-bottom: 2px solid var(--cyber-primary);
  border-left: 2px solid var(--cyber-primary);
  border-radius: 0 0 0 12px;
}

.corner-br {
  bottom: -1px;
  right: -1px;
  border-bottom: 2px solid var(--cyber-primary);
  border-right: 2px solid var(--cyber-primary);
  border-radius: 0 0 12px 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Orbitron', sans-serif;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--cyber-primary);
  text-transform: uppercase;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.15);
  position: relative;
}

.header-icon {
  font-size: 18px;
  filter: drop-shadow(0 0 8px currentColor);
}

.header-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--cyber-border), transparent);
  margin-left: 10px;
}

.card-content {
  position: relative;
  z-index: 2;
}

.info-item {
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 240, 255, 0.05);
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--cyber-text-dim);
  font-weight: 500;
  letter-spacing: 1px;
}

.label-dot {
  width: 6px;
  height: 6px;
  background: var(--cyber-primary);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--cyber-primary);
  animation: dot-pulse 2s ease-in-out infinite;
}

@keyframes dot-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.9); }
}

.value {
  font-family: 'Rajdhani', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--cyber-text);
  letter-spacing: 1px;
}

.role-badge {
  display: inline-block;
  padding: 3px 12px;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.2), rgba(0, 102, 255, 0.2));
  border: 1px solid var(--cyber-primary);
  border-radius: 4px;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 2px !important;
  color: var(--cyber-primary) !important;
  text-transform: uppercase;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.2);
}

.operation-stats,
.partner-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 20px;
  padding: 15px 0;
}

.stat-item {
  flex: 1;
  text-align: center;
  position: relative;
}

.stat-icon-wrapper {
  width: 45px;
  height: 45px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 0, 255, 0.15), rgba(255, 0, 255, 0.05));
  border: 1px solid rgba(255, 0, 255, 0.3);
  border-radius: 10px;
  font-size: 22px;
  color: var(--cyber-secondary);
  box-shadow: 0 0 15px rgba(255, 0, 255, 0.2);
}

.stat-icon-wrapper.alt {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.15), rgba(0, 240, 255, 0.05));
  border-color: rgba(0, 240, 255, 0.3);
  color: var(--cyber-primary);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
}

.partner-icon {
  width: 40px;
  height: 40px;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 20px;
}

.partner-icon.customer {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.15), rgba(0, 102, 255, 0.05));
  border: 1px solid rgba(0, 240, 255, 0.3);
  color: var(--cyber-primary);
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
}

.partner-icon.supplier {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.15), rgba(0, 136, 68, 0.05));
  border: 1px solid rgba(0, 255, 136, 0.3);
  color: var(--cyber-accent);
  box-shadow: 0 0 12px rgba(0, 255, 136, 0.2);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--cyber-text-dim);
  margin-bottom: 8px;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.stat-value {
  display: block;
  font-family: 'Orbitron', monospace;
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--cyber-primary), #ffffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
}

.stat-unit {
  display: block;
  font-size: 11px;
  color: var(--cyber-text-dim);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.stat-bar {
  width: 80%;
  height: 3px;
  background: rgba(0, 240, 255, 0.1);
  border-radius: 2px;
  margin: 8px auto 0;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--cyber-primary), var(--cyber-secondary));
  border-radius: 2px;
  box-shadow: 0 0 10px var(--cyber-primary);
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-divider {
  width: 1px;
  height: 80px;
  background: linear-gradient(180deg, transparent, var(--cyber-border), transparent);
}

.chart-card {
  padding: 25px 30px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.15);
}

.chart-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Orbitron', sans-serif;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--cyber-primary);
  text-transform: uppercase;
}

.chart-icon {
  font-size: 20px;
  filter: drop-shadow(0 0 10px currentColor);
  animation: icon-rotate 10s linear infinite;
}

@keyframes icon-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--cyber-accent);
  padding: 3px 10px;
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 4px;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: var(--cyber-accent);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--cyber-accent);
  animation: live-blink 1.5s ease-in-out infinite;
}

@keyframes live-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.cyber-radio-group {
  --el-radio-button-checked-bg-color: var(--cyber-primary) !important;
  --el-radio-button-checked-border-color: var(--cyber-primary) !important;
  --el-radio-button-checked-text-color: var(--cyber-bg-dark) !important;
}

.chart-content {
  min-height: 400px;
  padding: 10px 0;
}

.custom-chart {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.chart-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Orbitron', sans-serif;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--cyber-text);
  margin-bottom: 20px;
  text-transform: uppercase;
}

.data-info {
  font-size: 10px;
  color: var(--cyber-accent);
  letter-spacing: 1px;
  padding: 3px 10px;
  background: rgba(0, 255, 136, 0.08);
  border: 1px solid rgba(0, 255, 136, 0.2);
  border-radius: 3px;
}

.chart-bars-wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.scroll-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 28px;
  height: 44px;
  border: 1px solid var(--cyber-border);
  background: rgba(0, 20, 40, 0.85);
  color: var(--cyber-primary);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);
}

.scroll-btn:hover {
  background: rgba(0, 240, 255, 0.15);
  border-color: var(--cyber-primary);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.3);
}

.scroll-left {
  left: 8px;
  border-radius: 6px 0 0 6px;
}

.scroll-right {
  right: 8px;
  border-radius: 0 6px 6px 0;
}

.chart-bars {
  display: flex;
  justify-content: flex-start;
  align-items: flex-end;
  height: 340px;
  padding: 0 40px;
  gap: 8px;
  position: relative;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
}

.chart-bars::-webkit-scrollbar {
  height: 5px;
}
.chart-bars::-webkit-scrollbar-track {
  background: rgba(0, 20, 40, 0.3);
  border-radius: 3px;
}
.chart-bars::-webkit-scrollbar-thumb {
  background: linear-gradient(90deg, var(--cyber-primary), var(--cyber-secondary));
  border-radius: 3px;
}
.chart-bars::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(90deg, #33f1ff, #ff33ff);
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 72px;
  position: relative;
  flex-shrink: 0;
}

.bar-label {
  font-size: 11px;
  color: var(--cyber-text-dim);
  text-align: center;
  word-break: break-word;
  max-width: 80px;
  font-weight: 500;
  line-height: 1.35;
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.bar-wrapper {
  height: 260px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  position: relative;
  width: 100%;
  flex-shrink: 0;
}

.bar {
  width: 32px;
  border-radius: 4px 4px 0 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 2;
  cursor: pointer;
}

.bar:hover {
  transform: scaleY(1.02);
  transform-origin: bottom;
  filter: brightness(1.2);
}

.bar-glow {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  border-radius: 4px 4px 0 0;
  opacity: 0.3;
  filter: blur(8px);
  z-index: 1;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar-value {
  font-family: 'Orbitron', monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--cyber-primary);
  margin-top: 8px;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  flex-shrink: 0;
}

.pie-chart-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 60px;
  padding: 20px 0;
}

.pie-chart-container {
  position: relative;
}

.pie-chart {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  position: relative;
  box-shadow:
    0 0 30px rgba(0, 240, 255, 0.2),
    inset 0 0 30px rgba(0, 0, 0, 0.3);
  animation: pie-rotate 20s linear infinite;
}

@keyframes pie-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 110px;
  height: 110px;
  background: var(--cyber-bg-dark);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow:
    inset 0 0 20px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(0, 240, 255, 0.15);
  z-index: 2;
}

.pie-center-text {
  font-family: 'Orbitron', sans-serif;
  font-size: 18px;
  font-weight: 800;
  color: var(--cyber-primary);
  letter-spacing: 2px;
}

.pie-center-sub {
  font-size: 10px;
  color: var(--cyber-text-dim);
  letter-spacing: 3px;
  margin-top: 3px;
}

.pie-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border: 2px solid transparent;
  border-top-color: var(--cyber-primary);
  border-radius: 50%;
  animation: ring-rotate 4s linear infinite;
  opacity: 0.5;
}

@keyframes ring-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 180px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: rgba(0, 240, 255, 0.03);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.legend-item:hover {
  background: rgba(0, 240, 255, 0.06);
  border-color: rgba(0, 240, 255, 0.2);
  transform: translateX(5px);
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  font-size: 13px;
  color: var(--cyber-text);
  font-weight: 500;
  letter-spacing: 0.5px;
}

.legend-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.legend-value {
  font-family: 'Orbitron', monospace;
  font-size: 14px;
  font-weight: 700;
  color: var(--cyber-primary);
}

.legend-percent {
  font-size: 11px;
  color: var(--cyber-text-dim);
  font-weight: 600;
}
</style>
