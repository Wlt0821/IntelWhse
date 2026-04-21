<template>
  <div class="drone-monitor">
    <div class="monitor-header">
      <h2>
        <el-icon><Monitor /></el-icon>
        智能盘点
      </h2>
    </div>

    <div class="monitor-content">
      <SimpleHlsPlayer />
    </div>
  </div>
</template>

<script setup>
import { Monitor } from '@element-plus/icons-vue'
import SimpleHlsPlayer from '@/components/SimpleHlsPlayer.vue'
</script>

<style scoped>
.drone-monitor {
  padding: 20px;
  background: #f0f2f5;
  height: calc(100vh - 60px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
  flex-shrink: 0;
}

.monitor-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #303133;
}

.monitor-content {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}
</style>
