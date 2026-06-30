<template>
  <div>
    <h2 class="page-title">数据概览</h2>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card card">
        <div class="stat-icon" :style="{ background: item.bg }" v-html="item.icon"></div>
        <div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 前两个：ECharts图表（饼图 + 柱状图） -->
    <div class="charts-grid">
      <!-- 系统用户构成（饼图） -->
      <div class="card chart-card">
        <h3 class="chart-title">系统用户构成</h3>
        <div ref="userChartRef" class="chart-container"></div>
      </div>

      <!-- 楼栋房屋分布（柱状图） -->
      <div class="card chart-card">
        <h3 class="chart-title">楼栋房屋分布</h3>
        <div ref="buildingChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 中间三个图表 -->
    <div class="charts-grid">
      <!-- 报修状态 - 动态环形进度图 -->
      <div class="card chart-card">
        <h3 class="chart-title">报修状态</h3>
        <div class="status-ring-container">
          <div 
            v-for="(item, index) in repairData" 
            :key="item.name" 
            class="ring-item"
            :class="{ 'ring-active': activeRingIndex === index }"
            @mouseenter="handleRingHover(index)"
            @mouseleave="handleRingLeave()"
          >
            <div class="ring-wrapper">
              <svg viewBox="0 0 120 120" class="ring-svg">
                <circle
                  cx="60"
                  cy="60"
                  r="45"
                  fill="none"
                  stroke="#ECFDF5"
                  stroke-width="10"
                />
                <circle
                  cx="60"
                  cy="60"
                  r="45"
                  fill="none"
                  :stroke="ringColors[index % ringColors.length]"
                  stroke-width="10"
                  stroke-linecap="round"
                  :stroke-dasharray="`${getRingPercent(item.value)} 100`"
                  :stroke-dashoffset="0"
                  transform="rotate(-90 60 60)"
                >
                  <animate
                    attributeName="stroke-dasharray"
                    :from="`0 100`"
                    :to="`${getRingPercent(item.value)} 100`"
                    dur="1s"
                    fill="freeze"
                  />
                </circle>
                <circle
                  cx="60"
                  cy="60"
                  r="48"
                  fill="none"
                  :stroke="ringColors[index % ringColors.length]"
                  stroke-width="2"
                  stroke-linecap="round"
                  opacity="0"
                  class="ring-glow"
                  :class="{ 'ring-glow-active': activeRingIndex === index || (activeRingIndex === -1 && index === 0) }"
                >
                  <animate
                    attributeName="opacity"
                    values="0;0.6;0"
                    dur="2s"
                    repeatCount="indefinite"
                  />
                </circle>
                <text x="60" y="54" text-anchor="middle" font-size="20" font-weight="bold" fill="#1F2937">
                  {{ item.value }}
                </text>
                <text x="60" y="76" text-anchor="middle" font-size="10" fill="#6B7280">
                  {{ repairLabel(item.name) }}
                </text>
              </svg>
            </div>
            <div class="ring-label" :style="{ color: ringColors[index % ringColors.length] }">
              {{ repairLabel(item.name) }}
            </div>
          </div>
        </div>
        <div class="ring-status-indicator">
          <span v-for="(item, index) in repairData" :key="item.name" 
                class="status-dot-indicator"
                :class="{ active: activeRingIndex === index || (activeRingIndex === -1 && index === 0) }"
                :style="{ background: ringColors[index % ringColors.length] }"
                @click="activeRingIndex = index"
          ></span>
        </div>
      </div>

      <!-- 房屋状态 - 水平进度条 -->
      <div class="card chart-card">
        <h3 class="chart-title">房屋状态</h3>
        <div class="status-bar-container">
          <div v-for="(item, index) in unitStatusData" :key="item.name" class="status-bar-item">
            <div class="status-bar-header">
              <span class="status-bar-label">
                <span class="status-dot" :style="{ background: barColors[index % barColors.length] }"></span>
                {{ item.name }}
              </span>
              <span class="status-bar-count">{{ item.value }}套</span>
            </div>
            <div class="status-bar-track">
              <div
                class="status-bar-fill"
                :style="{
                  width: getStatusBarWidth(item.value) + '%',
                  background: barColors[index % barColors.length]
                }"
              >
                <div class="status-bar-fill-animate" :style="{ width: getStatusBarWidth(item.value) + '%' }"></div>
              </div>
            </div>
            <div class="status-bar-percent">{{ getStatusPercent(item.value) }}%</div>
          </div>
        </div>
      </div>

      <!-- 物业费收缴 - 仪表盘样式 -->
      <div class="card chart-card">
        <h3 class="chart-title">物业费收缴</h3>
        <div class="fee-gauge-container">
          <div class="gauge-wrapper">
            <svg viewBox="0 0 200 120" class="gauge-svg">
              <path
                d="M 30 110 A 80 80 0 0 1 170 110"
                fill="none"
                stroke="#ECFDF5"
                stroke-width="20"
                stroke-linecap="round"
              />
              <path
                d="M 30 110 A 80 80 0 0 1 170 110"
                fill="none"
                stroke="#059669"
                stroke-width="20"
                stroke-linecap="round"
                :stroke-dasharray="`${getGaugePercent()} 100`"
              >
                <animate
                  attributeName="stroke-dasharray"
                  :from="`0 100`"
                  :to="`${getGaugePercent()} 100`"
                  dur="1.5s"
                  fill="freeze"
                />
              </path>
              <line
                x1="100"
                y1="110"
                :x2="getPointerX()"
                :y2="getPointerY()"
                stroke="#EF4444"
                stroke-width="3"
                stroke-linecap="round"
              >
                <animate
                  attributeName="x2"
                  :from="100"
                  :to="getPointerX()"
                  dur="1.5s"
                  fill="freeze"
                />
                <animate
                  attributeName="y2"
                  :from="110"
                  :to="getPointerY()"
                  dur="1.5s"
                  fill="freeze"
                />
              </line>
              <circle cx="100" cy="110" r="6" fill="#EF4444" />
              <text x="100" y="70" text-anchor="middle" font-size="28" font-weight="bold" fill="#1F2937">
                {{ getGaugePercent() }}%
              </text>
              <text x="100" y="92" text-anchor="middle" font-size="12" fill="#6B7280">
                收缴率
              </text>
            </svg>
          </div>
          <div class="gauge-legend">
            <div v-for="item in feeData" :key="item.name" class="gauge-legend-item">
              <span class="gauge-dot" :style="{ background: feeLabel(item.name) === '已缴' ? '#059669' : '#F59E0B' }"></span>
              <span>{{ feeLabel(item.name) }}</span>
              <span class="gauge-legend-value">{{ item.value }}笔</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 后续三个图表 -->
    <div class="charts-grid">
      <!-- 物业费月度趋势 - 折线图 -->
      <div class="card chart-card">
        <h3 class="chart-title">物业费月度趋势</h3>
        <div ref="feeTrendChartRef" class="chart-container"></div>
      </div>

      <!-- 报修类型分布 - 横向条形图（动态） -->
      <div class="card chart-card">
        <h3 class="chart-title">报修类型分布</h3>
        <div class="repair-type-container">
          <div v-for="(item, index) in repairTypeData" :key="item.name" class="repair-type-item">
            <div class="repair-type-header">
              <span class="repair-type-label">
                <span class="repair-type-icon" :style="{ background: repairTypeColors[index % repairTypeColors.length] }">
                  {{ getRepairIcon(item.name) }}
                </span>
                {{ item.name }}
              </span>
              <span class="repair-type-count">{{ item.value }}件</span>
            </div>
            <div class="repair-type-track">
              <div 
                class="repair-type-fill" 
                :style="{
                  width: getRepairTypeWidth(item.value) + '%',
                  background: repairTypeColors[index % repairTypeColors.length]
                }"
              >
                <div class="repair-type-fill-animate"></div>
              </div>
            </div>
            <div class="repair-type-footer">
              <span class="repair-type-percent">{{ getRepairTypePercent(item.value) }}%</span>
              <span class="repair-type-bar-value">{{ item.value }}件</span>
            </div>
          </div>
          <!-- 统计摘要 -->
          <div class="repair-type-summary">
            <span>总计：{{ repairTypeTotal }} 件报修</span>
            <span class="repair-type-most">
              ⚡ 最多：{{ getMostCommonRepairType() }}
            </span>
          </div>
        </div>
      </div>

      <!-- 账单月份分布 - 柱状图 -->
      <div class="card chart-card">
        <h3 class="chart-title">账单月份分布</h3>
        <div ref="feeMonthChartRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick, onUnmounted } from 'vue'
import request from '../api/request'
import * as echarts from 'echarts'

const statCards = ref([
  { label: '楼栋总数', value: 0, bg: 'rgba(5,150,105,0.12)', icon: '🏢' },
  { label: '房屋总数', value: 0, bg: 'rgba(16,185,129,0.12)', icon: '🏠' },
  { label: '在册业主', value: 0, bg: 'rgba(59,130,246,0.12)', icon: '👥' },
  { label: '待处理报修', value: 0, bg: 'rgba(217,119,6,0.12)', icon: '🔧' },
  { label: '待缴账单', value: 0, bg: 'rgba(239,68,68,0.1)', icon: '💰' },
  { label: '待审访客', value: 0, bg: 'rgba(139,92,246,0.1)', icon: '🚪' }
])

const buildingData = ref([])
const repairData = ref([])
const unitStatusData = ref([])
const feeData = ref([])
const feeTrendData = ref([])
const repairTypeData = ref([])
const userRoleData = ref([])
const feeMonthData = ref([])

// DOM引用
const userChartRef = ref(null)
const buildingChartRef = ref(null)
const feeTrendChartRef = ref(null)
const feeMonthChartRef = ref(null)

// 动态高亮控制
const activeRingIndex = ref(-1)
let ringAutoPlayTimer = null
let isRingHovering = false

const pieColors = ['#059669', '#34D399', '#F59E0B', '#EF4444', '#8B5CF6', '#06B6D4']
const ringColors = ['#059669', '#F59E0B', '#EF4444']
const barColors = ['#059669', '#34D399', '#F59E0B', '#8B5CF6']
const repairTypeColors = ['#EF4444', '#F59E0B', '#3B82F6', '#8B5CF6', '#EC4899', '#06B6D4']

// ========== 报修类型辅助方法 ==========
function getRepairTypeWidth(value) {
  const max = Math.max(...repairTypeData.value.map(d => d.value), 1)
  return Math.round((value / max) * 100)
}

function getRepairTypePercent(value) {
  const total = repairTypeData.value.reduce((sum, d) => sum + d.value, 0)
  return total > 0 ? Math.round((value / total) * 100) : 0
}

const repairTypeTotal = computed(() => {
  return repairTypeData.value.reduce((sum, d) => sum + d.value, 0)
})

function getMostCommonRepairType() {
  if (repairTypeData.value.length === 0) return '无'
  const max = Math.max(...repairTypeData.value.map(d => d.value))
  const item = repairTypeData.value.find(d => d.value === max)
  return item ? item.name : '无'
}

function getRepairIcon(name) {
  const iconMap = {
    '水电维修': '💧',
    '电器维修': '⚡',
    '门窗维修': '🚪',
    '管道疏通': '🔧',
    '墙面维修': '🧱',
    '其他': '📌'
  }
  return iconMap[name] || '🔨'
}

// ========== 环形图辅助方法 ==========
function getRingPercent(value) {
  const total = repairData.value.reduce((sum, d) => sum + d.value, 0)
  return total > 0 ? Math.round((value / total) * 100) : 0
}

// ========== 状态条形图辅助方法 ==========
function getStatusBarWidth(value) {
  const max = Math.max(...unitStatusData.value.map(d => d.value), 1)
  return Math.round((value / max) * 100)
}

function getStatusPercent(value) {
  const total = unitStatusData.value.reduce((sum, d) => sum + d.value, 0)
  return total > 0 ? Math.round((value / total) * 100) : 0
}

// ========== 仪表盘辅助方法 ==========
function getGaugePercent() {
  const total = feeData.value.reduce((sum, d) => sum + d.value, 0)
  if (total === 0) return 0
  const paid = feeData.value.find(d => d.name === 'PAID')?.value || 0
  return Math.round((paid / total) * 100)
}

function getPointerX() {
  const percent = getGaugePercent() / 100
  const angle = -Math.PI * 0.75 + percent * Math.PI * 1.5
  return 100 + 60 * Math.cos(angle)
}

function getPointerY() {
  const percent = getGaugePercent() / 100
  const angle = -Math.PI * 0.75 + percent * Math.PI * 1.5
  return 110 - 60 * Math.sin(angle)
}

// ========== 环形图交互控制 ==========
function handleRingHover(index) {
  isRingHovering = true
  activeRingIndex.value = index
  stopRingAutoPlay()
}

function handleRingLeave() {
  isRingHovering = false
  activeRingIndex.value = -1
  startRingAutoPlay()
}

// ========== 原有辅助方法 ==========
function repairLabel(name) {
  return { PENDING: '待处理', PROCESSING: '处理中', DONE: '已完成' }[name] || name
}

function feeLabel(name) {
  return { PAID: '已缴', UNPAID: '未缴',OVERDUE: '逾期' }[name] || name
}

function roleLabel(name) {
  const map = {
    'ADMIN': '管理员',
    'PROPERTY_MANAGER': '物业经理',
    'OWNER': '业主'
  }
  return map[name] || name
}

// ========== ECharts图表渲染 ==========

// 渲染饼图
function renderPieChart(domRef, data, nameMap) {
  if (!domRef) return
  const chart = echarts.init(domRef)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemWidth: 12,
      itemHeight: 12
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{d}%',
        fontSize: 12
      },
      emphasis: {
        scale: true
      },
      data: data.map((item, index) => ({
        name: nameMap ? nameMap(item.name) : item.name,
        value: item.value,
        itemStyle: {
          color: pieColors[index % pieColors.length]
        }
      }))
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
  return chart
}

// 渲染柱状图（楼栋房屋分布）
function renderBarChart(domRef, data, nameMap) {
  if (!domRef) return
  const chart = echarts.init(domRef)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const p = params[0]
        return p.name + '<br/>' + p.seriesName + ': ' + p.value + '套'
      }
    },
    grid: {
      left: '10%',
      right: '8%',
      top: '10%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => nameMap ? nameMap(item.name) : item.name),
      axisLabel: {
        fontSize: 11,
        rotate: data.length > 4 ? 20 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: '房屋数',
      nameTextStyle: { fontSize: 11 }
    },
    series: [{
      type: 'bar',
      barWidth: '45%',
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#059669' },
          { offset: 1, color: '#34D399' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        formatter: function(params) {
          return params.value + '套'
        },
        fontSize: 11
      },
      data: data.map(item => item.value)
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
  return chart
}

// 渲染物业费月度趋势（动态折线图）
function renderFeeTrendChart(domRef, data) {
  if (!domRef) return
  const chart = echarts.init(domRef)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const p = params[0]
        return p.name + '<br/>' + p.seriesName + ': ¥' + p.value
      }
    },
    grid: {
      left: '10%',
      right: '8%',
      top: '15%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLabel: {
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '金额(元)',
      nameTextStyle: { fontSize: 11 }
    },
    series: [{
      name: '物业费',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: '#8B5CF6'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(139, 92, 246, 0.3)' },
          { offset: 1, color: 'rgba(139, 92, 246, 0.05)' }
        ])
      },
      itemStyle: {
        color: '#8B5CF6'
      },
      label: {
        show: true,
        formatter: function(params) {
          return '¥' + params.value
        },
        fontSize: 10,
        position: 'top'
      },
      data: data.map(item => item.value),
      animationDuration: 1500,
      animationEasing: 'cubicOut'
    }],
    animationDurationUpdate: 1000,
    animationEasingUpdate: 'cubicOut'
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
  return chart
}

// 渲染账单月份分布（动态柱状图）
function renderFeeMonthChart(domRef, data) {
  if (!domRef) return
  const chart = echarts.init(domRef)
  
  const colors = ['#06B6D4', '#3B82F6', '#8B5CF6', '#EC4899', '#F59E0B']
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const p = params[0]
        return p.name + '<br/>' + p.seriesName + ': ' + p.value + '笔'
      }
    },
    grid: {
      left: '10%',
      right: '8%',
      top: '15%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLabel: {
        fontSize: 11,
        rotate: data.length > 4 ? 20 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: '账单数',
      nameTextStyle: { fontSize: 11 }
    },
    series: [{
      name: '账单数',
      type: 'bar',
      barWidth: '50%',
      data: data.map((item, index) => ({
        value: item.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[index % colors.length] },
            { offset: 1, color: colors[(index + 1) % colors.length] }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: {
        show: true,
        position: 'top',
        formatter: function(params) {
          return params.value + '笔'
        },
        fontSize: 11
      },
      animationDuration: 1500,
      animationEasing: 'bounceOut'
    }],
    animationDurationUpdate: 1000,
    animationEasingUpdate: 'bounceOut'
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
  return chart
}

let charts = []

// 启动环形图自动轮播
function startRingAutoPlay() {
  if (isRingHovering) return
  if (ringAutoPlayTimer) clearInterval(ringAutoPlayTimer)
  ringAutoPlayTimer = setInterval(() => {
    if (repairData.value.length === 0) return
    activeRingIndex.value = (activeRingIndex.value + 1) % repairData.value.length
  }, 3000)
}

// 停止环形图自动轮播
function stopRingAutoPlay() {
  if (ringAutoPlayTimer) {
    clearInterval(ringAutoPlayTimer)
    ringAutoPlayTimer = null
  }
}

onMounted(async () => {
  const data = await request.get('/dashboard/stats')

  const pendingRepair = (data.repairStatusDistribution || [])
      .filter(i => i.name === 'PENDING' || i.name === 'PROCESSING')
      .reduce((s, i) => s + i.value, 0)
  const values = [data.buildingCount, data.unitCount, data.ownerCount, pendingRepair, data.unpaidFeeCount, data.pendingVisitorCount]
  statCards.value.forEach((c, i) => { c.value = values[i] ?? 0 })

  buildingData.value = data.buildingDistribution || []
  repairData.value = data.repairStatusDistribution || []
  unitStatusData.value = data.unitStatusDistribution || []
  feeData.value = data.feePayStatusDistribution || []
  feeTrendData.value = data.feeTrend || []
  repairTypeData.value = data.repairTypeDistribution || []
  userRoleData.value = data.userRoleDistribution || []
  feeMonthData.value = data.feeMonthDistribution || []

  await nextTick()

  const chart1 = renderPieChart(userChartRef.value, userRoleData.value, roleLabel)
  const chart2 = renderBarChart(buildingChartRef.value, buildingData.value)
  const chart3 = renderFeeTrendChart(feeTrendChartRef.value, feeTrendData.value)
  const chart4 = renderFeeMonthChart(feeMonthChartRef.value, feeMonthData.value)
  
  if (chart1) charts.push(chart1)
  if (chart2) charts.push(chart2)
  if (chart3) charts.push(chart3)
  if (chart4) charts.push(chart4)

  startRingAutoPlay()
})

onUnmounted(() => {
  stopRingAutoPlay()
  charts.forEach(chart => {
    if (chart && chart.dispose) chart.dispose()
  })
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
}
.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}
.chart-card {
  padding: 20px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}
.chart-container {
  width: 100%;
  height: 280px;
}

/* ========== 动态环形图样式 ========== */
.status-ring-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
}
.ring-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
  position: relative;
}
.ring-item:hover {
  transform: scale(1.05);
}
.ring-item.ring-active .ring-wrapper {
  animation: ringPulse 1s ease-in-out infinite;
}
.ring-wrapper {
  width: 100px;
  height: 100px;
  position: relative;
}
.ring-svg {
  width: 100%;
  height: 100%;
}
.ring-glow {
  transition: opacity 0.5s ease;
}
.ring-glow-active {
  opacity: 0.6 !important;
}
.ring-label {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}
.ring-item.ring-active .ring-label {
  transform: scale(1.1);
}

@keyframes ringPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.ring-status-indicator {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ECFDF5;
}
.status-dot-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.4;
}
.status-dot-indicator.active {
  opacity: 1;
  transform: scale(1.3);
}
.status-dot-indicator:hover {
  opacity: 0.8;
  transform: scale(1.2);
}

/* ========== 状态条形图样式 ========== */
.status-bar-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
}
.status-bar-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.status-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.status-bar-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1F2937;
}
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.status-bar-count {
  color: #6B7280;
  font-size: 13px;
}
.status-bar-track {
  height: 8px;
  background: #ECFDF5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}
.status-bar-fill {
  height: 100%;
  border-radius: 4px;
  position: relative;
  transition: width 1.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.status-bar-fill-animate {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, rgba(255,255,255,0.3), transparent);
  animation: shimmer 2s infinite;
}
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.status-bar-percent {
  font-size: 12px;
  color: #6B7280;
  text-align: right;
}

/* ========== 仪表盘样式 ========== */
.fee-gauge-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.gauge-wrapper {
  width: 200px;
  height: 120px;
}
.gauge-svg {
  width: 100%;
  height: 100%;
}
.gauge-legend {
  display: flex;
  gap: 24px;
}
.gauge-legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #1F2937;
}
.gauge-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.gauge-legend-value {
  color: #6B7280;
  font-size: 12px;
}

/* ========== 报修类型分布 - 横向条形图 ========== */
.repair-type-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 0;
}
.repair-type-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.repair-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.repair-type-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1F2937;
  font-weight: 500;
}
.repair-type-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
}
.repair-type-count {
  color: #6B7280;
  font-size: 13px;
  font-weight: 500;
}
.repair-type-track {
  height: 12px;
  background: #F3F4F6;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}
.repair-type-fill {
  height: 100%;
  border-radius: 6px;
  position: relative;
  transition: width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.repair-type-fill-animate {
  height: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(255,255,255,0.2), transparent);
  animation: repairShimmer 2.5s infinite;
}
@keyframes repairShimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.repair-type-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6B7280;
}
.repair-type-percent {
  font-weight: 600;
  color: #1F2937;
}
.repair-type-bar-value {
  color: #9CA3AF;
}
.repair-type-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #F3F4F6;
  font-size: 13px;
  color: #6B7280;
}
.repair-type-most {
  color: #1F2937;
  font-weight: 500;
  background: #F3F4F6;
  padding: 2px 12px;
  border-radius: 12px;
}

/* ========== 原有样式 ========== */
.empty {
  color: var(--color-text-secondary);
  text-align: center;
  padding: 24px;
}
</style>