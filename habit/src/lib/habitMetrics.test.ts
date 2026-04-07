import { describe, expect, it } from 'vitest'
import {
  getDashboardStats,
  getHabitCurrentStreak,
  getHabitLongestStreak,
  isHabitCompletedForDate,
  toggleHabitCompletion,
} from './habitMetrics'
import type { Habit } from '../types/habit'

function createHabit(overrides: Partial<Habit>): Habit {
  return {
    id: 'habit-1',
    title: 'Read for 10 minutes',
    description: '',
    category: 'Learning',
    frequency: 'daily',
    createdAt: '2026-04-01T00:00:00.000Z',
    completedDates: [],
    ...overrides,
  }
}

describe('habit metrics', () => {
  it('calculates a current daily streak from consecutive dates', () => {
    const habit = createHabit({
      completedDates: ['2026-04-05', '2026-04-06', '2026-04-07'],
    })

    expect(getHabitCurrentStreak(habit, new Date(2026, 3, 7))).toBe(3)
    expect(getHabitLongestStreak(habit)).toBe(3)
  })

  it('keeps weekday streaks alive across a weekend', () => {
    const habit = createHabit({
      frequency: 'weekdays',
      completedDates: ['2026-04-02', '2026-04-03'],
    })

    expect(getHabitCurrentStreak(habit, new Date(2026, 3, 4))).toBe(2)
    expect(isHabitCompletedForDate(habit, new Date(2026, 3, 4))).toBe(true)
  })

  it('treats weekly completions as one check-in per week', () => {
    const habit = createHabit({
      frequency: 'weekly',
      completedDates: ['2026-04-06'],
    })

    expect(isHabitCompletedForDate(habit, new Date(2026, 3, 8))).toBe(true)
    expect(toggleHabitCompletion(habit, new Date(2026, 3, 8)).completedDates).toEqual([])
  })

  it('summarizes the dashboard from scheduled and completed habits', () => {
    const habits = [
      createHabit({
        id: 'daily',
        completedDates: ['2026-04-07'],
      }),
      createHabit({
        id: 'weekday',
        frequency: 'weekdays',
        completedDates: ['2026-04-06'],
      }),
      createHabit({
        id: 'weekly',
        frequency: 'weekly',
        completedDates: ['2026-04-01'],
      }),
    ]

    expect(getDashboardStats(habits, new Date(2026, 3, 7))).toEqual({
      totalHabits: 3,
      scheduledToday: 3,
      onTrackToday: 1,
      bestCurrentStreak: 1,
      completionsLast7Days: 3,
      completionsLast30Days: 3,
      consistencyScore: 33,
    })
  })
})
