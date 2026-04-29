<template>
  <div class="drone-monitor">
    <div class="bg-grid"></div>
    <div class="bg-glow"></div>

    <div class="monitor-content">
      <SimpleHlsPlayer />
    </div>
  </div>
</template>

<script setup>
import SimpleHlsPlayer from '@/components/SimpleHlsPlayer.vue'
</script>

<style scoped>
.drone-monitor {
  padding: 20px;
  background: linear-gradient(135deg, #0a0e27 0%, #0d1529 50%, #0a0e27 100%);
  min-height: calc(100vh - 65px);
  position: relative;
}

.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(0,240,255,.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,240,255,.02) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none; opacity: .4;
}

.bg-glow {
  position: absolute;
  top: 20%; left: 10%;
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(0,240,255,.06) 0%, transparent 70%);
  pointer-events: none;
  animation: glow-pulse 6s ease-in-out infinite;
}
@keyframes glow-pulse{0%,100%{opacity:.4;transform:scale(1)}50%{opacity:.8;transform:scale(1.2)}}

.monitor-content {
  border-radius: 10px;
  border: 1px solid var(--cyber-border);
  box-shadow: 0 0 25px rgba(0,0,0,.3), 0 0 40px rgba(0,240,255,.08);
  position: relative;
  z-index: 2;
}
</style>
