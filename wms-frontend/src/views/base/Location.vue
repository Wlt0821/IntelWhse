<template>
  <div class="location-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>仓位设置</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="仓位名称">
            <el-input v-model="searchForm.locationName" placeholder="请输入仓位名称" clearable />
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
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
              <div v-if="statusValue !== ''" class="custom-display">
                {{ getStatusLabel(statusValue) }}
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="handleAdd">新增仓位</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="locationCode" label="仓位编码" width="120" />
        <el-table-column prop="locationName" label="仓位名称" width="180" />
        <el-table-column prop="locationType" label="仓位类型" width="120" />
        <el-table-column prop="locationLevel" label="层级" width="80" />
        <el-table-column prop="capacity" label="容量" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="仓位编码" prop="locationCode">
          <el-input v-model="form.locationCode" placeholder="请输入仓位编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="仓位名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="请输入仓位名称" />
        </el-form-item>
        <el-form-item label="所属库区" prop="zoneId">
          <el-select v-model="form.zoneId" placeholder="请选择所属库区">
          </el-select>
        </el-form-item>
        <el-form-item label="仓位类型" prop="locationType">
          <el-select v-model="form.locationType" placeholder="请选择仓位类型">
            <el-option label="普通仓位" value="NORMAL" />
            <el-option label="冷藏仓位" value="COLD" />
            <el-option label="恒温仓位" value="CONSTANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="层级" prop="locationLevel">
          <el-input-number v-model="form.locationLevel" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" type="textarea" />
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
  locationName: '',
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
  locationCode: '',
  locationName: '',
  zoneId: null,
  parentId: null,
  locationLevel: 1,
  locationType: '',
  capacity: 0,
  status: 1,
  remark: ''
})

const rules = {
  locationCode: [{ required: true, message: '请输入仓位编码', trigger: 'blur' }],
  locationName: [{ required: true, message: '请输入仓位名称', trigger: 'blur' }]
}

const getStatusLabel = (value) => {
  return value === 1 ? '启用' : '禁用'
}

const handleStatusChange = (value) => {
  searchForm.status = value
}

const handleStatusClear = () => {
  searchForm.status = undefined
}

const loadData = async () => {
  try {
    const res = await request.get('/base/location/page', { params: searchForm })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  }
}

const resetSearch = () => {
  searchForm.locationName = ''
  searchForm.status = undefined
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增仓位'
  Object.assign(form, {
    id: null,
    locationCode: '',
    locationName: '',
    zoneId: null,
    parentId: null,
    locationLevel: 1,
    locationType: '',
    capacity: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑仓位'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await request.put('/base/location', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/base/location', form)
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
    await ElMessageBox.confirm('确定要删除该仓位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/base/location/${row.id}`)
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
.location-container {
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
