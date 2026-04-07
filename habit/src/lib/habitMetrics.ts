import type {
  CompletionEvent,
  DashboardStats,
  Habit,
  HabitFrequency,
} from '../types/habit'
import {
  addDays,
  compareDateKeysDesc,
  getLocalDateKey,
  getPreviousWeekdayDateKey,
  getWeekStartDateKey,
  isWeekdayDate,
  parseDateKey,
  subtractDays,
} from './date'

function getPeriodKey(dateKey: string, frequency: HabitFrequency) {
  if (frequency === 'weekly') {
    return getWeekStartDateKey(dateKey)
  }

  return dateKey
}

function getReferencePeriodKey(frequency: HabitFrequency, referenceDate = new Date()) {
  const today = getLocalDateKey(referenceDate)

  if (frequency === 'weekly') {
    return getWeekStartDateKey(today)
  }

  if (frequency === 'weekdays' && !isWeekdayDate(referenceDate)) {
    return getPreviousWeekdayDateKey(today)
  }

  return today
}

function getPreviousPeriodKey(periodKey: string, frequency: HabitFrequency) {
  if (frequency === 'daily') {
    return subtractDays(periodKey, 1)
  }

  if (frequency === 'weekdays') {
    return getPreviousWeekdayDateKey(periodKey)
  }

  return subtractDays(periodKey, 7)
}

function getNextPeriodKey(periodKey: string, frequency: HabitFrequency) {
  if (frequency === 'daily') {
    return addDays(periodKey, 1)
  }

  if (frequency === 'weekdays') {
    let current = addDays(periodKey, 1)
    while (!isWeekdayDate(parseDateKey(current))) {
      current = addDays(current, 1)
    }
    return current
  }

  return addDays(periodKey, 7)
}

function getCompletedPeriodKeys(habit: Habit) {
  return Array.from(
    new Set(habit.completedDates.map((dateKey) => getPeriodKey(dateKey, habit.frequency))),
  ).sort((left, right) => parseDateKey(left).getTime() - parseDateKey(right).getTime())
}

export function isHabitScheduledForDate(habit: Habit, referenceDate = new Date()) {
  if (habit.frequency === 'weekdays') {
    return isWeekdayDate(referenceDate)
  }

  return true
}

export function isHabitCompletedForDate(habit: Habit, referenceDate = new Date()) {
  const referencePeriod = getReferencePeriodKey(habit.frequency, referenceDate)
  const completedPeriods = new Set(getCompletedPeriodKeys(habit))
  return completedPeriods.has(referencePeriod)
}

export function toggleHabitCompletion(habit: Habit, referenceDate = new Date()) {
  const today = getLocalDateKey(referenceDate)
  const referencePeriod = getReferencePeriodKey(habit.frequency, referenceDate)
  const matchingDate = habit.completedDates.find(
    (dateKey) => getPeriodKey(dateKey, habit.frequency) === referencePeriod,
  )

  const nextDates = matchingDate
    ? habit.completedDates.filter((dateKey) => dateKey !== matchingDate)
    : [...habit.completedDates, today]

  return {
    ...habit,
    completedDates: Array.from(new Set(nextDates)).sort(compareDateKeysDesc),
  }
}

export function getHabitCurrentStreak(habit: Habit, referenceDate = new Date()) {
  const completedPeriods = new Set(getCompletedPeriodKeys(habit))
  let streak = 0
  let cursor = getReferencePeriodKey(habit.frequency, referenceDate)

  while (completedPeriods.has(cursor)) {
    streak += 1
    cursor = getPreviousPeriodKey(cursor, habit.frequency)
  }

  return streak
}

export function getHabitLongestStreak(habit: Habit) {
  const completedPeriods = getCompletedPeriodKeys(habit)

  if (completedPeriods.length === 0) {
    return 0
  }

  let longest = 1
  let current = 1

  for (let index = 1; index < completedPeriods.length; index += 1) {
    if (
      completedPeriods[index] ===
      getNextPeriodKey(completedPeriods[index - 1], habit.frequency)
    ) {
      current += 1
    } else {
      current = 1
    }

    if (current > longest) {
      longest = current
    }
  }

  return longest
}

export function getHabitCompletionCountInDays(
  habit: Habit,
  numberOfDays: number,
  referenceDate = new Date(),
) {
  const endDate = getLocalDateKey(referenceDate)
  const startDate = subtractDays(endDate, numberOfDays - 1)
  const startTime = parseDateKey(startDate).getTime()
  const endTime = parseDateKey(endDate).getTime()

  return habit.completedDates.filter((dateKey) => {
    const time = parseDateKey(dateKey).getTime()
    return time >= startTime && time <= endTime
  }).length
}

export function getCompletionEvents(habits: Habit[]): CompletionEvent[] {
  return habits
    .flatMap((habit) =>
      habit.completedDates.map((date) => ({
        habitId: habit.id,
        habitTitle: habit.title,
        category: habit.category,
        date,
      })),
    )
    .sort((left, right) => {
      const dateSort = compareDateKeysDesc(left.date, right.date)
      if (dateSort !== 0) {
        return dateSort
      }

      return left.habitTitle.localeCompare(right.habitTitle)
    })
}

export function getDashboardStats(
  habits: Habit[],
  referenceDate = new Date(),
): DashboardStats {
  const scheduledToday = habits.filter((habit) =>
    isHabitScheduledForDate(habit, referenceDate),
  )
  const onTrackToday = scheduledToday.filter((habit) =>
    isHabitCompletedForDate(habit, referenceDate),
  )
  const bestCurrentStreak = habits.reduce(
    (best, habit) => Math.max(best, getHabitCurrentStreak(habit, referenceDate)),
    0,
  )
  const completionsLast7Days = habits.reduce(
    (count, habit) => count + getHabitCompletionCountInDays(habit, 7, referenceDate),
    0,
  )
  const completionsLast30Days = habits.reduce(
    (count, habit) => count + getHabitCompletionCountInDays(habit, 30, referenceDate),
    0,
  )

  return {
    totalHabits: habits.length,
    scheduledToday: scheduledToday.length,
    onTrackToday: onTrackToday.length,
    bestCurrentStreak,
    completionsLast7Days,
    completionsLast30Days,
    consistencyScore:
      scheduledToday.length === 0
        ? 0
        : Math.round((onTrackToday.length / scheduledToday.length) * 100),
  }
}
