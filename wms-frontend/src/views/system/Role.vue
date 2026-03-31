<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
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
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" width="180" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="description" label="角色描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="handlePermission(row)">权限配置</el-button>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入角色描述" type="textarea" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="800px">
      <el-tree
        ref="treeRef"
        :data="menuData"
        :props="treeProps"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :default-expanded-keys="expandedMenuIds"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({
  roleName: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const statusValue = ref('')
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const currentRoleId = ref(null)

const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const menuData = ref([
  {
    id: 1,
    menuName: '首页',
    children: []
  },
  {
    id: 2,
    menuName: '基础数据',
    children: [
      { id: 21, menuName: '客户设置' },
      { id: 22, menuName: '供应商设置' },
      { id: 23, menuName: '容器设置' },
      { id: 24, menuName: '商品设置' },
      { id: 25, menuName: '库区设置' },
      { id: 26, menuName: '仓位设置' }
    ]
  },
  {
    id: 3,
    menuName: '订单管理',
    children: [
      { id: 31, menuName: '客户订单' },
      { id: 32, menuName: '订单处理' },
      { id: 33, menuName: '拣选计划' }
    ]
  },
  {
    id: 4,
    menuName: '入库管理',
    children: [
      { id: 41, menuName: '入库计划' },
      { id: 42, menuName: '入库作业' }
    ]
  },
  {
    id: 5,
    menuName: '出库管理',
    children: [
      { id: 51, menuName: '拣货作业' },
      { id: 52, menuName: '装箱单' }
    ]
  },
  {
    id: 6,
    menuName: '补货管理',
    children: [
      { id: 61, menuName: '补货管理' }
    ]
  },
  {
    id: 7,
    menuName: '移库管理',
    children: [
      { id: 71, menuName: '移库管理' }
    ]
  },
  {
    id: 8,
    menuName: '盘点管理',
    children: [
      { id: 81, menuName: '盘点管理' }
    ]
  },
  {
    id: 9,
    menuName: '数据查询',
    children: [
      { id: 91, menuName: '数据查询' }
    ]
  },
  {
    id: 10,
    menuName: '系统管理',
    children: [
      { id: 101, menuName: '用户管理' },
      { id: 102, menuName: '角色管理' },
      { id: 103, menuName: '菜单管理' },
      { id: 104, menuName: '操作日志' }
    ]
  }
])

const treeProps = {
  children: 'children',
  label: 'menuName'
}

const checkedMenuIds = ref([])
const expandedMenuIds = ref([])

const getStatusLabel = (value) => {
  return value === 1 ? '启用' : '禁用'
}

const handleStatusChange = (value) => {
  searchForm.status = value
}

const handleStatusClear = () => {
  statusValue.value = ''
  searchForm.status = null
}

const loadData = () => {
  tableData.value = [
    { id: 1, roleName: '超级管理员', roleCode: 'SUPER_ADMIN', description: '拥有所有权限', status: 1, createTime: '2024-01-01 00:00:00' },
    { id: 2, roleName: '信息录入员', roleCode: 'DATA_ENTRY', description: '基础数据录入', status: 1, createTime: '2024-01-01 00:00:00' }
  ]
  total.value = 2
}

const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.status = null
  statusValue.value = ''
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  Object.assign(form, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadData()
  } catch {
  }
}

const handlePermission = (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  checkedMenuIds.value = [1, 2, 21, 22, 23, 24, 25, 26]
  expandedMenuIds.value = [2, 3, 4, 5, 6, 7, 8, 9, 10]
}

const handlePermissionSubmit = () => {
  ElMessage.success('权限配置成功')
  permissionDialogVisible.value = false
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-container {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.custom-select-wrapper .el-select {
  width: 100%;
}

.custom-display {
  position: absolute;
  top: 0;
  left: 12px;
  height: 100%;
  display: flex;
  align-items: center;
  pointer-events: none;
  color: #606266;
  font-size: 14px;
}

:deep(.el-select__wrapper) {
  color: transparent !important;
}

:deep(.el-select__selected-item) {
  color: transparent !important;
}
</style>
