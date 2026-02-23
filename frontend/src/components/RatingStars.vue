<template>
  <div class="rating-stars">
    <i
      v-for="star in 5"
      :key="star"
      :class="getStarClass(star)"
      @click="!readonly && handleClick(star)"
      @mouseenter="!readonly && handleHover(star)"
      @mouseleave="!readonly && handleLeave()"
      :style="{ fontSize: size + 'px', cursor: readonly ? 'default' : 'pointer' }"
    ></i>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  readonly: {
    type: Boolean,
    default: false
  },
  size: {
    type: Number,
    default: 24
  }
});

const emit = defineEmits(['update:modelValue']);

const hoverRating = ref(0);

const getStarClass = (star) => {
  const currentRating = hoverRating.value || props.modelValue;
  
  if (currentRating >= star) {
    return 'pi pi-star-fill star-filled';
  } else {
    return 'pi pi-star star-empty';
  }
};

const handleClick = (rating) => {
  emit('update:modelValue', rating);
};

const handleHover = (rating) => {
  hoverRating.value = rating;
};

const handleLeave = () => {
  hoverRating.value = 0;
};
</script>

<style scoped>
.rating-stars {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.rating-stars i {
  transition: all 0.2s ease;
  color: #d1d5db;
}

.star-filled {
  color: #fbbf24 !important;
  text-shadow: 0 0 8px rgba(251, 191, 36, 0.3);
}

.star-empty {
  color: #d1d5db;
}

.rating-stars i:not(.readonly):hover {
  transform: scale(1.2);
}
</style>
