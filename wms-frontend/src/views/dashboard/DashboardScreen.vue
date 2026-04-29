<template>
  <div class="dashboard-screen">
    <!-- 背景粒子效果 -->
    <div class="bg-particles">
      <div v-for="i in 30" :key="'p'+i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 网格背景 -->
    <div class="grid-bg"></div>

    <!-- 扫描线效果 -->
    <div class="scan-line"></div>

    <!-- 顶部标题栏 -->
    <header class="screen-header">
      <div class="header-glow left"></div>
      <div class="header-glow right"></div>
      <div class="header-left">
        <span class="back-btn" @click="goBack">
          <span class="btn-icon">◀</span>
          返回系统
        </span>
      </div>
      <h1 class="title">
        <span class="title-deco left">◈</span>
        智慧物流仓储 · 实时盘点监控中心
        <span class="title-deco right">◈</span>
      </h1>
      <div class="header-right">
        <div class="datetime-box">
          <div class="time">{{ currentTime.split(' ')[1] || '' }}</div>
          <div class="date">{{ currentTime.split(' ')[0] || '' }}</div>
        </div>
      </div>
      <div class="header-line-animated"></div>
    </header>

    <!-- 主体内容区 -->
    <main class="screen-body">
      <!-- 左侧面板 - 统计数据 -->
      <section class="panel panel-left">
        <div class="card stats-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title">
            <span class="title-icon">◆</span>
            盘点统计概览
            <span class="live-badge">LIVE</span>
          </h3>
          <div class="stats-grid">
            <div class="stat-item total">
              <div class="stat-glow"></div>
              <div class="stat-value">{{ stocktakeStats.total }}</div>
              <div class="stat-label">总盘点项</div>
              <div class="stat-icon-bg">∑</div>
            </div>
            <div class="stat-item normal">
              <div class="stat-glow"></div>
              <div class="stat-value">{{ stocktakeStats.normal }}</div>
              <div class="stat-label">正常</div>
              <div class="stat-icon-bg">✓</div>
            </div>
            <div class="stat-item surplus">
              <div class="stat-glow"></div>
              <div class="stat-value">{{ stocktakeStats.surplus }}</div>
              <div class="stat-label">盘盈</div>
              <div class="stat-icon-bg">↑</div>
            </div>
            <div class="stat-item deficit">
              <div class="stat-glow"></div>
              <div class="stat-value">{{ stocktakeStats.deficit }}</div>
              <div class="stat-label">盘亏</div>
              <div class="stat-icon-bg">↓</div>
            </div>
          </div>
          <div class="stats-percentage">
            <div class="percentage-bar">
              <div
                class="percentage-fill normal-fill"
                :style="{ width: normalPercentage + '%' }"
              ></div>
              <div
                class="percentage-fill surplus-fill"
                :style="{ width: surplusPercentage + '%', left: normalPercentage + '%' }"
              ></div>
              <div
                class="percentage-fill deficit-fill"
                :style="{ width: deficitPercentage + '%', left: (normalPercentage + surplusPercentage) + '%' }"
              ></div>
            </div>
            <div class="percentage-legend">
              <span class="legend-item normal-legend"><i></i>正常 {{ normalPercentage }}%</span>
              <span class="legend-item surplus-legend"><i></i>盘盈 {{ surplusPercentage }}%</span>
              <span class="legend-item deficit-legend"><i></i>盘亏 {{ deficitPercentage }}%</span>
            </div>
          </div>
        </div>

        <div class="card records-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title">
            <span class="title-icon">▣</span>
            最新盘点记录
          </h3>
          <div class="records-list" v-if="latestRecords.length > 0">
            <div
              v-for="(record, index) in latestRecords"
              :key="index"
              class="record-item"
              :class="{ 'record-latest': index === 0, record }"
            >
              <div class="record-index">{{ String(index + 1).padStart(2, '0') }}</div>
              <div class="record-main">
                <div class="record-name">{{ record.goodsName }}</div>
                <div class="record-location">{{ record.locationName }}</div>
              </div>
              <div class="record-compare">
                <div class="compare-item">
                  <span class="compare-label">平台</span>
                  <span class="compare-value platform">{{ record.platformQuantity }}</span>
                </div>
                <div class="compare-arrow">→</div>
                <div class="compare-item">
                  <span class="compare-label">实际</span>
                  <span class="compare-value" :class="getStatusClass(record.stocktakeStatus)">
                    {{ record.actualQuantity }}
                  </span>
                </div>
              </div>
              <div class="record-status">
                <span :class="['status-badge', record.stocktakeStatus]">
                  {{ getStatusText(record.stocktakeStatus) }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-records">
            <div class="empty-icon">⚠</div>
            <p>暂无盘点数据</p>
          </div>
        </div>

        <div class="card device-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title">
            <span class="title-icon">⬡</span>
            盘点设备状态
          </h3>
          <div class="device-list">
            <div class="device-item online">
              <div class="device-dot"><span class="dot-inner"></span></div>
              <span class="device-name">AGX-主控节点-01</span>
              <span class="device-status">ONLINE</span>
            </div>
            <div class="device-item online">
              <div class="device-dot"><span class="dot-inner"></span></div>
              <span class="device-name">AGX-巡检节点-02</span>
              <span class="device-status">ONLINE</span>
            </div>
            <div class="device-item standby">
              <div class="device-dot"><span class="dot-inner"></span></div>
              <span class="device-name">AGX-备用节点-03</span>
              <span class="device-status">STANDBY</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 中间面板 - 核心盘点数据 -->
      <section class="panel panel-center">
        <div class="card main-table-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <div class="table-header">
            <h3 class="card-title">
              <span class="title-icon">▤</span>
              实时盘点对比表
            </h3>
            <div class="table-stats">
              <span class="update-time">
                <span class="update-icon">⟳</span>
                {{ lastUpdateTime }}
              </span>
            </div>
          </div>
          <div class="table-container">
            <el-table
              :data="stocktakeData"
              :row-class-name="tableRowClassName"
              height="calc(100vh - 320px)"
              class="stocktake-table"
              stripe
            >
              <el-table-column prop="goodsName" label="商品名称" width="180">
                <template #default="{ row }">
                  <div class="goods-cell">
                    <span class="goods-icon">■</span>
                    <span>{{ row.goodsName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="locationName" label="仓位名称" width="160" />
              <el-table-column label="平台库存" width="130" align="right">
                <template #default="{ row }">
                  <span class="platform-quantity">{{ row.platformQuantity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="实际盘点" width="130" align="right">
                <template #default="{ row }">
                  <span :class="['actual-quantity', row.stocktakeStatus]">
                    {{ row.actualQuantity }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="差异数量" width="110" align="right">
                <template #default="{ row }">
                  <span :class="['diff-quantity', row.stocktakeStatus]">
                    {{ row.actualQuantity - row.platformQuantity > 0 ? '+' : '' }}
                    {{ row.actualQuantity - row.platformQuantity }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="盘点状态" width="100" align="center">
                <template #default="{ row }">
                  <span :class="['status-tag', row.stocktakeStatus]">
                    {{ getStatusText(row.stocktakeStatus) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="deviceName" label="盘点设备" width="150">
                <template #default="{ row }">
                  <span class="device-cell">{{ row.deviceName || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="stocktakeTime" label="盘点时间" min-width="160">
                <template #default="{ row }">
                  <span class="time-cell">{{ formatDateTime(row.stocktakeTime) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </section>

      <!-- 右侧面板 - 图表和趋势 -->
      <section class="panel panel-right">
        <div class="card trend-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title">
            <span class="title-icon">◢</span>
            今日盘点趋势
          </h3>
          <div class="trend-chart" ref="trendChartRef"></div>
        </div>

        <div class="card pie-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title">
            <span class="title-icon">◉</span>
            盘点状态分布
          </h3>
          <div class="pie-chart" ref="pieChartRef"></div>
        </div>

        <div class="card alert-card">
          <div class="corner-tl"></div><div class="corner-tr"></div>
          <div class="corner-bl"></div><div class="corner-br"></div>
          <h3 class="card-title alert-title">
            <span class="title-icon warning">⚠</span>
            异常预警
            <span class="alert-count" v-if="abnormalItems.length > 0">{{ abnormalItems.length }}</span>
          </h3>
          <div class="alert-list" v-if="abnormalItems.length > 0">
            <div v-for="(item, index) in abnormalItems" :key="index" class="alert-item" :class="item.stocktakeStatus">
              <div class="alert-icon">{{ getAlertIcon(item.stocktakeStatus) }}</div>
              <div class="alert-content">
                <div class="alert-name">{{ item.goodsName }}</div>
                <div class="alert-location">{{ item.locationName }}</div>
              </div>
              <div class="alert-detail">
                <span :class="['diff-value', item.stocktakeStatus]">
                  {{ item.actualQuantity - item.platformQuantity > 0 ? '+' : '' }}
                  {{ item.actualQuantity - item.platformQuantity }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-alerts">
            <div class="empty-icon">✓</div>
            <p>系统运行正常</p>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部状态栏 -->
    <footer class="screen-footer">
      <div class="footer-left">
        <span class="status-dot active"></span>
        <span>SYSTEM MONITORING ACTIVE</span>
      </div>
      <div class="footer-center">
        <span>CYBER WMS v2.0 // INTELLIGENT LOGISTICS SYSTEM</span>
      </div>
      <div class="footer-right">
        <span>Last Update: {{ lastUpdateTime }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

let echarts = null

async function loadEcharts() {
  if (echarts) return echarts
  return new Promise((resolve, reject) => {
    if (window.echarts) {
      echarts = window.echarts
      resolve(echarts)
      return
    }
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js'
    script.onload = () => {
      echarts = window.echarts
      resolve(echarts)
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const currentTime = ref('')
const lastUpdateTime = ref('-')
const stocktakeData = ref([])
const latestRecords = ref([])
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null
let dataTimer = null
let timeTimer = null

const getParticleStyle = (index) => {
  const random = () => Math.random()
  return {
    left: `${random() * 100}%`,
    top: `${random() * 100}%`,
    animationDelay: `${random() * 8}s`,
    animationDuration: `${6 + random() * 8}s`,
    width: `${1 + random() * 2}px`,
    height: `${1 + random() * 2}px`
  }
}

const stocktakeStats = computed(() => {
  const data = stocktakeData.value
  return {
    total: data.length,
    normal: data.filter(item => item.stocktakeStatus === 'NORMAL').length,
    surplus: data.filter(item => item.stocktakeStatus === 'SURPLUS').length,
    deficit: data.filter(item => item.stocktakeStatus === 'DEFICIT').length
  }
})

const normalPercentage = computed(() => {
  if (stocktakeStats.value.total === 0) return 0
  return Math.round((stocktakeStats.value.normal / stocktakeStats.value.total) * 100)
})

const surplusPercentage = computed(() => {
  if (stocktakeStats.value.total === 0) return 0
  return Math.round((stocktakeStats.value.surplus / stocktakeStats.value.total) * 100)
})

const deficitPercentage = computed(() => {
  if (stocktakeStats.value.total === 0) return 0
  return Math.round((stocktakeStats.value.deficit / stocktakeStats.value.total) * 100)
})

const abnormalItems = computed(() => {
  return stocktakeData.value.filter(
    item => item.stocktakeStatus === 'SURPLUS' || item.stocktakeStatus === 'DEFICIT'
  ).slice(0, 5)
})

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${y}-${m}-${d} ${h}:${min}:${s}`
}

function formatDateTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const date = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${date} ${h}:${min}:${s}`
}

function getStatusText(status) {
  const statusMap = { 'NORMAL': '正常', 'SURPLUS': '盘盈', 'DEFICIT': '盘亏' }
  return statusMap[status] || '未知'
}

function getStatusClass(status) {
  const classMap = { 'NORMAL': 'normal', 'SURPLUS': 'surplus', 'DEFICIT': 'deficit' }
  return classMap[status] || ''
}

function getAlertIcon(status) {
  return status === 'SURPLUS' ? '▲' : '▼'
}

function tableRowClassName({ row }) {
  if (row.hasDifference) {
    return 'warning-row'
  }
  return ''
}

async function fetchStocktakeData() {
    try {
        const res = await request.get('/inventory/comparison')
        if (res.code === 200 && res.data) {
            stocktakeData.value = res.data.map(item => ({
                ...item,
                hasDifference: item.stocktakeStatus !== 'NORMAL'
            }))
            
            latestRecords.value = [...stocktakeData.value]
                .sort((a, b) => new Date(b.stocktakeTime) - new Date(a.stocktakeTime))
                .slice(0, 8)
            
            lastUpdateTime.value = formatDateTime(new Date())
            
            updateCharts()
        }
    } catch (e) {
        console.error('获取盘点数据失败:', e)
    }
}

function initTrendChart() {
  if (!trendChartRef.value || !echarts) return
  trendChart = echarts.init(trendChartRef.value)
  
  const hours = Array.from({ length: 12 }, (_, i) => `${8 + i}:00`)
  const option = {
    grid: { top: 40, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#00f0ff33' } },
      axisLabel: { color: '#8ba4b8', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#00f0ff15', type: 'dashed' } },
      axisLabel: { color: '#8ba4b8', fontSize: 10 }
    },
    series: [
      {
        name: '盘点数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#00f0ff', width: 2, shadowColor: '#00f0ff', shadowBlur: 10 },
        itemStyle: { color: '#00f0ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,240,255,0.35)' },
            { offset: 1, color: 'rgba(0,240,255,0)' }
          ])
        },
        data: [15, 28, 45, 62, 78, 95, 112, 98, 82, 65, 42, 25]
      }
    ]
  }
  trendChart.setOption(option)
}

function initPieChart() {
  if (!pieChartRef.value || !echarts) return
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '55%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#0a0e27',
        borderWidth: 2,
        shadowColor: 'currentColor',
        shadowBlur: 10
      },
      label: {
        show: true,
        position: 'outside',
        color: '#8ba4b8',
        fontSize: 11
      },
      labelLine: { lineStyle: { color: '#00f0ff33' } },
      data: [
        { value: stocktakeStats.value.normal, name: '正常', itemStyle: { color: '#00ff88' } },
        { value: stocktakeStats.value.surplus, name: '盘盈', itemStyle: { color: '#ffff00' } },
        { value: stocktakeStats.value.deficit, name: '盘亏', itemStyle: { color: '#ff3366' } }
      ]
    }]
  }
  pieChart.setOption(option)
}

function updateCharts() {
  if (pieChart) {
    pieChart.setOption({
      series: [{
        data: [
          { value: stocktakeStats.value.normal, name: '正常', itemStyle: { color: '#00ff88' } },
          { value: stocktakeStats.value.surplus, name: '盘盈', itemStyle: { color: '#ffff00' } },
          { value: stocktakeStats.value.deficit, name: '盘亏', itemStyle: { color: '#ff3366' } }
        ]
      }]
    })
  }
}

function goBack() {
  window.close()
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  await loadEcharts()
  
  initTrendChart()
  initPieChart()
  
  window.addEventListener('resize', handleResize)
  
  await fetchStocktakeData()
  dataTimer = setInterval(fetchStocktakeData, 5000)
})

onUnmounted(() => {
  clearInterval(timeTimer)
  clearInterval(dataTimer)
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard-screen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #0d1529 50%, #0a0e27 100%);
  color: #e0f7ff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Orbitron', 'Rajdhani', 'Microsoft YaHei', sans-serif;
  position: relative;
}

.bg-particles {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  background: var(--cyber-primary);
  border-radius: 50%;
  box-shadow: 0 0 6px var(--cyber-primary), 0 0 12px rgba(0, 240, 255, 0.3);
  opacity: 0.4;
  animation: particle-float infinite ease-in-out;
}

@keyframes particle-float {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.4; }
  25% { transform: translate(15px, -20px) scale(1.2); opacity: 0.7; }
  50% { transform: translate(-8px, -40px) scale(0.8); opacity: 0.3; }
  75% { transform: translate(-18px, -15px) scale(1.1); opacity: 0.6; }
}

.grid-bg {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image:
    linear-gradient(rgba(0, 240, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.02) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
  z-index: 0;
  opacity: 0.5;
}

.scan-line {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent);
  box-shadow: 0 0 20px var(--cyber-primary), 0 0 40px rgba(0, 240, 255, 0.3);
  animation: scan-move 6s linear infinite;
  pointer-events: none;
  z-index: 1;
  opacity: 0.6;
}

@keyframes scan-move {
  0% { transform: translateY(-10px); }
  100% { transform: translateY(100vh); }
}

.screen-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.08) 0%, transparent 100%);
  border-bottom: 2px solid var(--cyber-border);
  position: relative;
  z-index: 10;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5);
}

.header-glow {
  position: absolute;
  bottom: -2px;
  width: 300px;
  height: 2px;
  background: var(--cyber-primary);
  filter: blur(2px);
}

.header-glow.left { left: 10%; animation: glow-left 4s ease-in-out infinite; }
.header-glow.right { right: 10%; animation: glow-right 4s ease-in-out infinite; }

@keyframes glow-left {
  0%, 100% { opacity: 0.3; width: 200px; }
  50% { opacity: 0.8; width: 350px; }
}
@keyframes glow-right {
  0%, 100% { opacity: 0.3; width: 200px; }
  50% { opacity: 0.8; width: 350px; }
}

.header-line-animated {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 500px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent);
  animation: header-line-flow 3s ease-in-out infinite;
}

@keyframes header-line-flow {
  0%, 100% { opacity: 0.5; width: 400px; }
  50% { opacity: 1; width: 600px; }
}

.header-left, .header-right { width: 220px; }

.back-btn {
  cursor: pointer;
  color: var(--cyber-text-dim);
  font-size: 12px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
  border: 1px solid var(--cyber-border);
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 500;
}

.back-btn:hover {
  color: var(--cyber-primary);
  background: rgba(0, 240, 255, 0.1);
  border-color: var(--cyber-primary);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
}

.btn-icon { font-size: 14px; }

.title {
  font-family: 'Orbitron', sans-serif;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #00f0ff 0%, #ffffff 50%, #00f0ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% auto;
  animation: title-shine 4s linear infinite;
  text-shadow: none;
  filter: drop-shadow(0 0 20px rgba(0, 240, 255, 0.4));
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 12px;
}

@keyframes title-shine {
  to { background-position: 200% center; }
}

.title-deco {
  font-size: 18px;
  color: var(--cyber-primary);
  -webkit-text-fill-color: var(--cyber-primary);
  animation: deco-spin 8s linear infinite;
  display: inline-block;
}

.title-deco.right { animation-direction: reverse; }

@keyframes deco-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.datetime-box {
  text-align: right;
  font-family: 'Orbitron', monospace;
}

.time {
  font-size: 22px;
  font-weight: 700;
  color: var(--cyber-primary);
  text-shadow: 0 0 15px rgba(0, 240, 255, 0.6);
  letter-spacing: 2px;
}

.date {
  font-size: 11px;
  color: var(--cyber-text-dim);
  letter-spacing: 1px;
  margin-top: 2px;
}

.screen-body {
  flex: 1;
  display: flex;
  gap: 15px;
  padding: 15px;
  overflow: hidden;
  position: relative;
  z-index: 2;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 15px;
  overflow: hidden;
}

.panel-left, .panel-right {
  width: 340px;
  flex-shrink: 0;
}

.panel-center {
  flex: 1;
  min-width: 0;
}

.card {
  background: rgba(10, 20, 40, 0.85);
  border: 1px solid var(--cyber-border);
  border-radius: 10px;
  padding: 18px;
  position: relative;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
  overflow: hidden;
  transition: all 0.3s ease;
}

.card:hover {
  border-color: rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 25px rgba(0, 240, 255, 0.1);
}

.corner-tl, .corner-tr, .corner-bl, .corner-br {
  position: absolute;
  width: 15px;
  height: 15px;
  pointer-events: none;
}

.corner-tl { top: -1px; left: -1px; border-top: 2px solid var(--cyber-primary); border-left: 2px solid var(--cyber-primary); border-radius: 10px 0 0 0; }
.corner-tr { top: -1px; right: -1px; border-top: 2px solid var(--cyber-primary); border-right: 2px solid var(--cyber-primary); border-radius: 0 10px 0 0; }
.corner-bl { bottom: -1px; left: -1px; border-bottom: 2px solid var(--cyber-primary); border-left: 2px solid var(--cyber-primary); border-radius: 0 0 0 10px; }
.corner-br { bottom: -1px; right: -1px; border-bottom: 2px solid var(--cyber-primary); border-right: 2px solid var(--cyber-primary); border-radius: 0 0 10px 0; }

.card-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 14px;
  color: var(--cyber-primary);
  margin-bottom: 15px;
  padding-left: 12px;
  border-left: 3px solid var(--cyber-primary);
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-transform: uppercase;
  font-weight: 700;
  position: relative;
}

.title-icon {
  font-size: 16px;
  filter: drop-shadow(0 0 6px currentColor);
}

.live-badge {
  margin-left: auto;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 2px;
  padding: 2px 8px;
  background: var(--cyber-accent);
  color: var(--cyber-bg-dark);
  border-radius: 3px;
  animation: live-blink 1.5s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.5);
}

@keyframes live-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 统计卡片 */
.stats-card { flex-shrink: 0; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 15px;
}

.stat-item {
  background: rgba(0, 240, 255, 0.03);
  border-radius: 8px;
  padding: 18px 12px;
  text-align: center;
  border: 1px solid var(--cyber-border);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 240, 255, 0.15);
  border-color: rgba(0, 240, 255, 0.4);
}

.stat-glow {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  animation: stat-glow-flow 3s ease-in-out infinite;
}

.stat-item.total .stat-glow { background: linear-gradient(90deg, transparent, var(--cyber-primary), transparent); }
.stat-item.normal .stat-glow { background: linear-gradient(90deg, transparent, var(--cyber-accent), transparent); }
.stat-item.surplus .stat-glow { background: linear-gradient(90deg, transparent, var(--cyber-warning), transparent); }
.stat-item.deficit .stat-glow { background: linear-gradient(90deg, transparent, var(--cyber-danger), transparent); }

@keyframes stat-glow-flow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.stat-value {
  font-family: 'Orbitron', monospace;
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 6px;
  position: relative;
  z-index: 2;
}

.stat-item.total .stat-value { color: var(--cyber-primary); text-shadow: 0 0 20px rgba(0, 240, 255, 0.5); }
.stat-item.normal .stat-value { color: var(--cyber-accent); text-shadow: 0 0 20px rgba(0, 255, 136, 0.5); }
.stat-item.surplus .stat-value { color: var(--cyber-warning); text-shadow: 0 0 20px rgba(255, 255, 0, 0.5); }
.stat-item.deficit .stat-value { color: var(--cyber-danger); text-shadow: 0 0 20px rgba(255, 51, 102, 0.5); }

.stat-label {
  font-size: 11px;
  color: var(--cyber-text-dim);
  letter-spacing: 2px;
  text-transform: uppercase;
  font-weight: 500;
  position: relative;
  z-index: 2;
}

.stat-icon-bg {
  position: absolute;
  bottom: -5px;
  right: -5px;
  font-size: 48px;
  opacity: 0.04;
  font-weight: bold;
}

.stats-percentage { margin-top: 5px; }

.percentage-bar {
  height: 10px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 5px;
  position: relative;
  overflow: hidden;
  margin-bottom: 10px;
  border: 1px solid rgba(0, 240, 255, 0.1);
}

.percentage-fill {
  position: absolute;
  height: 100%;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.normal-fill { background: linear-gradient(90deg, #00ff88, #00cc66); box-shadow: 0 0 10px rgba(0, 255, 136, 0.4); }
.surplus-fill { background: linear-gradient(90deg, #ffff00, #ffcc00); box-shadow: 0 0 10px rgba(255, 255, 0, 0.4); }
.deficit-fill { background: linear-gradient(90deg, #ff3366, #cc2952); box-shadow: 0 0 10px rgba(255, 51, 102, 0.4); }

.percentage-legend {
  display: flex;
  justify-content: center;
  gap: 18px;
  font-size: 10px;
}

.legend-item {
  padding-left: 14px;
  position: relative;
  color: var(--cyber-text-dim);
  letter-spacing: 1px;
  font-weight: 500;
}

.legend-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: 0 0 6px currentColor;
}

.normal-legend::before { background: var(--cyber-accent); color: var(--cyber-accent); }
.surplus-legend::before { background: var(--cyber-warning); color: var(--cyber-warning); }
.deficit-legend::before { background: var(--cyber-danger); color: var(--cyber-danger); }

/* 记录卡片 */
.records-card { flex: 1; min-height: 0; }

.records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  flex: 1;
  max-height: 280px;
}

.record-item {
  background: rgba(0, 240, 255, 0.02);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: 6px;
  padding: 10px 12px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
}

.record-item:hover {
  border-color: rgba(0, 240, 255, 0.3);
  background: rgba(0, 240, 255, 0.06);
  transform: translateX(3px);
}

.record-latest {
  border-left: 3px solid var(--cyber-primary);
  background: rgba(0, 240, 255, 0.06);
  animation: pulse-glow 2s infinite;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 5px rgba(0, 240, 255, 0.3); }
  50% { box-shadow: 0 0 20px rgba(0, 240, 255, 0.5); }
}

.record-index {
  font-family: 'Orbitron', monospace;
  font-size: 11px;
  font-weight: 700;
  color: var(--cyber-primary);
  opacity: 0.5;
  min-width: 20px;
}

.record-main { flex: 1; min-width: 0; }

.record-name {
  font-size: 13px;
  color: var(--cyber-text);
  font-weight: 500;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-location { font-size: 11px; color: var(--cyber-text-dim); }

.record-compare {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.compare-item { display: flex; flex-direction: column; align-items: center; }

.compare-label { font-size: 9px; color: var(--cyber-text-dim); margin-bottom: 2px; letter-spacing: 1px; }

.compare-value {
  font-family: 'Orbitron', monospace;
  font-size: 14px;
  font-weight: 700;
}

.compare-value.platform { color: var(--cyber-primary); }
.compare-value.normal { color: var(--cyber-accent); }
.compare-value.surplus { color: var(--cyber-warning); }
.compare-value.deficit { color: var(--cyber-danger); }

.compare-arrow { color: var(--cyber-text-dim); font-size: 14px; }

.record-status { flex-shrink: 0; }

.status-badge {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.status-badge.NORMAL { background: rgba(0, 255, 136, 0.15); color: var(--cyber-accent); border: 1px solid rgba(0, 255, 136, 0.3); }
.status-badge.SURPLUS { background: rgba(255, 255, 0, 0.15); color: var(--cyber-warning); border: 1px solid rgba(255, 255, 0, 0.3); }
.status-badge.DEFICIT { background: rgba(255, 51, 102, 0.15); color: var(--cyber-danger); border: 1px solid rgba(255, 51, 102, 0.3); }

.empty-records, .empty-alerts {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: var(--cyber-text-dim);
  padding: 30px;
}

.empty-icon { font-size: 36px; opacity: 0.4; margin-bottom: 10px; }

/* 设备卡片 */
.device-card { flex-shrink: 0; }

.device-list { display: flex; flex-direction: column; gap: 8px; }

.device-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(0, 240, 255, 0.02);
  border: 1px solid rgba(0, 240, 255, 0.08);
  border-radius: 6px;
  transition: all 0.3s;
}

.device-item:hover { background: rgba(0, 240, 255, 0.06); border-color: rgba(0, 240, 255, 0.2); }

.device-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}

.dot-inner {
  position: absolute;
  top: 2px; left: 2px;
  width: 6px; height: 6px;
  border-radius: 50%;
  background: white;
}

.device-item.online .device-dot {
  background: var(--cyber-accent);
  box-shadow: 0 0 12px var(--cyber-accent), 0 0 24px rgba(0, 255, 136, 0.3);
  animation: dot-pulse 2s infinite;
}

.device-item.standby .device-dot {
  background: #e6a23c;
  box-shadow: 0 0 8px #e6a23c;
}

@keyframes dot-pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 12px var(--cyber-accent), 0 0 24px rgba(0, 255, 136, 0.3); }
  50% { opacity: 0.6; box-shadow: 0 0 8px var(--cyber-accent), 0 0 16px rgba(0, 255, 136, 0.2); }
}

.device-name {
  flex: 1;
  font-size: 12px;
  color: var(--cyber-text);
  font-family: 'Rajdhani', sans-serif;
  font-weight: 500;
}

.device-status {
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 3px;
  font-weight: 700;
  letter-spacing: 1px;
}

.device-item.online .device-status { background: rgba(0, 255, 136, 0.15); color: var(--cyber-accent); border: 1px solid rgba(0, 255, 136, 0.3); }
.device-item.standby .device-status { background: rgba(230, 162, 60, 0.15); color: #e6a23c; border: 1px solid rgba(230, 162, 60, 0.3); }

/* 主表格 */
.main-table-card { flex: 1; min-height: 0; display: flex; flex-direction: column; }

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-header .card-title { margin-bottom: 0; }

.table-stats { font-size: 11px; color: var(--cyber-text-dim); }

.update-time {
  background: rgba(0, 240, 255, 0.06);
  padding: 5px 12px;
  border-radius: 4px;
  border: 1px solid rgba(0, 240, 255, 0.15);
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: 'Rajdhani', sans-serif;
  letter-spacing: 1px;
}

.update-icon { animation: rotate-slow 4s linear infinite; }

@keyframes rotate-slow { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.table-container { flex: 1; min-height: 0; overflow: hidden; }

.stocktake-table { background: transparent !important; --el-table-border-color: rgba(0, 240, 255, 0.12) !important; }

:deep(.stocktake-table .el-table__inner-wrapper::before) { display: none; }
:deep(.stocktake-table .el-table__header-wrapper) { background: rgba(0, 240, 255, 0.04); }

:deep(.stocktake-table th.el-table__cell) {
  background: rgba(0, 240, 255, 0.06) !important;
  color: var(--cyber-primary) !important;
  border-color: rgba(0, 240, 255, 0.12) !important;
  font-weight: 700 !important;
  letter-spacing: 2px !important;
  font-size: 12px !important;
  text-transform: uppercase !important;
  font-family: 'Rajdhani', sans-serif !important;
}

:deep(.stocktake-table td.el-table__cell) {
  border-color: rgba(0, 240, 255, 0.06) !important;
  background: transparent !important;
  color: var(--cyber-text) !important;
  font-size: 13px !important;
}

:deep(.stocktake-table tr) { background: transparent !important; }
:deep(.stocktake-table tr:hover td.el-table__cell) { background: rgba(0, 240, 255, 0.05) !important; }
:deep(.stocktake-table .el-table__row--striped td.el-table__cell) { background: rgba(0, 0, 0, 0.15) !important; }

:deep(.stocktake-table .warning-row td.el-table__cell) {
  background: rgba(255, 51, 102, 0.08) !important;
  animation: warning-flash 2s ease-in-out infinite;
}

@keyframes warning-flash {
  0%, 100% { background: rgba(255, 51, 102, 0.08) !important; }
  50% { background: rgba(255, 51, 102, 0.15) !important; }
}

.goods-cell { display: flex; align-items: center; gap: 8px; }
.goods-icon { color: var(--cyber-primary); font-size: 12px; }

.platform-quantity {
  color: var(--cyber-primary);
  font-weight: 700;
  font-family: 'Orbitron', monospace;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}

.actual-quantity { font-weight: 700; font-family: 'Orbitron', monospace; }
.actual-quantity.NORMAL { color: var(--cyber-accent); }
.actual-quantity.SURPLUS { color: var(--cyber-warning); }
.actual-quantity.DEFICIT { color: var(--cyber-danger); }

.diff-quantity { font-weight: 700; font-family: 'Orbitron', monospace; }
.diff-quantity.NORMAL { color: var(--cyber-accent); }
.diff-quantity.SURPLUS { color: var(--cyber-warning); }
.diff-quantity.DEFICIT { color: var(--cyber-danger); }

.device-cell { color: var(--cyber-text-dim); font-size: 12px; }
.time-cell { color: var(--cyber-text-dim); font-size: 12px; font-family: 'Rajdhani', sans-serif; }

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.status-tag.NORMAL { background: rgba(0, 255, 136, 0.12); color: var(--cyber-accent); border: 1px solid rgba(0, 255, 136, 0.25); }
.status-tag.SURPLUS { background: rgba(255, 255, 0, 0.12); color: var(--cyber-warning); border: 1px solid rgba(255, 255, 0, 0.25); }
.status-tag.DEFICIT { background: rgba(255, 51, 102, 0.12); color: var(--cyber-danger); border: 1px solid rgba(255, 51, 102, 0.25); }

/* 图表卡片 */
.trend-card { height: 230px; flex-shrink: 0; }
.trend-chart { height: 170px; }
.pie-card { height: 250px; flex-shrink: 0; }
.pie-chart { height: 190px; }

.alert-card { flex: 1; min-height: 0; }
.alert-title .alert-count {
  margin-left: auto;
  background: var(--cyber-danger);
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 700;
  animation: count-pulse 1s ease-in-out infinite;
}

@keyframes count-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.alert-list { display: flex; flex-direction: column; gap: 8px; overflow-y: auto; flex: 1; }

.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(255, 51, 102, 0.04);
  border: 1px solid rgba(255, 51, 102, 0.12);
  border-radius: 6px;
  transition: all 0.3s;
  animation: alert-slidein 0.3s ease-out;
}

@keyframes alert-slidein {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.alert-item:hover { background: rgba(255, 51, 102, 0.08); border-color: rgba(255, 51, 102, 0.25); }
.alert-item.SURPLUS { background: rgba(255, 255, 0, 0.04); border-color: rgba(255, 255, 0, 0.12); }
.alert-item.SURPLUS:hover { background: rgba(255, 255, 0, 0.08); border-color: rgba(255, 255, 0, 0.25); }

.alert-icon { font-size: 18px; flex-shrink: 0; }
.alert-item.SURPLUS .alert-icon { color: var(--cyber-warning); }
.alert-item.DEFICIT .alert-icon { color: var(--cyber-danger); }

.alert-content { flex: 1; min-width: 0; }
.alert-name { font-size: 13px; color: var(--cyber-text); font-weight: 500; margin-bottom: 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.alert-location { font-size: 11px; color: var(--cyber-text-dim); }

.alert-detail { flex-shrink: 0; }
.diff-value { font-size: 18px; font-weight: 800; font-family: 'Orbitron', monospace; }
.diff-value.SURPLUS { color: var(--cyber-warning); text-shadow: 0 0 10px rgba(255, 255, 0, 0.4); }
.diff-value.DEFICIT { color: var(--cyber-danger); text-shadow: 0 0 10px rgba(255, 51, 102, 0.4); }

/* 底部状态栏 */
.screen-footer {
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: rgba(0, 0, 0, 0.4);
  color: var(--cyber-text-dim);
  font-size: 11px;
  border-top: 1px solid var(--cyber-border);
  position: relative;
  z-index: 10;
  letter-spacing: 1px;
  font-family: 'Rajdhani', sans-serif;
  font-weight: 500;
}

.footer-left { display: flex; align-items: center; gap: 8px; text-transform: uppercase; }

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--cyber-text-dim);
}

.status-dot.active {
  background: var(--cyber-accent);
  box-shadow: 0 0 10px var(--cyber-accent), 0 0 20px rgba(0, 255, 136, 0.3);
  animation: dot-pulse 2s infinite;
}

.footer-center { letter-spacing: 3px; text-transform: uppercase; opacity: 0.7; }

::-webkit-scrollbar { width: 4px; height: 4px; }
::-webkit-scrollbar-track { background: rgba(0, 240, 255, 0.02); border-radius: 2px; }
::-webkit-scrollbar-thumb { background: linear-gradient(180deg, var(--cyber-primary), var(--cyber-secondary)); border-radius: 2px; }
::-webkit-scrollbar-thumb:hover { background: linear-gradient(180deg, #33f1ff, #ff33ff); }
</style>
