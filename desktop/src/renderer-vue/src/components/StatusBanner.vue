<template>
  <div :class="['status-banner', tone]">
    <slot />
  </div>
</template>

<script setup>
defineProps({
  tone: {
    type: String,
    default: "info"
  }
});
</script>
