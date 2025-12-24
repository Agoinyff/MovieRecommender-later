<template>
  <div class="app-shell">
    <Navbar />
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import Navbar from './components/Navbar.vue';
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main {
  flex: 1;
  padding: 32px 24px 60px;
}
</style>
