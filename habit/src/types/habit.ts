export const HABIT_FREQUENCIES = ['daily', 'weekdays', 'weekly'] as const

export const HABIT_CATEGORIES = [
  '건강',
  '집중',
  '운동',
  '학습',
  '생활',
  '마음관리',
] as const

export type HabitFrequency = (typeof HABIT_FREQUENCIES)[number]
export type HabitCategory = (typeof HABIT_CATEGORIES)[number]
export type ThemeMode = 'light' | 'dark'

export interface Habit {
  id: string
  title: string
  description: string
  category: string
  frequency: HabitFrequency
  createdAt: string
  completedDates: string[]
}

export interface HabitFormValues {
  title: string
  description: string
  category: string
  frequency: HabitFrequency
}

export interface CompletionEvent {
  habitId: string
  habitTitle: string
  category: string
  date: string
}

export interface DashboardStats {
  totalHabits: number
  scheduledToday: number
  onTrackToday: number
  bestCurrentStreak: number
  completionsLast7Days: number
  completionsLast30Days: number
  consistencyScore: number
}
