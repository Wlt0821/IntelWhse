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
                                <div class="stat-label">总盘点项</div>
                                <div class="stat-value">{{ totalItems }}</div>
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-card normal">
                            <div class="stat-icon">
                                <el-icon><SuccessFilled /></el-icon>
                            </div>
                            <div class="stat-content">
                                <div class="stat-label">正常项</div>
                                <div class="stat-value">{{ normalItems }}</div>
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-card over">
                            <div class="stat-icon">
                                <el-icon><WarningFilled /></el-icon>
                            </div>
                            <div class="stat-content">
                                <div class="stat-label">盘盈项</div>
                                <div class="stat-value">{{ overItems }}</div>
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-card short">
                            <div class="stat-icon">
                                <el-icon><CircleCloseFilled /></el-icon>
                            </div>
                            <div class="stat-content">
                                <div class="stat-label">盘亏项</div>
                                <div class="stat-value">{{ shortItems }}</div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </div>

            <div class="status-info" v-if="lastUpdateTime">
                <el-alert
                    :title="'最后更新时间：' + lastUpdateTime + ' | 盘点设备：' + (currentDeviceName || '暂无')"
                    type="info"
                    :closable="false"
                    show-icon
                />
            </div>

            <el-table
                :data="tableData"
                border
                style="width: 100%"
                :row-class-name="tableRowClassName"
            >
                <el-table-column prop="goodsName" label="商品名称" min-width="180" />
                <el-table-column prop="locationName" label="仓位名称" min-width="180" />
                <el-table-column prop="platformQuantity" label="平台库存数据" width="150" align="right">
                    <template #default="{ row }">
                        <span class="platform-quantity">{{ row.platformQuantity }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="actualQuantity" label="实际盘点数据" width="150" align="right">
                    <template #default="{ row }">
                        <span :class="getActualQuantityClass(row)">{{ row.actualQuantity }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="stocktakeStatus" label="盘点状态" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="getStatusType(row.stocktakeStatus)">
                            {{ getStatusText(row.stocktakeStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="deviceName" label="盘点设备" min-width="180" />
                <el-table-column prop="stocktakeTime" label="最新盘点时间" width="180">
                    <template #default="{ row }">
                        {{ formatDateTime(row.stocktakeTime) }}
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination
                    v-model:current-page="pageNum"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Refresh, Box, SuccessFilled, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const tableData = ref([])
const autoRefresh = ref(false)
const refreshTimer = ref(null)
const lastUpdateTime = ref('')
const currentDeviceName = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allData = ref([])

const totalItems = computed(() => allData.value.length)
const normalItems = computed(() => allData.value.filter(item => item.stocktakeStatus === 'NORMAL').length)
const overItems = computed(() => allData.value.filter(item => item.stocktakeStatus === 'SURPLUS').length)
const shortItems = computed(() => allData.value.filter(item => item.stocktakeStatus === 'DEFICIT').length)

const tableRowClassName = ({ row }) => {
    if (row.hasMismatch) {
        return 'mismatch-row'
    }
    return ''
}

const getStatusType = (status) => {
    if (status === 'SURPLUS') return 'warning'
    if (status === 'DEFICIT') return 'danger'
    return 'success'
}

const getStatusText = (status) => {
    if (status === 'SURPLUS') return '盘盈'
    if (status === 'DEFICIT') return '盘亏'
    return '正常'
}

const getActualQuantityClass = (row) => {
    if (row.stocktakeStatus === 'SURPLUS') return 'over-quantity'
    if (row.stocktakeStatus === 'DEFICIT') return 'short-quantity'
    return 'normal-quantity'
}

const loadData = async () => {
    try {
        const [allRes, pageRes] = await Promise.all([
            request.get('/inventory/comparison'),
            request.get('/inventory/comparison/page', {
                params: {
                    pageNum: pageNum.value,
                    pageSize: pageSize.value
                }
            })
        ])
        
        if (allRes.code === 200) {
            allData.value = allRes.data || []
            if (allData.value.length > 0) {
                const latest = allData.value.find(item => item.stocktakeTime) || allData.value[0]
                if (latest.stocktakeTime) {
                    lastUpdateTime.value = formatDateTime(latest.stocktakeTime)
                }
                if (latest.deviceName) {
                    currentDeviceName.value = latest.deviceName
                }
            }
        }
        
        if (pageRes.code === 200) {
            tableData.value = pageRes.data?.records || []
            total.value = pageRes.data?.total || 0
        }
    } catch (error) {
        console.error('加载数据失败', error)
    }
}

const handleSizeChange = (val) => {
    pageSize.value = val
    pageNum.value = 1
    loadData()
}

const handleCurrentChange = (val) => {
    pageNum.value = val
    loadData()
}

const formatDateTime = (date) => {
    if (!date) return '-'
    const d = new Date(date)
    return d.toLocaleString('zh-CN')
}

const handleAutoRefresh = () => {
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
    height: auto !important;
    min-height: auto !important;
    max-height: none !important;
    display: block !important;
    flex: none !important;
}

/* 强制覆盖所有父级容器的高度继承 */
.inventory-realtime-container .el-card,
.inventory-realtime-container > * {
    height: auto !important;
    min-height: auto !important;
    max-height: none !important;
}

.inventory-realtime-container :deep(.el-card) {
    height: auto !important;
}

.inventory-realtime-container :deep(.el-card__body) {
    height: auto !important;
    min-height: auto !important;
    max-height: none !important;
    display: block !important;
    overflow: visible !important;
}

/* 分页器紧贴表格，不被拉伸 */
.inventory-realtime-container :deep(.el-pagination) {
    height: auto !important;
}

.inventory-realtime-container :deep(.el-table) {
    height: auto !important;
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

.stat-card.normal {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    color: white;
}

.stat-card.over {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
}

.stat-card.short {
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
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

.status-info {
    margin-bottom: 20px;
}

.mismatch-row {
    background-color: #fee7e7 !important;
}

.mismatch-row:hover {
    background-color: #fdd1d1 !important;
}

.platform-quantity {
    font-weight: 500;
    color: #409eff;
}

.normal-quantity {
    font-weight: 500;
    color: #67c23a;
}

.over-quantity {
    font-weight: 600;
    color: #e6a23c;
}

.short-quantity {
    font-weight: 600;
    color: #f56c6c;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-bottom: 4px;
  height: auto !important;
  min-height: auto !important;
  max-height: none !important;
  flex-shrink: 0 !important;
}
</style>
