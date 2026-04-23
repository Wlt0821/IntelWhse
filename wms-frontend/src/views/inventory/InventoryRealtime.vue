<template>
  <div class="inventory-realtime-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><DataAnalysis /></el-icon>
            实时盘点数据
          </span>
          <el-button type="success" @click="handleAutoRefresh" :icon="Refresh">
            {{ autoRefresh ? '停止刷新' : '自动刷新' }}
          </el-button>
        </div>
      </template>

      <div class="stats-summary">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">总盘点次数</div>
                <div class="stat-value">{{ totalRecords }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card today">
              <div class="stat-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">今日盘点</div>
                <div class="stat-value">{{ todayRecords }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card boxes">
              <div class="stat-icon">
                <el-icon><Goods /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">识别货物总数</div>
                <div class="stat-value">{{ totalBoxes }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card qrs">
              <div class="stat-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">识别二维码数</div>
                <div class="stat-value">{{ totalQrs }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="批次号">
            <el-input v-model="searchForm.batchId" placeholder="请输入批次号" clearable />
          </el-form-item>
          <el-form-item label="设备名称">
            <el-input v-model="searchForm.deviceName" placeholder="请输入设备名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="batchId" label="批次号" width="180" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="timestamp" label="盘点时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalBoxes" label="识别货物数" width="120" align="right">
          <template #default="{ row }">
            <span class="number-highlight">{{ row.totalBoxes }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQrs" label="扫码数量" width="120" align="right">
          <template #default="{ row }">
            <span class="number-highlight">{{ row.totalQrs }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扫码率" width="120" align="right">
          <template #default="{ row }">
            <span :class="getScanRateClass(row)">
              {{ calculateScanRate(row) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="cargoSummary" label="货物汇总" min-width="200">
          <template #default="{ row }">
            <el-popover trigger="hover" placement="top">
              <template #reference>
                <span class="summary-preview">{{ truncateJson(row.cargoSummary) }}</span>
              </template>
              <pre class="json-display">{{ formatJson(row.cargoSummary) }}</pre>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end; display: flex;"
      />
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="盘点记录详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="批次号">{{ currentRecord.batchId }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentRecord.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="盘点时间">{{ formatDateTime(currentRecord.timestamp) }}</el-descriptions-item>
        <el-descriptions-item label="识别货物数">{{ currentRecord.totalBoxes }}</el-descriptions-item>
        <el-descriptions-item label="扫码数量">{{ currentRecord.totalQrs }}</el-descriptions-item>
        <el-descriptions-item label="扫码率">{{ calculateScanRate(currentRecord) }}%</el-descriptions-item>
        <el-descriptions-item label="货物汇总" :span="2">
          <pre class="json-display">{{ formatJson(currentRecord.cargoSummary) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="二维码详情" :span="2">
          <pre class="json-display">{{ formatJson(currentRecord.qrDetails) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, Refresh, Box, Calendar, Goods, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const autoRefresh = ref(false)
const refreshTimer = ref(null)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

const searchForm = ref({
  pageNum: 1,
  pageSize: 10,
  batchId: '',
  deviceName: ''
})

const totalRecords = computed(() => total.value)

const todayRecords = computed(() => {
  const today = new Date().toDateString()
  return tableData.value.filter(r => {
    const recordDate = new Date(r.timestamp).toDateString()
    return recordDate === today
  }).length
})

const totalBoxes = computed(() => {
  return tableData.value.reduce((sum, r) => sum + (r.totalBoxes || 0), 0)
})

const totalQrs = computed(() => {
  return tableData.value.reduce((sum, r) => sum + (r.totalQrs || 0), 0)
})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/inventory/page', { params: searchForm.value })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = {
    pageNum: 1,
    pageSize: 10,
    batchId: '',
    deviceName: ''
  }
  loadData()
}

function formatDateTime(date) {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

function formatJson(jsonStr) {
  if (!jsonStr) return '无数据'
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

function truncateJson(jsonStr) {
  if (!jsonStr) return '-'
  try {
    const str = typeof jsonStr === 'string' ? jsonStr : JSON.stringify(jsonStr)
    return str.length > 30 ? str.substring(0, 30) + '...' : str
  } catch {
    return '-'
  }
}

function calculateScanRate(row) {
  if (!row.totalBoxes || row.totalBoxes === 0) return 0
  return ((row.totalQrs / row.totalBoxes) * 100).toFixed(1)
}

function getScanRateClass(row) {
  const rate = parseFloat(calculateScanRate(row))
  if (rate >= 90) return 'scan-rate-high'
  if (rate >= 70) return 'scan-rate-medium'
  return 'scan-rate-low'
}

function viewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该盘点记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/inventory/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {
  }
}

function handleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    refreshTimer.value = setInterval(() => {
      loadData()
    }, 5000)
    ElMessage.success('已开启自动刷新（每5秒）')
  } else {
    if (refreshTimer.value) {
      clearInterval(refreshTimer.value)
      refreshTimer.value = null
    }
    ElMessage.info('已停止自动刷新')
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})
</script>

<style scoped>
.inventory-realtime-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.stats-summary {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.today {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card.boxes {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-card.qrs {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.number-highlight {
  font-weight: 600;
  color: #409eff;
}

.scan-rate-high {
  color: #67c23a;
  font-weight: 600;
}

.scan-rate-medium {
  color: #e6a23c;
  font-weight: 600;
}

.scan-rate-low {
  color: #f56c6c;
  font-weight: 600;
}

.summary-preview {
  color: #909399;
  cursor: pointer;
}

.json-display {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
