<template>
  <div class="loading-container">
    <div class="spinner">
      <i class="pi pi-spin pi-spinner"></i>
    </div>
    <p v-if="message" class="loading-message">{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    default: '加载中...'
  }
});
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.spinner {
  font-size: 48px;
  color: #6366f1;
}

.loading-message {
  margin: 0;
  color: #6b7280;
  font-size: 15px;
  font-weight: 500;
}
</style>

