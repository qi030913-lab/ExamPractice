import { computed, ref, unref, watch } from "vue";

export function usePagination(items, options = {}) {
  const {
    initialPageSize = 6
  } = options;

  const currentPage = ref(1);
  const pageSize = ref(initialPageSize);

  const totalItems = computed(() => {
    const source = unref(items) || [];
    return source.length;
  });

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(totalItems.value / pageSize.value));
  });

  const paginatedItems = computed(() => {
    const source = unref(items) || [];
    const startIndex = (currentPage.value - 1) * pageSize.value;
    return source.slice(startIndex, startIndex + pageSize.value);
  });

  const pageSummary = computed(() => {
    if (!totalItems.value) {
      return {
        start: 0,
        end: 0
      };
    }

    const start = (currentPage.value - 1) * pageSize.value + 1;
    const end = Math.min(currentPage.value * pageSize.value, totalItems.value);
    return { start, end };
  });

  watch(pageSize, () => {
    currentPage.value = 1;
  });

  watch(totalPages, (pageCount) => {
    if (currentPage.value > pageCount) {
      currentPage.value = pageCount;
    }
  });

  watch(
    () => unref(items),
    () => {
      currentPage.value = 1;
    }
  );

  function goToPage(page) {
    if (!Number.isFinite(page)) {
      return;
    }

    currentPage.value = Math.min(Math.max(1, page), totalPages.value);
  }

  return {
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    paginatedItems,
    pageSummary,
    goToPage
  };
}
