import { addDays, getLocalDateKey, getWeekStartDateKey, parseDateKey } from './date'

export interface HeatmapCell {
  date: string
  count: number
  intensity: 0 | 1 | 2 | 3 | 4
  level: 0 | 1 | 2 | 3 | 4
  isFuture: boolean
  isToday: boolean
}

export interface HeatmapWeek {
  index: number
  weekStart: string
  cells: HeatmapCell[]
}

export interface HeatmapData {
  weeks: HeatmapWeek[]
  totalCompletions: number
  totalCount: number
  maxCount: number
  streakDaysActive: number
  activeDays: number
  monthLabels: string[]
  bestDay: {
    date: string
    count: number
  } | null
}

export type ContributionHeatmap = HeatmapData

interface HeatmapEvent {
  date: string
}

interface BuildContributionHeatmapOptions {
  weeks?: number
  referenceDate?: Date
}

function getIntensity(count: number): HeatmapCell['intensity'] {
  if (count >= 4) {
    return 4
  }

  if (count >= 3) {
    return 3
  }

  if (count >= 2) {
    return 2
  }

  if (count >= 1) {
    return 1
  }

  return 0
}

function buildCounts(events: HeatmapEvent[]) {
  return events.reduce(
    (map, event) => {
      map.set(event.date, (map.get(event.date) ?? 0) + 1)
      return map
    },
    new Map<string, number>(),
  )
}

function getMonthLabels(firstWeekStart: string, weeks: number) {
  const formatter = new Intl.DateTimeFormat('ko-KR', { month: 'short' })

  return Array.from({ length: weeks }, (_, weekIndex) => {
    const columnStart = addDays(firstWeekStart, weekIndex * 7)
    const previousStart = addDays(columnStart, -7)

    return weekIndex === 0 ||
      parseDateKey(columnStart).getMonth() !== parseDateKey(previousStart).getMonth()
      ? formatter.format(parseDateKey(columnStart))
      : ''
  })
}

export function buildContributionHeatmap(
  events: HeatmapEvent[],
  options: BuildContributionHeatmapOptions = {},
): HeatmapData {
  const weeks = Math.max(1, options.weeks ?? 18)
  const referenceDate = options.referenceDate ?? new Date()
  const today = getLocalDateKey(referenceDate)
  const currentWeekStart = getWeekStartDateKey(today)
  const firstWeekStart = addDays(currentWeekStart, -(weeks - 1) * 7)
  const counts = buildCounts(events)
  const todayTime = parseDateKey(today).getTime()
  let bestDay: HeatmapData['bestDay'] = null

  const heatmapWeeks = Array.from({ length: weeks }, (_, weekIndex) => {
    const start = addDays(firstWeekStart, weekIndex * 7)
    const cells = Array.from({ length: 7 }, (_, dayIndex) => {
      const date = addDays(start, dayIndex)
      const count = counts.get(date) ?? 0
      const time = parseDateKey(date).getTime()

      return {
        date,
        count,
        intensity: getIntensity(count),
        level: getIntensity(count),
        isFuture: time > todayTime,
        isToday: date === today,
      } satisfies HeatmapCell
    })

    cells.forEach((cell) => {
      if (!cell.isFuture && cell.count > 0) {
        if (!bestDay || cell.count > bestDay.count) {
          bestDay = {
            date: cell.date,
            count: cell.count,
          }
        }
      }
    })

    return {
      index: weekIndex,
      weekStart: start,
      cells,
    } satisfies HeatmapWeek
  })

  const visibleCounts = heatmapWeeks.flatMap((week) =>
    week.cells.filter((cell) => !cell.isFuture).map((cell) => cell.count),
  )
  const totalCompletions = visibleCounts.reduce((sum, count) => sum + count, 0)
  const activeDays = visibleCounts.filter((count) => count > 0).length
  const maxCount = Math.max(0, ...visibleCounts)
  const streakDaysActive = activeDays

  return {
    weeks: heatmapWeeks,
    totalCompletions,
    totalCount: totalCompletions,
    maxCount,
    streakDaysActive,
    activeDays,
    monthLabels: getMonthLabels(firstWeekStart, weeks),
    bestDay,
  }
}
