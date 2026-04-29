<template>
  <div class="transfer-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>移库管理</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="计划编号">
            <el-input v-model="searchForm.planNo" placeholder="请输入计划编号" clearable />
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
                <el-option label="待审核" value="PENDING" />
                <el-option label="已审核" value="APPROVED" />
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
        <el-table-column prop="sourceLocationName" label="源仓位" width="180" />
        <el-table-column prop="targetLocationName" label="目标仓位" width="180" />
        <el-table-column prop="goodsName" label="商品" width="180" />
        <el-table-column prop="quantity" label="移库数量" width="120" />
        <el-table-column prop="reason" label="移库原因" />
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
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="请选择商品" style="width: 100%;">
            <el-option v-for="item in goodsList" :key="item.id" :label="item.goodsName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="源仓位" prop="sourceLocationId">
          <el-select v-model="form.sourceLocationId" placeholder="请选择源仓位" style="width: 100%;">
            <el-option v-for="item in locationList" :key="item.id" :label="item.locationName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标仓位" prop="targetLocationId">
          <el-select v-model="form.targetLocationId" placeholder="请选择目标仓位" style="width: 100%;">
            <el-option v-for="item in locationList" :key="item.id" :label="item.locationName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="移库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="移库原因" prop="reason">
          <el-input v-model="form.reason" placeholder="请输入移库原因" type="textarea" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PENDING">待审核</el-radio>
            <el-radio label="APPROVED">已审核</el-radio>
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

const statusValue = ref('')

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const total = ref(0)
const goodsList = ref([])
const locationList = ref([])

const form = reactive({
  id: null,
  planNo: '',
  sourceLocationId: null,
  targetLocationId: null,
  goodsId: null,
  quantity: 0,
  reason: '',
  status: 'DRAFT'
})

const rules = {
  planNo: [{ required: true, message: '请输入计划编号', trigger: 'blur' }]
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
    PROCESSING: '执行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const getStatusLabel = (value) => {
  const map = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'APPROVED': '已审核',
    'PROCESSING': '执行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[value] || ''
}

const loadGoods = async () => {
  try {
    const res = await request.get('/base/goods/all', { params: { status: 1 } })
    goodsList.value = res.data || []
  } catch (e) {
    console.error('加载商品失败', e)
    goodsList.value = []
  }
}

const loadLocations = async () => {
  try {
    const res = await request.get('/base/location/all', { params: { status: 1 } })
    locationList.value = res.data || []
  } catch (e) {
    console.error('加载仓位失败', e)
    locationList.value = []
  }
}

const loadData = async () => {
  try {
    const params = { ...searchForm }
    if (statusValue.value) {
      params.status = statusValue.value
    }
    const res = await request.get('/transfer/plan/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.planNo = ''
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增移库计划'
  Object.assign(form, {
    id: null,
    planNo: '',
    sourceLocationId: null,
    targetLocationId: null,
    goodsId: null,
    quantity: 0,
    reason: '',
    status: 'DRAFT'
  })
  loadGoods()
  loadLocations()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑移库计划'
  Object.assign(form, row)
  loadGoods()
  loadLocations()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await request.put('/transfer/plan', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/transfer/plan', form)
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
    await ElMessageBox.confirm('确定要删除该移库计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/transfer/plan/${row.id}`)
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
  loadGoods()
  loadLocations()
})
</script>

<style scoped>
.transfer-manage-container {
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
