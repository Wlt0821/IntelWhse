<template>
  <div class="packing-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>装箱单</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="装箱编号">
            <el-input v-model="searchForm.packingNo" placeholder="请输入装箱编号" clearable />
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
                <el-option label="已装箱" value="PACKED" />
                <el-option label="已出库" value="SHIPPED" />
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
        <el-button type="primary" @click="handleAdd">新增装箱单</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="packingNo" label="装箱编号" width="180" />
        <el-table-column prop="containerId" label="容器ID" width="120" />
        <el-table-column prop="planId" label="计划ID" width="120" />
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
        <el-form-item label="装箱编号" prop="packingNo">
          <el-input v-model="form.packingNo" placeholder="请输入装箱编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="容器ID" prop="containerId">
          <el-input-number v-model="form.containerId" :min="1" />
        </el-form-item>
        <el-form-item label="计划ID" prop="planId">
          <el-input-number v-model="form.planId" :min="1" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number v-model="form.totalQuantity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PACKED">已装箱</el-radio>
            <el-radio label="SHIPPED">已出库</el-radio>
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
  packingNo: '',
  status: undefined
})

const statusValue = ref('')

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const total = ref(0)

const form = reactive({
  id: null,
  packingNo: '',
  containerId: null,
  planId: null,
  totalQuantity: 0,
  status: 'DRAFT'
})

const rules = {
  packingNo: [{ required: true, message: '请输入装箱编号', trigger: 'blur' }]
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PACKED: 'primary',
    SHIPPED: 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    PACKED: '已装箱',
    SHIPPED: '已出库'
  }
  return textMap[status] || status
}

const getStatusLabel = (value) => {
  const labelMap = {
    DRAFT: '草稿',
    PACKED: '已装箱',
    SHIPPED: '已出库'
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
    const res = await request.get('/outbound/packing/page', { params: searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.packingNo = ''
  searchForm.status = undefined
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增装箱单'
  Object.assign(form, {
    id: null,
    packingNo: '',
    containerId: null,
    planId: null,
    totalQuantity: 0,
    status: 'DRAFT'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑装箱单'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await request.put('/outbound/packing', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/outbound/packing', form)
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
    await ElMessageBox.confirm('确定要删除该装箱单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/outbound/packing/${row.id}`)
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
.packing-list-container {
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
