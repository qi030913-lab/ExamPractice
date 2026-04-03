<template>
  <div
    :class="['status-banner', tone]"
    :role="tone === 'danger' ? 'alert' : 'status'"
    :aria-live="tone === 'danger' ? 'assertive' : 'polite'"
    aria-atomic="true"
  >
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
