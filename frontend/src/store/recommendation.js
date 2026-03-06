import { defineStore } from 'pinia';
import { ref } from 'vue';

const STORAGE_KEY = 'movie-recommender-strategy';

export const useRecommendationStore = defineStore('recommendation', () => {
  const currentStrategy = ref(localStorage.getItem(STORAGE_KEY) || 'ITEM_BASED');

  const setStrategy = (strategy) => {
    currentStrategy.value = strategy;
    localStorage.setItem(STORAGE_KEY, strategy);
  };

  return {
    currentStrategy,
    setStrategy
  };
});
