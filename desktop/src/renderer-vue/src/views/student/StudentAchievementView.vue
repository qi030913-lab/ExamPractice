<template>
  <section class="workspace-page">
    <div class="section-head">
      <div class="page-copy">
        <p class="page-tag">学生成就</p>
        <h2>用图表复盘你的考试成长轨迹</h2>
        <p>
          这里会实时汇总你的已提交考试记录，展示成绩趋势、题型准确率和学科表现。提交新成绩后刷新页面即可立刻看到最新变化。
        </p>
      </div>
      <div class="action-row">
        <RouterLink class="text-link" to="/student">返回学生首页</RouterLink>
        <button class="ghost-button" type="button" :disabled="loading" @click="loadAchievement(true)">
          {{ loading ? "刷新中..." : "刷新数据" }}
        </button>
      </div>
    </div>

    <StatusBanner v-if="errorMessage" tone="danger">
      {{ errorMessage }}
    </StatusBanner>
    <StatusBanner v-if="successMessage" tone="info">
      {{ successMessage }}
    </StatusBanner>

    <div v-if="summary" class="summary-row achievement-summary">
      <article class="mini-card achievement-card achievement-card-emerald">
        <h3>已完成考试</h3>
        <p>{{ summary.completedExamCount ?? 0 }}</p>
        <span>进行中 {{ summary.inProgressCount ?? 0 }} 场</span>
      </article>
      <article class="mini-card achievement-card achievement-card-sand">
        <h3>平均分</h3>
        <p>{{ formatScore(summary.averageScore) }}</p>
        <span>最高分 {{ formatNullableScore(summary.bestScore) }}</span>
      </article>
      <article class="mini-card achievement-card achievement-card-ink">
        <h3>累计正确率</h3>
        <p>{{ formatPercent(summary.accuracyRate) }}</p>
        <span>答对 {{ summary.totalCorrectCount ?? 0 }} / {{ summary.totalQuestionCount ?? 0 }}</span>
      </article>
      <article class="mini-card achievement-card achievement-card-copper">
        <h3>通过率</h3>
        <p>{{ formatPassRate(summary) }}</p>
        <span>通过 {{ summary.passCount ?? 0 }} 场 / 未通过 {{ summary.failCount ?? 0 }} 场</span>
      </article>
    </div>

    <div v-if="loading && !summary" class="empty-copy">正在加载学生成就数据...</div>
    <template v-else-if="summary">
      <div class="chart-grid">
        <article class="list-card chart-card chart-card-wide">
          <div class="section-head">
            <div>
              <h3>成绩趋势</h3>
              <p class="section-copy">每次已提交考试都会加入折线趋势，帮助你观察阶段性波动。</p>
            </div>
            <span class="pill pill-muted">共 {{ scoreTrend.length }} 次</span>
          </div>
          <div ref="scoreTrendChartRef" class="chart-panel" />
        </article>

        <article class="list-card chart-card">
          <div class="section-head">
            <div>
              <h3>题型准确率</h3>
              <p class="section-copy">横向对比不同题型的掌握情况，快速定位薄弱点。</p>
            </div>
          </div>
          <div ref="questionTypeChartRef" class="chart-panel" />
        </article>

        <article class="list-card chart-card">
          <div class="section-head">
            <div>
              <h3>学科表现</h3>
              <p class="section-copy">把不同学科的平均分和正确率放在一起看，更容易看出偏科趋势。</p>
            </div>
          </div>
          <div ref="subjectChartRef" class="chart-panel" />
        </article>
      </div>

      <div class="detail-layout">
        <article class="list-card">
          <div class="section-head">
            <div>
              <h3>学科表现明细</h3>
              <p class="section-copy">这里按学科展示考试次数、平均分和准确率，和右侧图表联动。</p>
            </div>
          </div>

          <div v-if="subjectPerformance.length" class="record-list">
            <article
              v-for="subject in subjectPerformance"
              :key="subject.subject"
              class="record-card"
            >
              <div class="record-card-head">
                <strong>{{ subject.subject }}</strong>
                <span class="pill pill-muted">{{ subject.recordCount }} 次考试</span>
              </div>
              <div class="record-card-meta">
                <span>平均分：{{ formatScore(subject.averageScore) }}</span>
                <span>正确率：{{ formatPercent(subject.accuracyRate) }}</span>
                <span>通过：{{ subject.passCount ?? 0 }}</span>
                <span>未通过：{{ subject.failCount ?? 0 }}</span>
                <span>答题：{{ subject.answeredCount ?? 0 }}/{{ subject.totalQuestionCount ?? 0 }}</span>
                <span>最近提交：{{ formatDateTime(subject.latestSubmitTime) }}</span>
              </div>
            </article>
          </div>
          <p v-else class="empty-copy">还没有足够的学科表现数据。</p>
        </article>

        <article class="list-card">
          <div class="section-head">
            <div>
              <h3>最近成绩动态</h3>
              <p class="section-copy">这里会展示最近提交的考试，用于和上方趋势图一起查看。</p>
            </div>
          </div>

          <div v-if="scoreTrend.length" class="list-stack">
            <RouterLink
              v-for="record in scoreTrend.slice().reverse().slice(0, 6)"
              :key="record.recordId"
              class="list-row"
              :to="`/student/records/${record.recordId}`"
            >
              <strong>{{ record.paperName || "未命名试卷" }}</strong>
              <span>
                {{ record.subject || "未分类" }} / {{ formatNullableScore(record.score) }} 分
                / {{ record.passed ? "已通过" : "未通过" }}
              </span>
            </RouterLink>
          </div>
          <p v-else class="empty-copy">提交考试后，这里会显示最近的成绩动态。</p>
        </article>
      </div>
    </template>
    <p v-else class="empty-copy">当前还没有可用于展示图表的成绩数据。</p>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import StatusBanner from "@/components/StatusBanner.vue";
import { useSessionStore } from "@/stores/session";
import { getStudentAchievement } from "@/services/student-api";

const sessionStore = useSessionStore();

const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const summary = ref(null);
const scoreTrend = ref([]);
const questionTypeAccuracy = ref([]);
const subjectPerformance = ref([]);

const scoreTrendChartRef = ref(null);
const questionTypeChartRef = ref(null);
const subjectChartRef = ref(null);

let scoreTrendChart = null;
let questionTypeChart = null;
let subjectChart = null;
let refreshTimer = null;
let focusHandler = null;
let resizeHandler = null;
let chartFactory = null;
let chartFactoryPromise = null;

const latestUpdatedCopy = computed(() => formatDateTime(new Date()));

async function loadAchievement(showRefreshMessage = false) {
  if (!sessionStore.user?.userId) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";
  if (showRefreshMessage) {
    successMessage.value = "";
  }

  try {
    const result = await getStudentAchievement(sessionStore.user.userId);
    if (!result?.success) {
      throw new Error(result?.message || "加载学生成就数据失败");
    }

    summary.value = result.data?.summary || null;
    scoreTrend.value = result.data?.scoreTrend || [];
    questionTypeAccuracy.value = result.data?.questionTypeAccuracy || [];
    subjectPerformance.value = result.data?.subjectPerformance || [];
    await sessionStore.loadWorkbench();
    await nextTick();
    await renderCharts();

    if (showRefreshMessage) {
      successMessage.value = `学生成就数据已刷新，更新时间 ${latestUpdatedCopy.value}`;
    }
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || "加载学生成就数据失败";
  } finally {
    loading.value = false;
  }
}

async function ensureChartFactory() {
  if (chartFactory) {
    return chartFactory;
  }

  if (!chartFactoryPromise) {
    chartFactoryPromise = Promise.all([
      import("echarts/core"),
      import("echarts/renderers"),
      import("echarts/charts"),
      import("echarts/components")
    ]).then(([core, renderers, charts, components]) => {
      core.use([
        renderers.CanvasRenderer,
        charts.BarChart,
        charts.LineChart,
        components.GridComponent,
        components.LegendComponent,
        components.MarkLineComponent,
        components.TooltipComponent
      ]);

      chartFactory = {
        init: core.init,
        graphic: core.graphic
      };

      return chartFactory;
    });
  }

  return chartFactoryPromise;
}

async function renderCharts() {
  await ensureChartFactory();
  renderScoreTrendChart();
  renderQuestionTypeChart();
  renderSubjectChart();
}

function renderScoreTrendChart() {
  if (!scoreTrendChartRef.value || !chartFactory) {
    return;
  }

  const { init, graphic } = chartFactory;
  scoreTrendChart = scoreTrendChart || init(scoreTrendChartRef.value);
  const chartData = scoreTrend.value;

  scoreTrendChart.setOption({
    animationDuration: 700,
    backgroundColor: "transparent",
    color: ["#0f766e", "#cc6b1d"],
    grid: {
      top: 48,
      left: 46,
      right: 24,
      bottom: 48
    },
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(32, 26, 21, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#fff"
      },
      formatter(params) {
        const point = params?.[0]?.data;
        if (!point) {
          return "";
        }
        return [
          `<strong>${point.paperName || "未命名试卷"}</strong>`,
          `${point.subject || "未分类"}`,
          `成绩：${formatNullableScore(point.score)} / ${point.totalScore ?? "-"}`,
          `及格线：${point.passScore ?? "-"}`,
          `提交：${formatDateTime(point.submitTime)}`
        ].join("<br/>");
      }
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: "rgba(92, 72, 56, 0.2)"
        }
      },
      axisLabel: {
        color: "#6e655d",
        formatter: (_value, index) => `第${index + 1}次`
      },
      data: chartData.map((_item, index) => `第${index + 1}次`)
    },
    yAxis: {
      type: "value",
      min: 0,
      max(value) {
        return Math.max(100, Math.ceil((value.max || 100) / 10) * 10);
      },
      splitLine: {
        lineStyle: {
          color: "rgba(92, 72, 56, 0.08)"
        }
      },
      axisLabel: {
        color: "#6e655d"
      }
    },
    series: [
      {
        name: "成绩",
        type: "line",
        smooth: 0.35,
        symbol: "circle",
        symbolSize: 10,
        data: chartData.map((item, index) => ({
          value: Number(item.score ?? 0),
          ...item,
          index: index + 1
        })),
        lineStyle: {
          width: 4
        },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(15, 118, 110, 0.28)" },
            { offset: 1, color: "rgba(15, 118, 110, 0.03)" }
          ])
        },
        itemStyle: {
          borderWidth: 2,
          borderColor: "#fff"
        },
        markLine: summary.value
          ? {
              symbol: "none",
              label: {
                color: "#9a551e",
                formatter: `平均分 ${formatScore(summary.value.averageScore)}`
              },
              lineStyle: {
                type: "dashed",
                color: "rgba(204, 107, 29, 0.9)"
              },
              data: [{ yAxis: Number(summary.value.averageScore || 0) }]
            }
          : undefined
      }
    ]
  });
}

function renderQuestionTypeChart() {
  if (!questionTypeChartRef.value || !chartFactory) {
    return;
  }

  const { init, graphic } = chartFactory;
  questionTypeChart = questionTypeChart || init(questionTypeChartRef.value);
  const data = questionTypeAccuracy.value;

  questionTypeChart.setOption({
    animationDuration: 700,
    backgroundColor: "transparent",
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow"
      },
      backgroundColor: "rgba(32, 26, 21, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#fff"
      },
      formatter(params) {
        const item = params?.[0]?.data;
        if (!item) {
          return "";
        }
        return [
          `<strong>${item.label}</strong>`,
          `准确率：${formatPercent(item.accuracyRate)}`,
          `答对：${item.correctCount} / ${item.totalCount}`,
          `已作答：${item.answeredCount}`
        ].join("<br/>");
      }
    },
    grid: {
      top: 20,
      left: 42,
      right: 18,
      bottom: 64
    },
    xAxis: {
      type: "category",
      axisLabel: {
        color: "#6e655d",
        interval: 0,
        rotate: data.length > 4 ? 20 : 0
      },
      axisLine: {
        lineStyle: {
          color: "rgba(92, 72, 56, 0.2)"
        }
      },
      data: data.map((item) => item.label)
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      axisLabel: {
        color: "#6e655d",
        formatter: "{value}%"
      },
      splitLine: {
        lineStyle: {
          color: "rgba(92, 72, 56, 0.08)"
        }
      }
    },
    series: [
      {
        type: "bar",
        barWidth: "52%",
        data: data.map((item) => ({
          value: Number(item.accuracyRate || 0),
          ...item,
          itemStyle: {
            borderRadius: [14, 14, 6, 6],
            color: new graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: "#0f766e" },
              { offset: 1, color: "#6fb6a9" }
            ])
          }
        })),
        label: {
          show: true,
          position: "top",
          color: "#201a15",
          formatter: ({ value }) => `${formatPercent(value)}`
        }
      }
    ]
  });
}

function renderSubjectChart() {
  if (!subjectChartRef.value || !chartFactory) {
    return;
  }

  const { init, graphic } = chartFactory;
  subjectChart = subjectChart || init(subjectChartRef.value);
  const data = subjectPerformance.value;

  subjectChart.setOption({
    animationDuration: 700,
    backgroundColor: "transparent",
    legend: {
      top: 0,
      textStyle: {
        color: "#6e655d"
      }
    },
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(32, 26, 21, 0.92)",
      borderWidth: 0,
      textStyle: {
        color: "#fff"
      },
      formatter(params) {
        const item = data[params?.[0]?.dataIndex ?? 0];
        if (!item) {
          return "";
        }
        return [
          `<strong>${item.subject}</strong>`,
          `平均分：${formatScore(item.averageScore)}`,
          `正确率：${formatPercent(item.accuracyRate)}`,
          `通过：${item.passCount} / 未通过：${item.failCount}`
        ].join("<br/>");
      }
    },
    grid: {
      top: 48,
      left: 42,
      right: 18,
      bottom: 64
    },
    xAxis: {
      type: "category",
      axisLabel: {
        color: "#6e655d",
        interval: 0,
        rotate: data.length > 4 ? 18 : 0
      },
      axisLine: {
        lineStyle: {
          color: "rgba(92, 72, 56, 0.2)"
        }
      },
      data: data.map((item) => item.subject)
    },
    yAxis: [
      {
        type: "value",
        name: "平均分",
        min: 0,
        axisLabel: {
          color: "#6e655d"
        },
        splitLine: {
          lineStyle: {
            color: "rgba(92, 72, 56, 0.08)"
          }
        }
      },
      {
        type: "value",
        name: "正确率",
        min: 0,
        max: 100,
        axisLabel: {
          color: "#6e655d",
          formatter: "{value}%"
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: "平均分",
        type: "bar",
        barWidth: "40%",
        data: data.map((item) => ({
          value: Number(item.averageScore || 0),
          itemStyle: {
            borderRadius: [12, 12, 6, 6],
            color: new graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: "#cc6b1d" },
              { offset: 1, color: "#f4b37a" }
            ])
          }
        }))
      },
      {
        name: "正确率",
        type: "line",
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 9,
        data: data.map((item) => Number(item.accuracyRate || 0)),
        lineStyle: {
          width: 3,
          color: "#0f766e"
        },
        itemStyle: {
          color: "#0f766e",
          borderWidth: 2,
          borderColor: "#fff"
        }
      }
    ]
  });
}

function bindRefreshHooks() {
  resizeHandler = () => {
    scoreTrendChart?.resize();
    questionTypeChart?.resize();
    subjectChart?.resize();
  };
  window.addEventListener("resize", resizeHandler);

  focusHandler = () => {
    loadAchievement(false).catch(() => undefined);
  };
  window.addEventListener("focus", focusHandler);

  refreshTimer = window.setInterval(() => {
    loadAchievement(false).catch(() => undefined);
  }, 30000);
}

function unbindRefreshHooks() {
  if (resizeHandler) {
    window.removeEventListener("resize", resizeHandler);
    resizeHandler = null;
  }
  if (focusHandler) {
    window.removeEventListener("focus", focusHandler);
    focusHandler = null;
  }
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

function disposeCharts() {
  scoreTrendChart?.dispose();
  questionTypeChart?.dispose();
  subjectChart?.dispose();
  scoreTrendChart = null;
  questionTypeChart = null;
  subjectChart = null;
}

function formatScore(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue.toFixed(1) : "0.0";
}

function formatNullableScore(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? numericValue.toFixed(1) : value;
}

function formatPercent(value) {
  const numericValue = Number(value);
  return Number.isFinite(numericValue) ? `${numericValue.toFixed(1)}%` : "0.0%";
}

function formatPassRate(currentSummary) {
  const completed = Number(currentSummary?.completedExamCount || 0);
  const passed = Number(currentSummary?.passCount || 0);
  if (!completed) {
    return "0.0%";
  }
  return `${((passed * 100) / completed).toFixed(1)}%`;
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return date.toLocaleString("zh-CN", { hour12: false });
}

onMounted(async () => {
  await loadAchievement(false);
  bindRefreshHooks();
});

onBeforeUnmount(() => {
  unbindRefreshHooks();
  disposeCharts();
});
</script>
