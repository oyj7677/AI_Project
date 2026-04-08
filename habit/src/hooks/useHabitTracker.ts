import { startTransition, useEffect, useEffectEvent, useState } from 'react'
import { formatDateLabel, getLocalDateKey } from '../lib/date'
import {
  createHabit as createHabitRequest,
  deleteHabit as deleteHabitRequest,
  fetchHabits,
  importHabits as importHabitsRequest,
  toggleHabitCompletion as toggleHabitCompletionRequest,
} from '../lib/api'
import { buildContributionHeatmap, type HeatmapData } from '../lib/heatmap'
import {
  getCompletionEvents,
  getDashboardStats,
  getHabitCompletionCountInDays,
  getHabitCurrentStreak,
  getHabitLongestStreak,
  isHabitCompletedForDate,
  isHabitScheduledForDate,
} from '../lib/habitMetrics'
import { loadHabits, loadTheme, saveHabits, saveTheme } from '../lib/storage'
import type {
  CompletionEvent,
  DashboardStats,
  Habit,
  HabitFormValues,
  ThemeMode,
} from '../types/habit'

export interface HabitWithMetrics extends Habit {
  isScheduledToday: boolean
  isCompleteToday: boolean
  currentStreak: number
  longestStreak: number
  completionsLast7Days: number
  completionsLast30Days: number
  totalCompletions: number
}

export interface ActivityEntry extends CompletionEvent {
  dateLabel: string
}

export interface CategorySummary {
  label: string
  habitCount: number
  onTrackCount: number
}

export interface SyncBanner {
  title: string
  message: string
  tone: 'neutral' | 'success' | 'warning'
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && error.message) {
    return `${fallback} ${error.message}`
  }

  return fallback
}

function sortHabits(left: HabitWithMetrics, right: HabitWithMetrics) {
  if (left.isScheduledToday !== right.isScheduledToday) {
    return Number(right.isScheduledToday) - Number(left.isScheduledToday)
  }

  if (left.isCompleteToday !== right.isCompleteToday) {
    return Number(left.isCompleteToday) - Number(right.isCompleteToday)
  }

  if (left.currentStreak !== right.currentStreak) {
    return right.currentStreak - left.currentStreak
  }

  return left.title.localeCompare(right.title)
}

function sortCategories(left: CategorySummary, right: CategorySummary) {
  if (left.onTrackCount !== right.onTrackCount) {
    return right.onTrackCount - left.onTrackCount
  }

  if (left.habitCount !== right.habitCount) {
    return right.habitCount - left.habitCount
  }

  return left.label.localeCompare(right.label)
}

export function useHabitTracker() {
  const [habits, setHabits] = useState<Habit[]>(() => loadHabits())
  const [theme, setTheme] = useState<ThemeMode>(() => loadTheme())
  const [isLoading, setIsLoading] = useState(true)
  const [syncError, setSyncError] = useState<string | null>(null)
  const [syncInfo, setSyncInfo] = useState<string | null>(null)
  const referenceDate = new Date()

  useEffect(() => {
    saveHabits(habits)
  }, [habits])

  useEffect(() => {
    saveTheme(theme)
    document.documentElement.dataset.theme = theme
  }, [theme])

  const syncHabits = useEffectEvent(async () => {
    setIsLoading(true)
    setSyncError(null)

    try {
      const remoteHabits = await fetchHabits()
      const cachedHabits = loadHabits()
      const shouldImportCachedHabits =
        remoteHabits.length === 0 && cachedHabits.length > 0

      const nextHabits = shouldImportCachedHabits
        ? await importHabitsRequest(cachedHabits)
        : remoteHabits

      startTransition(() => {
        setHabits(nextHabits)
      })
      setSyncInfo(
        shouldImportCachedHabits
          ? '브라우저에 저장돼 있던 기존 습관을 새 Spring 서버로 옮겼습니다.'
          : null,
      )
    } catch (error) {
      setSyncError(
        getErrorMessage(
          error,
          'Spring 서버에 연결할 수 없어 브라우저에 남아 있는 마지막 데이터를 보여주고 있습니다.',
        ),
      )
    } finally {
      setIsLoading(false)
    }
  })

  useEffect(() => {
    void syncHabits()
  }, [])

  const habitCards = habits
    .map((habit) => ({
      ...habit,
      isScheduledToday: isHabitScheduledForDate(habit, referenceDate),
      isCompleteToday: isHabitCompletedForDate(habit, referenceDate),
      currentStreak: getHabitCurrentStreak(habit, referenceDate),
      longestStreak: getHabitLongestStreak(habit),
      completionsLast7Days: getHabitCompletionCountInDays(habit, 7, referenceDate),
      completionsLast30Days: getHabitCompletionCountInDays(
        habit,
        30,
        referenceDate,
      ),
      totalCompletions: habit.completedDates.length,
    }))
    .sort(sortHabits)

  const dashboardStats: DashboardStats = getDashboardStats(habits, referenceDate)
  const completionEvents = getCompletionEvents(habits)
  const heatmap: HeatmapData = buildContributionHeatmap(
    completionEvents.map((entry) => ({ date: entry.date })),
    { referenceDate, weeks: 18 },
  )
  const recentActivity: ActivityEntry[] = completionEvents
    .slice(0, 8)
    .map((entry) => ({
      ...entry,
      dateLabel: formatDateLabel(entry.date, referenceDate),
    }))

  const streakLeader =
    habitCards.find((habit) => habit.currentStreak > 0) ?? habitCards[0] ?? null

  const categorySummary = Array.from(
    habits.reduce((summary, habit) => {
      const current = summary.get(habit.category) ?? {
        label: habit.category,
        habitCount: 0,
        onTrackCount: 0,
      }

      current.habitCount += 1
      if (isHabitCompletedForDate(habit, referenceDate)) {
        current.onTrackCount += 1
      }

      summary.set(habit.category, current)
      return summary
    }, new Map<string, CategorySummary>()),
  )
    .map(([, value]) => value)
    .sort(sortCategories)

  const consistencyLabel =
    dashboardStats.totalHabits === 0
      ? '습관을 추가하면 꾸준함 지표가 여기에 표시돼요.'
      : `현재 전체 계획의 꾸준함은 ${dashboardStats.consistencyScore}%예요`

  const syncBanner: SyncBanner | null = isLoading
    ? {
        title: 'Spring 서버 연결 중',
        message: '습관 데이터를 불러오고 있습니다.',
        tone: 'neutral',
      }
    : syncError
      ? {
          title: '서버 연결 확인 필요',
          message: syncError,
          tone: 'warning',
        }
      : syncInfo
        ? {
            title: '데이터 이전 완료',
            message: syncInfo,
            tone: 'success',
          }
        : null

  async function addHabit(values: HabitFormValues) {
    try {
      const createdHabit = await createHabitRequest(values)

      setSyncError(null)
      setSyncInfo(null)
      startTransition(() => {
        setHabits((current) => [createdHabit, ...current])
      })
    } catch (error) {
      const message = getErrorMessage(
        error,
        '새 습관을 서버에 저장하지 못했습니다.',
      )
      setSyncError(message)
      throw new Error(message)
    }
  }

  async function toggleHabitCompletionById(id: string) {
    try {
      const updatedHabit = await toggleHabitCompletionRequest(
        id,
        getLocalDateKey(new Date()),
      )

      setSyncError(null)
      startTransition(() => {
        setHabits((current) =>
          current.map((habit) => (habit.id === id ? updatedHabit : habit)),
        )
      })
    } catch (error) {
      setSyncError(
        getErrorMessage(error, '체크인 변경 사항을 서버에 반영하지 못했습니다.'),
      )
    }
  }

  async function deleteHabit(id: string) {
    try {
      await deleteHabitRequest(id)

      setSyncError(null)
      startTransition(() => {
        setHabits((current) => current.filter((habit) => habit.id !== id))
      })
    } catch (error) {
      setSyncError(
        getErrorMessage(error, '습관 삭제 요청을 서버에 반영하지 못했습니다.'),
      )
    }
  }

  function toggleTheme() {
    startTransition(() => {
      setTheme((current) => (current === 'light' ? 'dark' : 'light'))
    })
  }

  return {
    theme,
    habits: habitCards,
    dashboardStats,
    recentActivity,
    heatmap,
    categorySummary,
    streakLeader,
    consistencyLabel,
    syncBanner,
    addHabit,
    toggleHabitCompletion: toggleHabitCompletionById,
    deleteHabit,
    toggleTheme,
  }
}
