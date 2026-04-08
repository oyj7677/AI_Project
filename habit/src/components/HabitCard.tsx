import { formatDateLabel } from '../lib/date'
import type { HabitWithMetrics } from '../hooks/useHabitTracker'

interface HabitCardProps {
  habit: HabitWithMetrics
  onToggleCompletion: (id: string) => Promise<void> | void
  onDelete: (id: string) => Promise<void> | void
}

function getFrequencyLabel(frequency: HabitWithMetrics['frequency']) {
  if (frequency === 'daily') {
    return '매일'
  }

  if (frequency === 'weekdays') {
    return '평일'
  }

  return '매주'
}

function getStatusCopy(habit: HabitWithMetrics) {
  if (habit.isCompleteToday) {
    return {
      className: '',
      label: '진행 중',
      hint:
        habit.frequency === 'weekly'
          ? '이번 주 체크인을 이미 완료했어요.'
          : habit.frequency === 'weekdays' && !habit.isScheduledToday
            ? '주말 전에 체크인을 마쳐 연속 기록이 유지되고 있어요.'
            : '오늘 체크인이 이미 연속 기록에 반영되었어요.',
    }
  }

  if (habit.isScheduledToday) {
    return {
      className: 'habit-card__status--pending',
      label: '지금 가능',
      hint:
        habit.frequency === 'weekly'
          ? '이번 주는 한 번만 체크해도 충분해요.'
          : '지금 체크하면 연속 기록을 이어갈 수 있어요.',
    }
  }

  return {
    className: 'habit-card__status--rest',
    label: '휴식 중',
    hint:
      habit.frequency === 'weekdays'
        ? '이 습관은 월요일부터 금요일까지만 진행돼요.'
        : '지금은 추가로 할 일이 없어요.',
  }
}

export function HabitCard({
  habit,
  onToggleCompletion,
  onDelete,
}: HabitCardProps) {
  const status = getStatusCopy(habit)
  const canToggle = habit.isScheduledToday || habit.isCompleteToday
  const lastCompletedOn = habit.completedDates[0]

  return (
    <li className="habit-card">
      <div className="habit-card__header">
        <div>
          <div className="habit-card__meta">
            <span className="tag">{habit.category}</span>
            <span className="tag tag--frequency">
              {getFrequencyLabel(habit.frequency)}
            </span>
            <span className={`habit-card__status ${status.className}`}>
              {status.label}
            </span>
          </div>
          <h3>{habit.title}</h3>
        </div>
      </div>

      <p className="habit-card__description">
        {habit.description || '아직 메모가 없어요. 작고 반복 가능한 습관으로 시작해 보세요.'}
      </p>

      <div className="habit-card__metrics">
        <div className="metric">
          <span className="metric__label">현재 연속</span>
          <span className="metric__value">{habit.currentStreak}</span>
        </div>
        <div className="metric">
          <span className="metric__label">최고 연속</span>
          <span className="metric__value">{habit.longestStreak}</span>
        </div>
        <div className="metric">
          <span className="metric__label">최근 7일</span>
          <span className="metric__value">{habit.completionsLast7Days}</span>
        </div>
        <div className="metric">
          <span className="metric__label">누적 체크인</span>
          <span className="metric__value">{habit.totalCompletions}</span>
        </div>
      </div>

      <div className="habit-card__footer">
        <p className="habit-card__hint">
          {status.hint}
          {lastCompletedOn ? ` 마지막 완료: ${formatDateLabel(lastCompletedOn)}.` : ''}
        </p>
        <div className="habit-card__actions">
          <button
            type="button"
            className={`habit-card__button ${habit.isCompleteToday ? 'habit-card__button--done' : ''}`}
            disabled={!canToggle}
            onClick={() => onToggleCompletion(habit.id)}
          >
            {habit.isCompleteToday ? '체크 취소' : '체크인'}
          </button>
          <button
            type="button"
            className="habit-card__delete"
            onClick={() => onDelete(habit.id)}
          >
            삭제
          </button>
        </div>
      </div>
    </li>
  )
}
