<template>
  <div class="inbound-task-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>入库作业</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="作业编号">
            <el-input v-model="searchForm.taskNo" placeholder="请输入作业编号" clearable />
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
                <el-option label="待执行" value="PENDING" />
                <el-option label="执行中" value="PROCESSING" />
                <el-option label="已完成" value="COMPLETED" />
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
        <el-button type="primary" @click="handleAdd">新增作业</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="taskNo" label="作业编号" width="180" />
        <el-table-column prop="planId" label="计划ID" width="120" />
        <el-table-column prop="taskType" label="作业类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.taskType">{{ row.taskType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorId" label="操作人ID" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ row.startTime ? formatDate(row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatDate(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createTime ? formatDate(row.createTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="作业编号" prop="taskNo">
          <el-input v-model="form.taskNo" placeholder="请输入作业编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="计划ID" prop="planId">
          <el-input-number v-model="form.planId" :min="1" />
        </el-form-item>
        <el-form-item label="作业类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="请选择作业类型">
            <el-option label="上架作业" value="PUTAWAY" />
            <el-option label="质检作业" value="QUALITY" />
            <el-option label="入库验收" value="CHECKIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="PENDING">待执行</el-radio>
            <el-radio label="PROCESSING">执行中</el-radio>
            <el-radio label="COMPLETED">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作人ID" prop="operatorId">
          <el-input-number v-model="form.operatorId" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  taskNo: '',
  status: ''
})

const statusValue = ref('')

const getStatusLabel = (value) => {
  const labelMap = {
    'PENDING': '待执行',
    'PROCESSING': '执行中',
    'COMPLETED': '已完成'
  }
  return labelMap[value] || ''
}

const handleStatusChange = (value) => {
  searchForm.status = value
}

const handleStatusClear = () => {
  searchForm.status = ''
}

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const total = ref(0)

const form = reactive({
  id: null,
  taskNo: '',
  planId: null,
  taskType: '',
  status: 'PENDING',
  operatorId: null,
  startTime: null,
  endTime: null
})

const rules = {
  taskNo: [{ required: true, message: '请输入作业编号', trigger: 'blur' }]
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    PROCESSING: '',
    COMPLETED: 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    PENDING: '待执行',
    PROCESSING: '执行中',
    COMPLETED: '已完成'
  }
  return textMap[status] || status
}

const loadData = async () => {
  try {
    const res = await request.get('/inbound/task/page', { params: searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.taskNo = ''
  searchForm.status = ''
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增入库作业'
  Object.assign(form, {
    id: null,
    taskNo: '',
    planId: null,
    taskType: '',
    status: 'PENDING',
    operatorId: null,
    startTime: null,
    endTime: null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑入库作业'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await request.put('/inbound/task', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/inbound/task', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该入库作业吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/inbound/task/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.inbound-task-container {
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
