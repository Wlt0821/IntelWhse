<template>
  <div class="order-process-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单处理</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="订单编号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <div class="custom-select-wrapper">
              <el-select 
                v-model="statusValue" 
                placeholder="请选择状态" 
                clearable
                style="min-width: 150px;"
                @change="handleStatusChange"
                @clear="handleStatusClear"
              >
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已确认" value="CONFIRMED" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
              <div v-if="statusValue" class="custom-display">
                {{ getStatusLabel(statusValue) }}
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerId" label="客户ID" width="120" />
        <el-table-column prop="orderType" label="订单类型" width="120" />
        <el-table-column prop="totalQuantity" label="总数量" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleConfirm(row)" v-if="row.status === 'DRAFT'">确认</el-button>
            <el-button size="small" type="success" @click="handleProcess(row)" v-if="row.status === 'CONFIRMED'">开始处理</el-button>
            <el-button size="small" type="warning" @click="handleComplete(row)" v-if="row.status === 'PROCESSING'">完成</el-button>
            <el-button size="small" type="danger" @click="handleCancel(row)" v-if="['DRAFT', 'CONFIRMED'].includes(row.status)">取消</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  status: undefined
})

const statusValue = ref('')

const tableData = ref([])
const total = ref(0)

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    CONFIRMED: 'primary',
    PROCESSING: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    CONFIRMED: '已确认',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const getStatusLabel = (value) => {
  const labelMap = {
    DRAFT: '草稿',
    CONFIRMED: '已确认',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return labelMap[value] || value
}

const handleStatusChange = (value) => {
  searchForm.status = value
}

const handleStatusClear = () => {
  searchForm.status = undefined
}

const loadData = async () => {
  try {
    const res = await request.get('/order/customer/page', { params: searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = undefined
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const updateStatus = async (row, newStatus, actionName) => {
  try {
    await ElMessageBox.confirm(`确定要${actionName}该订单吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    row.status = newStatus
    await request.put('/order/customer', row)
    ElMessage.success(`${actionName}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleConfirm = (row) => {
  updateStatus(row, 'CONFIRMED', '确认')
}

const handleProcess = (row) => {
  updateStatus(row, 'PROCESSING', '开始处理')
}

const handleComplete = (row) => {
  updateStatus(row, 'COMPLETED', '完成')
}

const handleCancel = (row) => {
  updateStatus(row, 'CANCELLED', '取消')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-process-container {
  padding: 0;
}

.card-header {
  font-weight: bold;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
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
