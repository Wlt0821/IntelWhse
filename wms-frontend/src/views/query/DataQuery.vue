<template>
  <div class="data-query-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据统计表</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="库存查询" name="inventory">
          <div class="search-bar">
            <el-form :inline="true" :model="inventorySearchForm">
              <el-form-item label="商品编码">
                <el-input v-model="inventorySearchForm.goodsCode" placeholder="请输入商品编码" clearable />
              </el-form-item>
              <el-form-item label="仓位编码">
                <el-input v-model="inventorySearchForm.locationCode" placeholder="请输入仓位编码" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadInventoryData">查询</el-button>
                <el-button @click="resetInventorySearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="inventoryTableData" border style="width: 100%">
            <el-table-column prop="goodsId" label="商品ID" width="100" />
            <el-table-column prop="goodsName" label="商品名称" width="180" />
            <el-table-column prop="locationId" label="仓位ID" width="100" />
            <el-table-column prop="quantity" label="库存数量" width="120" />
            <el-table-column prop="lockedQuantity" label="锁定数量" width="120" />
            <el-table-column prop="availableQuantity" label="可用数量" width="120" />
            <el-table-column prop="minWarningQuantity" label="最小预警" width="120" />
            <el-table-column prop="maxWarningQuantity" label="最大预警" width="120" />
            <el-table-column prop="warningStatus" label="预警状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.warningStatus === 1" type="warning">低于最小值</el-tag>
                <el-tag v-else-if="row.warningStatus === 2" type="danger">高于最大值</el-tag>
                <el-tag v-else type="success">正常</el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="inventorySearchForm.pageNum"
            v-model:page-size="inventorySearchForm.pageSize"
            :total="inventoryTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadInventoryData"
            @current-change="loadInventoryData"
            style="margin-top: 20px; justify-content: flex-end; display: flex;"
          />

          <div class="stats-tabs" style="margin-top: 30px;">
            <el-radio-group v-model="activeStatsTab" size="small">
              <el-radio-button label="inventory">商品库存量</el-radio-button>
              <el-radio-button label="inbound">商品入库</el-radio-button>
              <el-radio-button label="outbound">商品出库</el-radio-button>
            </el-radio-group>
          </div>

          <div class="chart-container">
            <div v-if="activeStatsTab === 'inventory'" class="inventory-chart">
              <div class="chart-placeholder">
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
            </div>

            <div v-if="activeStatsTab === 'inbound'" class="inbound-chart">
              <div class="chart-placeholder">
                <div class="custom-chart">
                  <div class="chart-title">入库计划状态统计</div>
                  <div class="pie-chart-wrapper">
                    <div class="pie-chart" :style="getInboundPieBackground()">
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
            </div>

            <div v-if="activeStatsTab === 'outbound'" class="outbound-chart">
              <div class="chart-placeholder">
                <div class="custom-chart">
                  <div class="chart-title">订单状态统计</div>
                  <div class="pie-chart-wrapper">
                    <div class="pie-chart" :style="getOrderPieBackground()">
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
          </div>
        </el-tab-pane>

        <el-tab-pane label="入库查询" name="inbound">
          <div class="search-bar">
            <el-form :inline="true" :model="inboundSearchForm">
              <el-form-item label="计划编号">
                <el-input v-model="inboundSearchForm.planNo" placeholder="请输入计划编号" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <div class="custom-select-wrapper">
                  <el-select 
                    v-model="inboundStatusValue" 
                    placeholder="请选择状态" 
                    clearable
                    style="min-width: 150px;"
                    @change="handleInboundStatusChange"
                    @clear="handleInboundStatusClear"
                  >
                    <el-option label="草稿" value="DRAFT" />
                    <el-option label="待审核" value="PENDING" />
                    <el-option label="已审核" value="APPROVED" />
                    <el-option label="处理中" value="PROCESSING" />
                    <el-option label="已完成" value="COMPLETED" />
                    <el-option label="已取消" value="CANCELLED" />
                  </el-select>
                  <div v-if="inboundStatusValue" class="custom-display">
                    {{ getInboundStatusLabel(inboundStatusValue) }}
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadInboundData">查询</el-button>
                <el-button @click="resetInboundSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="inboundTableData" border style="width: 100%">
            <el-table-column prop="planNo" label="计划编号" width="180" />
            <el-table-column prop="supplierId" label="供应商ID" width="120" />
            <el-table-column prop="planType" label="计划类型" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.planType">{{ row.planType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalQuantity" label="总数量" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ row.createTime ? formatDate(row.createTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="inboundSearchForm.pageNum"
            v-model:page-size="inboundSearchForm.pageSize"
            :total="inboundTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadInboundData"
            @current-change="loadInboundData"
            style="margin-top: 20px; justify-content: flex-end; display: flex;"
          />
        </el-tab-pane>

        <el-tab-pane label="出库查询" name="outbound">
          <div class="search-bar">
            <el-form :inline="true" :model="outboundSearchForm">
              <el-form-item label="任务编号">
                <el-input v-model="outboundSearchForm.taskNo" placeholder="请输入任务编号" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <div class="custom-select-wrapper">
                  <el-select 
                    v-model="outboundStatusValue" 
                    placeholder="请选择状态" 
                    clearable
                    style="min-width: 150px;"
                    @change="handleOutboundStatusChange"
                    @clear="handleOutboundStatusClear"
                  >
                    <el-option label="待拣货" value="PENDING" />
                    <el-option label="拣货中" value="PROCESSING" />
                    <el-option label="已完成" value="COMPLETED" />
                  </el-select>
                  <div v-if="outboundStatusValue" class="custom-display">
                    {{ getOutboundStatusLabel(outboundStatusValue) }}
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadOutboundData">查询</el-button>
                <el-button @click="resetOutboundSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="outboundTableData" border style="width: 100%">
            <el-table-column prop="taskNo" label="任务编号" width="180" />
            <el-table-column prop="planId" label="计划ID" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getPickingStatusType(row.status)">
                  {{ getPickingStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ row.createTime ? formatDate(row.createTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="outboundSearchForm.pageNum"
            v-model:page-size="outboundSearchForm.pageSize"
            :total="outboundTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadOutboundData"
            @current-change="loadOutboundData"
            style="margin-top: 20px; justify-content: flex-end; display: flex;"
          />
        </el-tab-pane>

        <el-tab-pane label="订单查询" name="order">
          <div class="search-bar">
            <el-form :inline="true" :model="orderSearchForm">
              <el-form-item label="订单编号">
                <el-input v-model="orderSearchForm.orderNo" placeholder="请输入订单编号" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <div class="custom-select-wrapper">
                  <el-select 
                    v-model="orderStatusValue" 
                    placeholder="请选择状态" 
                    clearable
                    style="min-width: 150px;"
                    @change="handleOrderStatusChange"
                    @clear="handleOrderStatusClear"
                  >
                    <el-option label="草稿" value="DRAFT" />
                    <el-option label="已确认" value="CONFIRMED" />
                    <el-option label="处理中" value="PROCESSING" />
                    <el-option label="已完成" value="COMPLETED" />
                    <el-option label="已取消" value="CANCELLED" />
                  </el-select>
                  <div v-if="orderStatusValue" class="custom-display">
                    {{ getOrderStatusLabel(orderStatusValue) }}
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadOrderData">查询</el-button>
                <el-button @click="resetOrderSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="orderTableData" border style="width: 100%">
            <el-table-column prop="orderNo" label="订单编号" width="180" />
            <el-table-column prop="customerId" label="客户ID" width="120" />
            <el-table-column prop="orderType" label="订单类型" width="120" />
            <el-table-column prop="totalQuantity" label="总数量" width="120" />
            <el-table-column prop="totalAmount" label="总金额" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ row.createTime ? formatDate(row.createTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="orderSearchForm.pageNum"
            v-model:page-size="orderSearchForm.pageSize"
            :total="orderTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadOrderData"
            @current-change="loadOrderData"
            style="margin-top: 20px; justify-content: flex-end; display: flex;"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'

const activeTab = ref('inventory')
const activeStatsTab = ref('inventory')

const inventorySearchForm = reactive({
  goodsCode: '',
  locationCode: '',
  pageNum: 1,
  pageSize: 10
})
const inventoryTableData = ref([])
const inventoryTotal = ref(0)

const inboundSearchForm = reactive({
  planNo: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
const inboundStatusValue = ref('')
const inboundTableData = ref([])
const inboundTotal = ref(0)

const outboundSearchForm = reactive({
  taskNo: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
const outboundStatusValue = ref('')
const outboundTableData = ref([])
const outboundTotal = ref(0)

const orderSearchForm = reactive({
  orderNo: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})
const orderStatusValue = ref('')
const orderTableData = ref([])
const orderTotal = ref(0)

const barColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00CED1', '#FF6347', '#9370DB']
const pieColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#00CED1']
const orderPieColors = ['#909399', '#409EFF', '#E6A23C', '#67C23A', '#F56C6C']

const inventoryChartData = ref([])
const inboundChartData = ref([])
const orderChartData = ref([])

const maxInventoryValue = computed(() => {
  if (inventoryChartData.value.length === 0) return 1
  return Math.max(...inventoryChartData.value.map(item => item.value))
})

const getInboundPieBackground = () => {
  if (inboundChartData.value.length === 0) {
    return { background: 'conic-gradient(#E5E7EB 0deg 360deg)' }
  }
  let currentDegree = 0
  const gradients = []
  inboundChartData.value.forEach((item, index) => {
    const endDegree = currentDegree + (item.percent * 3.6)
    gradients.push(`${pieColors[index]} ${currentDegree}deg ${endDegree}deg`)
    currentDegree = endDegree
  })
  return { background: `conic-gradient(${gradients.join(', ')})` }
}

const getOrderPieBackground = () => {
  if (orderChartData.value.length === 0) {
    return { background: 'conic-gradient(#E5E7EB 0deg 360deg)' }
  }
  let currentDegree = 0
  const gradients = []
  orderChartData.value.forEach((item, index) => {
    const endDegree = currentDegree + (item.percent * 3.6)
    gradients.push(`${orderPieColors[index]} ${currentDegree}deg ${endDegree}deg`)
    currentDegree = endDegree
  })
  return { background: `conic-gradient(${gradients.join(', ')})` }
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'primary',
    PROCESSING: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    PENDING: '待审核',
    APPROVED: '已审核',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const getInboundStatusLabel = (value) => {
  const labelMap = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'APPROVED': '已审核',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labelMap[value] || ''
}

const handleInboundStatusChange = (value) => {
  inboundSearchForm.status = value
}

const handleInboundStatusClear = () => {
  inboundSearchForm.status = ''
}

const getPickingStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    PROCESSING: '',
    COMPLETED: 'success'
  }
  return typeMap[status] || 'info'
}

const getPickingStatusText = (status) => {
  const textMap = {
    PENDING: '待拣货',
    PROCESSING: '拣货中',
    COMPLETED: '已完成'
  }
  return textMap[status] || status
}

const getOutboundStatusLabel = (value) => {
  const labelMap = {
    'PENDING': '待拣货',
    'PROCESSING': '拣货中',
    'COMPLETED': '已完成'
  }
  return labelMap[value] || ''
}

const handleOutboundStatusChange = (value) => {
  outboundSearchForm.status = value
}

const handleOutboundStatusClear = () => {
  outboundSearchForm.status = ''
}

const getOrderStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    CONFIRMED: 'primary',
    PROCESSING: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getOrderStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    CONFIRMED: '已确认',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const getOrderStatusLabel = (value) => {
  const labelMap = {
    'DRAFT': '草稿',
    'CONFIRMED': '已确认',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return labelMap[value] || ''
}

const handleOrderStatusChange = (value) => {
  orderSearchForm.status = value
}

const handleOrderStatusClear = () => {
  orderSearchForm.status = ''
}

const loadInventoryData = async () => {
  try {
    const res = await request.get('/home/inventory/page', {
      params: inventorySearchForm
    })
    inventoryTableData.value = res.data.records || []
    inventoryTotal.value = res.data.total || 0
  } catch (e) {
    console.error(e)
    inventoryTableData.value = []
    inventoryTotal.value = 0
  }
}

const resetInventorySearch = () => {
  inventorySearchForm.goodsCode = ''
  inventorySearchForm.locationCode = ''
  inventorySearchForm.pageNum = 1
  loadInventoryData()
}

const loadInboundData = async () => {
  try {
    const res = await request.get('/inbound/plan/page', {
      params: inboundSearchForm
    })
    inboundTableData.value = res.data.records || []
    inboundTotal.value = res.data.total || 0
  } catch (e) {
    console.error(e)
    inboundTableData.value = []
    inboundTotal.value = 0
  }
}

const resetInboundSearch = () => {
  inboundSearchForm.planNo = ''
  inboundSearchForm.status = ''
  inboundStatusValue.value = ''
  inboundSearchForm.pageNum = 1
  loadInboundData()
}

const loadOutboundData = async () => {
  try {
    const res = await request.get('/outbound/picking-task/page', {
      params: outboundSearchForm
    })
    outboundTableData.value = res.data.records || []
    outboundTotal.value = res.data.total || 0
  } catch (e) {
    console.error(e)
    outboundTableData.value = []
    outboundTotal.value = 0
  }
}

const resetOutboundSearch = () => {
  outboundSearchForm.taskNo = ''
  outboundSearchForm.status = ''
  outboundStatusValue.value = ''
  outboundSearchForm.pageNum = 1
  loadOutboundData()
}

const loadOrderData = async () => {
  try {
    const res = await request.get('/order/customer/page', {
      params: orderSearchForm
    })
    orderTableData.value = res.data.records || []
    orderTotal.value = res.data.total || 0
  } catch (e) {
    console.error(e)
    orderTableData.value = []
    orderTotal.value = 0
  }
}

const resetOrderSearch = () => {
  orderSearchForm.orderNo = ''
  orderSearchForm.status = ''
  orderStatusValue.value = ''
  orderSearchForm.pageNum = 1
  loadOrderData()
}

const loadInventoryChart = async () => {
  try {
    const res = await request.get('/home/inventory-chart')
    inventoryChartData.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadInboundChart = async () => {
  try {
    const res = await request.get('/home/inbound-chart')
    inboundChartData.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadOrderChart = async () => {
  try {
    const res = await request.get('/home/order-chart')
    orderChartData.value = res.data
  } catch (e) {
    console.error(e)
  }
}

watch(activeStatsTab, (newType) => {
  if (newType === 'inventory') {
    loadInventoryChart()
  } else if (newType === 'inbound') {
    loadInboundChart()
  } else if (newType === 'outbound') {
    loadOrderChart()
  }
})

onMounted(() => {
  loadInventoryData()
  loadInboundData()
  loadOutboundData()
  loadOrderData()
  loadInventoryChart()
})
</script>

<style scoped>
.data-query-container {
  padding: 0;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.search-bar {
  margin-bottom: 20px;
}

.stats-tabs {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.chart-container {
  min-height: 300px;
  padding: 20px 0;
}

.chart-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
}

.custom-chart {
  width: 100%;
  max-width: 800px;
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

.custom-select-wrapper {
  position: relative;
}

.custom-select-wrapper :deep(.el-select__wrapper) {
  color: transparent;
}

.custom-select-wrapper :deep(.el-select__selected-item) {
  color: transparent;
}

.custom-display {
  position: absolute;
  top: 0;
  left: 12px;
  right: 30px;
  height: 32px;
  line-height: 32px;
  color: #606266;
  font-size: 14px;
  pointer-events: none;
  z-index: 10;
}
</style>
