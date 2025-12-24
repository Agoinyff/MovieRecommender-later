<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="page === 0" @click="emit('change', page - 1)">上一页</button>
    <span>第 {{ page + 1 }} / {{ totalPages }} 页</span>
    <button :disabled="page + 1 >= totalPages" @click="emit('change', page + 1)">下一页</button>
  </div>
</template>

<script setup>
const props = defineProps({
  page: { type: Number, default: 0 },
  totalPages: { type: Number, default: 0 }
});

const emit = defineEmits(['change']);
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px 0 8px;
  color: rgba(226, 232, 240, 0.8);
}

button {
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: rgba(15, 23, 42, 0.35);
  color: #e2e8f0;
  padding: 8px 16px;
  border-radius: 999px;
  font-weight: 600;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
