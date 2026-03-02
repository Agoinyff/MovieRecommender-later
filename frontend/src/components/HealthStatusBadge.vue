<template>
  <div class="health-status-badge" :class="statusClass" :title="tooltipText">
    <div class="status-indicator">
      <div class="pulse-ring" v-if="status === 'online'"></div>
      <div class="status-dot"></div>
    </div>
    <span class="status-text">{{ statusText }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    default: 'unknown', // 'online', 'offline', 'error', 'unknown'
    validator: (value) => ['online', 'offline', 'error', 'unknown'].includes(value)
  },
  details: {
    type: String,
    default: ''
  }
});

const statusClass = computed(() => `status-${props.status}`);

const statusText = computed(() => {
  const texts = {
    online: '正常运行',
    offline: '离线',
    error: '异常',
    unknown: '未知'
  };
  return texts[props.status] || '未知';
});

const tooltipText = computed(() => {
  if (props.details) {
    return props.details;
  }
  return `系统状态: ${statusText.value}`;
});
</script>

<style scoped>
.health-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: default;
  transition: all 0.3s ease;
}

.status-indicator {
  position: relative;
  width: 10px;
  height: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}

.pulse-ring {
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
  opacity: 0.7;
}

@keyframes pulse {
  0% {
    transform: scale(0.6);
    opacity: 0.7;
  }
  50% {
    transform: scale(1);
    opacity: 0.3;
  }
  100% {
    transform: scale(0.6);
    opacity: 0.7;
  }
}

/* Online Status */
.status-online {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}

.status-online .status-dot {
  background: #10b981;
  box-shadow: 0 0 12px rgba(16, 185, 129, 0.5);
}

.status-online .pulse-ring {
  background: #10b981;
}

/* Offline Status */
.status-offline {
  background: rgba(107, 114, 128, 0.1);
  color: #4b5563;
}

.status-offline .status-dot {
  background: #6b7280;
}

/* Error Status */
.status-error {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.status-error .status-dot {
  background: #ef4444;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.5);
}

/* Unknown Status */
.status-unknown {
  background: rgba(156, 163, 175, 0.1);
  color: #6b7280;
}

.status-unknown .status-dot {
  background: #9ca3af;
}

.status-text {
  line-height: 1;
}
</style>
