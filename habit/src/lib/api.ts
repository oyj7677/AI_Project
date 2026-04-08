import type { Habit, HabitFormValues } from '../types/habit'
import { compareDateKeysDesc } from './date'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(
  /\/$/,
  '',
)

interface ApiErrorResponse {
  message?: string
}

function normalizeHabit(habit: Habit): Habit {
  return {
    ...habit,
    completedDates: Array.from(new Set(habit.completedDates)).sort(compareDateKeysDesc),
  }
}

async function readErrorMessage(response: Response) {
  try {
    const payload = (await response.json()) as ApiErrorResponse
    if (payload.message) {
      return payload.message
    }
  } catch {
    // Fall back to the HTTP status below when the payload is empty or non-JSON.
  }

  return `${response.status} ${response.statusText}`.trim()
}

async function request<T>(path: string, init?: RequestInit) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export async function fetchHabits() {
  const habits = await request<Habit[]>('/habits')
  return habits.map(normalizeHabit)
}

export async function createHabit(values: HabitFormValues) {
  const habit = await request<Habit>('/habits', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(values),
  })

  return normalizeHabit(habit)
}

export async function importHabits(habits: Habit[]) {
  const imported = await request<Habit[]>('/habits/import', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ habits }),
  })

  return imported.map(normalizeHabit)
}

export async function toggleHabitCompletion(id: string, date: string) {
  const habit = await request<Habit>(`/habits/${id}/completion`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ date }),
  })

  return normalizeHabit(habit)
}

export async function deleteHabit(id: string) {
  await request<void>(`/habits/${id}`, {
    method: 'DELETE',
  })
}
