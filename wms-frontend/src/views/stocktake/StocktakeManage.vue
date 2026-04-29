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
        <el-table-column prop="zoneName" label="库区" width="180" />
        <el-table-column prop="locationName" label="仓位" width="180" />
        <el-table-column prop="goodsName" label="商品" width="180" />
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
        <el-form-item label="库区" prop="zoneId">
          <el-select v-model="form.zoneId" placeholder="请选择库区" style="width: 100%;" clearable>
            <el-option v-for="item in zoneList" :key="item.id" :label="item.zoneName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓位" prop="locationId">
          <el-select v-model="form.locationId" placeholder="请选择仓位" style="width: 100%;" clearable>
            <el-option v-for="item in locationList" :key="item.id" :label="item.locationName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="请选择商品" style="width: 100%;" clearable>
            <el-option v-for="item in goodsList" :key="item.id" :label="item.goodsName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="DRAFT">草稿</el-radio>
            <el-radio value="PENDING">待执行</el-radio>
            <el-radio value="PROCESSING">执行中</el-radio>
            <el-radio value="COMPLETED">已完成</el-radio>
            <el-radio value="CANCELLED">已取消</el-radio>
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
const zoneList = ref([])
const locationList = ref([])
const goodsList = ref([])

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

const loadZones = async () => {
  try {
    const res = await request.get('/base/zone/all', { params: { status: 1 } })
    zoneList.value = res.data || []
  } catch (e) {
    console.error('加载库区失败', e)
    zoneList.value = []
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

const loadGoods = async () => {
  try {
    const res = await request.get('/base/goods/all', { params: { status: 1 } })
    goodsList.value = res.data || []
  } catch (e) {
    console.error('加载商品失败', e)
    goodsList.value = []
  }
}

const loadData = async () => {
  try {
    const params = { }
    params.pageNum = searchForm.pageNum || 1
    params.pageSize = searchForm.pageSize || 10
    if (searchForm.planNo && searchForm.planNo.trim()) {
      params.planNo = searchForm.planNo.trim()
    }
    if (checkTypeValue.value) {
      params.checkType = checkTypeValue.value
    }
    if (statusValue.value) {
      params.status = statusValue.value
    }
    const res = await request.get('/stocktake/plan/page', { params })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
    tableData.value = []
    total.value = 0
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
  loadZones()
  loadLocations()
  loadGoods()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑盘点计划'
  Object.assign(form, row)
  loadZones()
  loadLocations()
  loadGoods()
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
  loadZones()
  loadLocations()
  loadGoods()
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
  display: inline-block;
}

.custom-display {
  position: absolute;
  top: 0;
  left: 12px;
  right: 30px;
  height: 32px;
  line-height: 32px;
  color: var(--cyber-text);
  font-size: 14px;
  pointer-events: none;
  z-index: 10;
}
</style>
