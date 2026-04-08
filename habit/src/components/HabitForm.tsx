import { useState, type FormEvent } from 'react'
import {
  HABIT_CATEGORIES,
  HABIT_FREQUENCIES,
  type HabitFormValues,
} from '../types/habit'

interface HabitFormProps {
  onSubmit: (values: HabitFormValues) => Promise<void> | void
  wrapInPanel?: boolean
  showHeading?: boolean
  onCancel?: () => void
}

const initialValues: HabitFormValues = {
  title: '',
  description: '',
  category: HABIT_CATEGORIES[0],
  frequency: HABIT_FREQUENCIES[0],
}

export function HabitForm({
  onSubmit,
  wrapInPanel = true,
  showHeading = true,
  onCancel,
}: HabitFormProps) {
  const [values, setValues] = useState(initialValues)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField<Key extends keyof HabitFormValues>(
    key: Key,
    value: HabitFormValues[Key],
  ) {
    setValues((current) => ({
      ...current,
      [key]: value,
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!values.title.trim()) {
      setError('매일 한눈에 알아볼 수 있도록 짧은 제목을 입력해 주세요.')
      return
    }

    setIsSubmitting(true)
    setError('')

    try {
      await onSubmit(values)
      setValues((current) => ({
        ...initialValues,
        category: current.category,
        frequency: current.frequency,
      }))
    } catch (submitError) {
      setError(
        submitError instanceof Error
          ? submitError.message
          : '습관을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.',
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  const formContent = (
    <>
      {showHeading ? (
        <div>
          <p className="eyebrow">습관 만들기</p>
          <h2 id="habit-form-title">계속 이어가고 싶은 다음 루틴을 추가하세요.</h2>
        </div>
      ) : null}

      <form onSubmit={handleSubmit} className="habit-form">
        <div className="habit-form__field">
          <label htmlFor="habit-title">제목</label>
          <input
            id="habit-title"
            name="title"
            placeholder="점심 후 물 한 잔 마시기"
            value={values.title}
            disabled={isSubmitting}
            onChange={(event) => updateField('title', event.target.value)}
          />
        </div>

        <div className="habit-form__field">
          <label htmlFor="habit-description">설명</label>
          <textarea
            id="habit-description"
            name="description"
            placeholder="왜 중요한 습관인지, 완료 기준이 무엇인지 짧게 적어보세요."
            value={values.description}
            disabled={isSubmitting}
            onChange={(event) => updateField('description', event.target.value)}
          />
        </div>

        <div className="habit-form__row">
          <div className="habit-form__field">
            <label htmlFor="habit-category">카테고리</label>
            <select
              id="habit-category"
              name="category"
              value={values.category}
              disabled={isSubmitting}
              onChange={(event) => updateField('category', event.target.value)}
            >
              {HABIT_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="habit-form__field">
            <label htmlFor="habit-frequency">반복 주기</label>
            <select
              id="habit-frequency"
              name="frequency"
              value={values.frequency}
              disabled={isSubmitting}
              onChange={(event) =>
                updateField('frequency', event.target.value as HabitFormValues['frequency'])
              }
            >
              {HABIT_FREQUENCIES.map((frequency) => (
                <option key={frequency} value={frequency}>
                  {frequency === 'daily'
                    ? '매일'
                    : frequency === 'weekdays'
                      ? '평일'
                      : '매주'}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-actions">
          <div className="form-hint" role={error ? 'alert' : undefined}>
            {error || '반복 주기를 정한 뒤 대시보드에서 체크인을 쌓아 연속 기록을 만들어보세요.'}
          </div>
          <div className="form-actions__buttons">
            {onCancel ? (
              <button
                type="button"
                className="secondary-action"
                onClick={onCancel}
                disabled={isSubmitting}
              >
                취소
              </button>
            ) : null}
            <button type="submit" className="submit-button" disabled={isSubmitting}>
              {isSubmitting ? '저장 중...' : '습관 추가'}
            </button>
          </div>
        </div>
      </form>
    </>
  )

  if (!wrapInPanel) {
    return formContent
  }

  return (
    <section className="habit-form-panel" aria-labelledby="habit-form-title">
      {formContent}
    </section>
  )
}
