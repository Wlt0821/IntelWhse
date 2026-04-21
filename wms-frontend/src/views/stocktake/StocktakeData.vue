<template>
  <div class="stocktake-data-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点数据对比</span>
          <el-button type="primary" @click="openSetDataDialog">
            <el-icon><Edit /></el-icon>
            设置盘点数据
          </el-button>
        </div>
      </template>

      <div class="plan-selector">
        <el-form :inline="true">
          <el-form-item label="选择盘点计划">
            <el-select v-model="selectedPlanId" placeholder="请选择盘点计划" style="width: 300px" @change="loadStocktakeData">
              <el-option
                v-for="plan in planList"
                :key="plan.id"
                :label="plan.planNo"
                :value="plan.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="selectedPlanId" class="data-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="data-card">
              <template #header>
                <div class="card-header-inner">
                  <span>
                    <el-icon><Box /></el-icon>
                    系统库存数据
                  </span>
                </div>
              </template>
              <el-table :data="systemInventoryData" border size="small" max-height="400">
                <el-table-column prop="locationCode" label="仓位编码" width="120" />
                <el-table-column prop="locationName" label="仓位名称" width="150" />
                <el-table-column prop="goodsCode" label="商品编码" width="120" />
                <el-table-column prop="goodsName" label="商品名称" width="180" />
                <el-table-column prop="quantity" label="系统数量" width="100" align="right">
                  <template #default="{ row }">
                    <span class="quantity-text">{{ row.quantity }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card class="data-card">
              <template #header>
                <div class="card-header-inner">
                  <span>
                    <el-icon><Monitor /></el-icon>
                    无人机盘点数据
                  </span>
                </div>
              </template>
              <el-table :data="droneStocktakeData" border size="small" max-height="400">
                <el-table-column prop="locationCode" label="仓位编码" width="120" />
                <el-table-column prop="locationName" label="仓位名称" width="150" />
                <el-table-column prop="goodsCode" label="商品编码" width="120" />
                <el-table-column prop="goodsName" label="商品名称" width="180" />
                <el-table-column prop="actualQuantity" label="实际数量" width="100" align="right">
                  <template #default="{ row }">
                    <span class="quantity-text">{{ row.actualQuantity }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="comparison-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header-inner">
              <span>
                <el-icon><DataAnalysis /></el-icon>
                盘点对比结果
              </span>
              <el-tag :type="comparisonSummary.type" size="small">
                {{ comparisonSummary.text }}
              </el-tag>
            </div>
          </template>
          <el-table :data="comparisonData" border size="small">
            <el-table-column prop="locationCode" label="仓位编码" width="120" />
            <el-table-column prop="goodsCode" label="商品编码" width="120" />
            <el-table-column prop="goodsName" label="商品名称" width="180" />
            <el-table-column prop="systemQuantity" label="系统数量" width="100" align="right" />
            <el-table-column prop="actualQuantity" label="实际数量" width="100" align="right" />
            <el-table-column prop="diffQuantity" label="差异数量" width="100" align="right">
              <template #default="{ row }">
                <span :class="getDiffClass(row.diffQuantity)">
                  {{ row.diffQuantity > 0 ? '+' : '' }}{{ row.diffQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="diffType" label="差异类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getDiffTagType(row.diffType)" size="small">
                  {{ getDiffTypeText(row.diffType) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="summary-stats" v-if="comparisonStats">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-item normal">
                  <div class="stat-label">正常</div>
                  <div class="stat-value">{{ comparisonStats.normalCount }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item over">
                  <div class="stat-label">盘盈</div>
                  <div class="stat-value">+{{ comparisonStats.overCount }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item short">
                  <div class="stat-label">盘亏</div>
                  <div class="stat-value">-{{ comparisonStats.shortCount }}</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item total">
                  <div class="stat-label">总计</div>
                  <div class="stat-value">{{ comparisonStats.totalCount }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="请选择盘点计划查看数据" />
    </el-card>

    <el-dialog
      v-model="setDataDialogVisible"
      title="设置无人机盘点数据"
      width="800px"
    >
      <el-form :model="stocktakeForm" label-width="100px">
        <el-form-item label="盘点计划">
          <el-select v-model="stocktakeForm.planId" placeholder="请选择盘点计划" style="width: 100%">
            <el-option
              v-for="plan in planList"
              :key="plan.id"
              :label="plan.planNo"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点明细">
          <el-table :data="stocktakeForm.items" border size="small">
            <el-table-column label="仓位编码" width="140">
              <template #default="{ row, $index }">
                <el-input v-model="row.locationCode" size="small" placeholder="仓位编码" />
              </template>
            </el-table-column>
            <el-table-column label="商品编码" width="140">
              <template #default="{ row, $index }">
                <el-input v-model="row.goodsCode" size="small" placeholder="商品编码" />
              </template>
            </el-table-column>
            <el-table-column label="商品名称" width="180">
              <template #default="{ row, $index }">
                <el-input v-model="row.goodsName" size="small" placeholder="商品名称" />
              </template>
            </el-table-column>
            <el-table-column label="实际数量" width="120">
              <template #default="{ row, $index }">
                <el-input-number v-model="row.actualQuantity" :min="0" size="small" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" size="small" link @click="removeStocktakeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addStocktakeItem">
            <el-icon><Plus /></el-icon>
            添加明细
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setDataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStocktakeData">
          <el-icon><Check /></el-icon>
          保存数据
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Box, Monitor, DataAnalysis, Plus, Check } from '@element-plus/icons-vue'

const selectedPlanId = ref(null)
const setDataDialogVisible = ref(false)

const planList = ref([
  { id: 1, planNo: 'CP2026041600001', checkType: 'FULL', status: 'COMPLETED' },
  { id: 2, planNo: 'CP2026041600002', checkType: 'SAMPLING', status: 'COMPLETED' }
])

const systemInventoryData = ref([
  { locationCode: 'LOC001', locationName: 'A区01排01列', goodsCode: 'ML001', goodsName: '特仑苏纯牛奶250ml*12', quantity: 100 },
  { locationCode: 'LOC001', locationName: 'A区01排01列', goodsCode: 'ML002', goodsName: '金典纯牛奶250ml*12', quantity: 80 },
  { locationCode: 'LOC002', locationName: 'A区01排02列', goodsCode: 'SN001', goodsName: '旺仔牛奶245ml*24', quantity: 150 }
])

const droneStocktakeData = ref([])

const stocktakeForm = ref({
  planId: null,
  items: []
})

const comparisonData = computed(() => {
  if (!droneStocktakeData.value.length) return []
  
  return droneStocktakeData.value.map(droneItem => {
    const systemItem = systemInventoryData.value.find(
      s => s.locationCode === droneItem.locationCode && s.goodsCode === droneItem.goodsCode
    )
    
    const systemQuantity = systemItem ? systemItem.quantity : 0
    const diffQuantity = droneItem.actualQuantity - systemQuantity
    let diffType = 'NORMAL'
    
    if (diffQuantity > 0) {
      diffType = 'OVER'
    } else if (diffQuantity < 0) {
      diffType = 'SHORT'
    }
    
    return {
      locationCode: droneItem.locationCode,
      locationName: droneItem.locationName || '',
      goodsCode: droneItem.goodsCode,
      goodsName: droneItem.goodsName || '',
      systemQuantity: systemQuantity,
      actualQuantity: droneItem.actualQuantity,
      diffQuantity: diffQuantity,
      diffType: diffType
    }
  })
})

const comparisonStats = computed(() => {
  if (!comparisonData.value.length) return null
  
  let normalCount = 0
  let overCount = 0
  let shortCount = 0
  
  comparisonData.value.forEach(item => {
    if (item.diffType === 'NORMAL') normalCount++
    else if (item.diffType === 'OVER') overCount++
    else if (item.diffType === 'SHORT') shortCount++
  })
  
  return {
    normalCount,
    overCount,
    shortCount,
    totalCount: comparisonData.value.length
  }
})

const comparisonSummary = computed(() => {
  if (!comparisonStats.value) {
    return { type: 'info', text: '暂无数据' }
  }
  
  if (comparisonStats.value.shortCount > 0) {
    return { type: 'danger', text: `存在${comparisonStats.value.shortCount}项盘亏` }
  } else if (comparisonStats.value.overCount > 0) {
    return { type: 'warning', text: `存在${comparisonStats.value.overCount}项盘盈` }
  } else {
    return { type: 'success', text: '所有数据一致' }
  }
})

function loadStocktakeData() {
  const savedData = localStorage.getItem(`stocktake_data_${selectedPlanId.value}`)
  if (savedData) {
    droneStocktakeData.value = JSON.parse(savedData)
  } else {
    droneStocktakeData.value = []
  }
}

function openSetDataDialog() {
  stocktakeForm.value = {
    planId: selectedPlanId.value || null,
    items: []
  }
  setDataDialogVisible.value = true
}

function addStocktakeItem() {
  stocktakeForm.value.items.push({
    locationCode: '',
    goodsCode: '',
    goodsName: '',
    actualQuantity: 0
  })
}

function removeStocktakeItem(index) {
  stocktakeForm.value.items.splice(index, 1)
}

function saveStocktakeData() {
  if (!stocktakeForm.value.planId) {
    ElMessage.warning('请选择盘点计划')
    return
  }
  
  if (!stocktakeForm.value.items.length) {
    ElMessage.warning('请至少添加一条明细')
    return
  }
  
  localStorage.setItem(
    `stocktake_data_${stocktakeForm.value.planId}`,
    JSON.stringify(stocktakeForm.value.items)
  )
  
  if (stocktakeForm.value.planId === selectedPlanId.value) {
    droneStocktakeData.value = [...stocktakeForm.value.items]
  }
  
  setDataDialogVisible.value = false
  ElMessage.success('盘点数据已保存')
}

function getDiffClass(diff) {
  if (diff > 0) return 'diff-over'
  if (diff < 0) return 'diff-short'
  return ''
}

function getDiffTagType(type) {
  if (type === 'OVER') return 'warning'
  if (type === 'SHORT') return 'danger'
  return 'success'
}

function getDiffTypeText(type) {
  if (type === 'OVER') return '盘盈'
  if (type === 'SHORT') return '盘亏'
  return '正常'
}

onMounted(() => {
  if (planList.value.length) {
    selectedPlanId.value = planList.value[0].id
    loadStocktakeData()
  }
})
</script>

<style scoped>
.stocktake-data-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-inner {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plan-selector {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.data-section {
  margin-top: 20px;
}

.data-card {
  height: 100%;
}

.quantity-text {
  font-weight: 600;
}

.comparison-card {
  margin-top: 20px;
}

.diff-over {
  color: #e6a23c;
  font-weight: 600;
}

.diff-short {
  color: #f56c6c;
  font-weight: 600;
}

.summary-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
}

.stat-item.normal {
  background: #f0f9ff;
  border: 1px solid #b3e19d;
}

.stat-item.over {
  background: #fdf6ec;
  border: 1px solid #f5dab1;
}

.stat-item.short {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}

.stat-item.total {
  background: #f4f4f5;
  border: 1px solid #d3d4d6;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-item.normal .stat-value {
  color: #67c23a;
}

.stat-item.over .stat-value {
  color: #e6a23c;
}

.stat-item.short .stat-value {
  color: #f56c6c;
}

.stat-item.total .stat-value {
  color: #303133;
}
</style>
