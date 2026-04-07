import type { HabitWithMetrics } from '../hooks/useHabitTracker'
import { HabitCard } from './HabitCard'

interface HabitListProps {
  habits: HabitWithMetrics[]
  onToggleCompletion: (id: string) => void
  onDelete: (id: string) => void
  onCreateHabit?: () => void
}

export function HabitList({
  habits,
  onToggleCompletion,
  onDelete,
  onCreateHabit,
}: HabitListProps) {
  if (habits.length === 0) {
    return (
      <div className="empty-state">
        <h3>아직 습관이 없어요</h3>
        <p>
          첫 습관을 하나 추가하면 이 보드에 오늘의 루틴과 연속 기록이 바로
          표시됩니다.
        </p>
        {onCreateHabit ? (
          <button type="button" className="submit-button" onClick={onCreateHabit}>
            첫 습관 만들기
          </button>
        ) : null}
      </div>
    )
  }

  return (
    <ul className="habit-list">
      {habits.map((habit) => (
        <HabitCard
          key={habit.id}
          habit={habit}
          onToggleCompletion={onToggleCompletion}
          onDelete={onDelete}
        />
      ))}
    </ul>
  )
}
