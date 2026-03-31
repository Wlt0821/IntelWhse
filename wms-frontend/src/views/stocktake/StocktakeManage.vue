<template>
  <div class="stocktake-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点管理</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="计划编号">
            <el-input v-model="searchForm.planNo" placeholder="请输入计划编号" clearable />
          </el-form-item>
          <el-form-item label="盘点类型">
            <div class="custom-select-wrapper">
              <el-select 
                v-model="checkTypeValue" 
                placeholder="请选择盘点类型" 
                clearable
                style="min-width: 150px;"
              >
                <el-option label="全盘" value="FULL" />
                <el-option label="抽盘" value="SAMPLING" />
                <el-option label="循环盘点" value="CYCLE" />
              </el-select>
              <div v-if="checkTypeValue" class="custom-display">
                {{ getCheckTypeLabel(checkTypeValue) }}
              </div>
            </div>
          </el-form-item>
          <el-form-item label="状态">
            <div class="custom-select-wrapper">
              <el-select 
                v-model="statusValue" 
                placeholder="请选择状态" 
                clearable
                style="min-width: 150px;"
              >
                <el-option label="草稿" value="DRAFT" />
                <el-option label="待执行" value="PENDING" />
                <el-option label="执行中" value="PROCESSING" />
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
        <el-button type="primary" @click="handleAdd">新增计划</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="planNo" label="计划编号" width="180" />
        <el-table-column prop="checkType" label="盘点类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getCheckTypeText(row.checkType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="zoneId" label="库区ID" width="120" />
        <el-table-column prop="locationId" label="仓位ID" width="120" />
        <el-table-column prop="goodsId" label="商品ID" width="120" />
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="计划编号" prop="planNo">
          <el-input v-model="form.planNo" placeholder="请输入计划编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="盘点类型" prop="checkType">
          <el-select v-model="form.checkType" placeholder="请选择盘点类型">
            <el-option label="全盘" value="FULL" />
            <el-option label="抽盘" value="SAMPLING" />
            <el-option label="循环盘点" value="CYCLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="库区ID" prop="zoneId">
          <el-input-number v-model="form.zoneId" :min="1" />
        </el-form-item>
        <el-form-item label="仓位ID" prop="locationId">
          <el-input-number v-model="form.locationId" :min="1" />
        </el-form-item>
        <el-form-item label="商品ID" prop="goodsId">
          <el-input-number v-model="form.goodsId" :min="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PENDING">待执行</el-radio>
            <el-radio label="PROCESSING">执行中</el-radio>
            <el-radio label="COMPLETED">已完成</el-radio>
            <el-radio label="CANCELLED">已取消</el-radio>
          </el-radio-group>
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
  planNo: ''
})

const checkTypeValue = ref('')
const statusValue = ref('')

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const total = ref(0)

const form = reactive({
  id: null,
  planNo: '',
  checkType: '',
  zoneId: null,
  locationId: null,
  goodsId: null,
  status: 'DRAFT'
})

const rules = {
  planNo: [{ required: true, message: '请输入计划编号', trigger: 'blur' }],
  checkType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }]
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
    PROCESSING: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    PENDING: '待执行',
    PROCESSING: '执行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const getCheckTypeText = (checkType) => {
  const textMap = {
    FULL: '全盘',
    SAMPLING: '抽盘',
    CYCLE: '循环盘点'
  }
  return textMap[checkType] || checkType
}

const getCheckTypeLabel = (value) => {
  const map = {
    'FULL': '全盘',
    'SAMPLING': '抽盘',
    'CYCLE': '循环盘点'
  }
  return map[value] || ''
}

const getStatusLabel = (value) => {
  const map = {
    'DRAFT': '草稿',
    'PENDING': '待执行',
    'PROCESSING': '执行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[value] || ''
}

const loadData = async () => {
  try {
    const params = { ...searchForm }
    if (checkTypeValue.value) {
      params.checkType = checkTypeValue.value
    }
    if (statusValue.value) {
      params.status = statusValue.value
    }
    const res = await request.get('/stocktake/plan/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.planNo = ''
  checkTypeValue.value = ''
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增盘点计划'
  Object.assign(form, {
    id: null,
    planNo: '',
    checkType: '',
    zoneId: null,
    locationId: null,
    goodsId: null,
    status: 'DRAFT'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑盘点计划'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await request.put('/stocktake/plan', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/stocktake/plan', form)
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
    await ElMessageBox.confirm('确定要删除该盘点计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/stocktake/plan/${row.id}`)
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
.stocktake-manage-container {
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
