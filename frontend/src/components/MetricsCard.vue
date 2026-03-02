<template>
  <div class="metrics-card" :style="{ borderColor: color }">
    <div class="card-header">
      <div class="icon-wrapper" :style="{ background: `${color}15` }">
        <i :class="icon" :style="{ color: color }"></i>
      </div>
      <h3 class="card-title">{{ title }}</h3>
    </div>
    
    <div class="card-body">
      <div class="value-wrapper">
        <span class="value">{{ formattedValue }}</span>
        <span v-if="unit" class="unit">{{ unit }}</span>
      </div>
      
      <div v-if="percentage !== undefined" class="progress-bar">
        <div 
          class="progress-fill" 
          :style="{ 
            width: `${Math.min(percentage, 100)}%`,
            background: getProgressColor(percentage)
          }"
        ></div>
      </div>
      
      <div v-if="subtitle" class="subtitle">{{ subtitle }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'pi pi-chart-line'
  },
  color: {
    type: String,
    default: '#6366f1'
  },
  percentage: {
    type: Number,
    default: undefined
  },
  subtitle: {
    type: String,
    default: ''
  }
});

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString();
  }
  return props.value;
});

const getProgressColor = (percent) => {
  if (percent < 50) return '#10b981'; // Green
  if (percent < 80) return '#f59e0b'; // Orange
  return '#ef4444'; // Red
};
</script>

<style scoped>
.metrics-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 2px solid rgba(229, 231, 235, 0.6);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.metrics-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper i {
  font-size: 24px;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #4b5563;
  line-height: 1.4;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.value {
  font-size: 36px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
}

.unit {
  font-size: 16px;
  font-weight: 600;
  color: #6b7280;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.subtitle {
  font-size: 13px;
  color: #9ca3af;
  font-weight: 500;
}
</style>
