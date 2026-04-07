import { compareDateKeysDesc } from './date'
import type { Habit, ThemeMode } from '../types/habit'
import { HABIT_FREQUENCIES } from '../types/habit'

const HABITS_STORAGE_KEY = 'rhythm-habit-studio:habits'
const THEME_STORAGE_KEY = 'rhythm-habit-studio:theme'
const LEGACY_CATEGORY_MAP: Record<string, string> = {
  Health: '건강',
  Focus: '집중',
  Fitness: '운동',
  Learning: '학습',
  Home: '생활',
  Mindset: '마음관리',
}

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function isHabitFrequency(value: unknown): value is Habit['frequency'] {
  return typeof value === 'string' && HABIT_FREQUENCIES.includes(value as Habit['frequency'])
}

function isHabit(value: unknown): value is Habit {
  if (!value || typeof value !== 'object') {
    return false
  }

  const candidate = value as Record<string, unknown>
  return (
    typeof candidate.id === 'string' &&
    typeof candidate.title === 'string' &&
    typeof candidate.description === 'string' &&
    typeof candidate.category === 'string' &&
    isHabitFrequency(candidate.frequency) &&
    typeof candidate.createdAt === 'string' &&
    Array.isArray(candidate.completedDates) &&
    candidate.completedDates.every((entry) => typeof entry === 'string')
  )
}

function normalizeHabit(habit: Habit): Habit {
  return {
    ...habit,
    category: LEGACY_CATEGORY_MAP[habit.category] ?? habit.category,
    completedDates: Array.from(new Set(habit.completedDates)).sort(
      compareDateKeysDesc,
    ),
  }
}

export function loadHabits() {
  if (!canUseStorage()) {
    return []
  }

  try {
    const stored = window.localStorage.getItem(HABITS_STORAGE_KEY)
    if (!stored) {
      return []
    }

    const parsed: unknown = JSON.parse(stored)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed.filter(isHabit).map(normalizeHabit)
  } catch {
    return []
  }
}

export function saveHabits(habits: Habit[]) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(HABITS_STORAGE_KEY, JSON.stringify(habits))
}

export function loadTheme(): ThemeMode {
  if (!canUseStorage()) {
    return 'light'
  }

  const stored = window.localStorage.getItem(THEME_STORAGE_KEY)
  if (stored === 'dark' || stored === 'light') {
    return stored
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches
    ? 'dark'
    : 'light'
}

export function saveTheme(theme: ThemeMode) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(THEME_STORAGE_KEY, theme)
}
