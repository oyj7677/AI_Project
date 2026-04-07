const DAY_MS = 1000 * 60 * 60 * 24

export function getLocalDateKey(date = new Date()) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function parseDateKey(dateKey: string) {
  const [year, month, day] = dateKey.split('-').map(Number)
  return new Date(year, (month || 1) - 1, day || 1)
}

export function addDays(dateKey: string, amount: number) {
  return getLocalDateKey(new Date(parseDateKey(dateKey).getTime() + amount * DAY_MS))
}

export function subtractDays(dateKey: string, amount: number) {
  return addDays(dateKey, -amount)
}

export function compareDateKeysDesc(left: string, right: string) {
  return parseDateKey(right).getTime() - parseDateKey(left).getTime()
}

export function isWeekdayDate(date: Date) {
  const weekday = date.getDay()
  return weekday >= 1 && weekday <= 5
}

export function getWeekStartDateKey(dateOrKey: Date | string) {
  const baseDate =
    typeof dateOrKey === 'string' ? parseDateKey(dateOrKey) : new Date(dateOrKey)

  const date = new Date(
    baseDate.getFullYear(),
    baseDate.getMonth(),
    baseDate.getDate(),
  )
  const day = date.getDay()
  const offset = day === 0 ? -6 : 1 - day
  date.setDate(date.getDate() + offset)
  return getLocalDateKey(date)
}

export function getPreviousWeekdayDateKey(dateKey: string) {
  let current = subtractDays(dateKey, 1)

  while (!isWeekdayDate(parseDateKey(current))) {
    current = subtractDays(current, 1)
  }

  return current
}

export function formatDateLabel(dateKey: string, referenceDate = new Date()) {
  const today = getLocalDateKey(referenceDate)
  const yesterday = subtractDays(today, 1)

  if (dateKey === today) {
    return '오늘'
  }

  if (dateKey === yesterday) {
    return '어제'
  }

  const formatter = new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    year:
      parseDateKey(dateKey).getFullYear() === referenceDate.getFullYear()
        ? undefined
        : 'numeric',
  })

  return formatter.format(parseDateKey(dateKey))
}
