import { startTransition, useEffect, useState } from 'react'
import { formatDateLabel } from '../lib/date'
import { buildContributionHeatmap, type HeatmapData } from '../lib/heatmap'
import {
  getCompletionEvents,
  getDashboardStats,
  getHabitCompletionCountInDays,
  getHabitCurrentStreak,
  getHabitLongestStreak,
  isHabitCompletedForDate,
  isHabitScheduledForDate,
  toggleHabitCompletion,
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

function createHabit(values: HabitFormValues): Habit {
  return {
    id: crypto.randomUUID(),
    title: values.title.trim(),
    description: values.description.trim(),
    category: values.category,
    frequency: values.frequency,
    createdAt: new Date().toISOString(),
    completedDates: [],
  }
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
  const referenceDate = new Date()

  useEffect(() => {
    saveHabits(habits)
  }, [habits])

  useEffect(() => {
    saveTheme(theme)
    document.documentElement.dataset.theme = theme
  }, [theme])

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

  function addHabit(values: HabitFormValues) {
    startTransition(() => {
      setHabits((current) => [createHabit(values), ...current])
    })
  }

  function toggleHabitCompletionById(id: string) {
    startTransition(() => {
      setHabits((current) =>
        current.map((habit) =>
          habit.id === id ? toggleHabitCompletion(habit, referenceDate) : habit,
        ),
      )
    })
  }

  function deleteHabit(id: string) {
    startTransition(() => {
      setHabits((current) => current.filter((habit) => habit.id !== id))
    })
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
    addHabit,
    toggleHabitCompletion: toggleHabitCompletionById,
    deleteHabit,
    toggleTheme,
  }
}
