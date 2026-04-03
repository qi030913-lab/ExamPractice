<template>
  <div v-if="shouldRender" class="pager-bar">
    <p class="pager-info">
      第 {{ currentPage }} / {{ totalPages }} 页
      · 当前显示 {{ start }}-{{ end }} {{ itemLabel }}
      · 共 {{ totalItems }} {{ itemLabel }}
    </p>
    <div class="pager-controls">
      <label v-if="pageSizeOptions.length" class="page-size-control">
        <span>每页</span>
        <select :value="pageSize" @change="handlePageSizeChange">
          <option v-for="size in pageSizeOptions" :key="size" :value="size">
            {{ size }} {{ itemLabel }}
          </option>
        </select>
      </label>
      <div class="pager-actions">
        <button
          class="ghost-button"
          type="button"
          :disabled="currentPage <= 1"
          @click="$emit('change-page', currentPage - 1)"
        >
          上一页
        </button>
        <div class="pager-pages">
          <button
            v-for="page in visiblePages"
            :key="page"
            type="button"
            :class="['pager-button', currentPage === page ? 'pager-button-active' : '']"
            @click="$emit('change-page', page)"
          >
            {{ page }}
          </button>
        </div>
        <button
          class="ghost-button"
          type="button"
          :disabled="currentPage >= totalPages"
          @click="$emit('change-page', currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  pageSizeOptions: {
    type: Array,
    default: () => []
  },
  start: {
    type: Number,
    default: 0
  },
  end: {
    type: Number,
    default: 0
  },
  itemLabel: {
    type: String,
    default: "条"
  }
});

const emit = defineEmits(["change-page", "update:pageSize"]);

const minimumPageSize = computed(() => {
  if (!props.pageSizeOptions.length) {
    return props.pageSize;
  }

  return Math.min(...props.pageSizeOptions);
});

const shouldRender = computed(() => {
  return props.totalItems > minimumPageSize.value || props.totalPages > 1;
});

const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;
  const half = Math.floor(maxVisible / 2);
  let start = Math.max(1, props.currentPage - half);
  let end = Math.min(props.totalPages, start + maxVisible - 1);

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1);
  }

  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }

  return pages;
});

function handlePageSizeChange(event) {
  const nextPageSize = Number(event.target.value);
  if (!Number.isFinite(nextPageSize) || nextPageSize <= 0) {
    return;
  }

  emit("update:pageSize", nextPageSize);
}
</script>
