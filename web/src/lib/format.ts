export function formatCurrency(value: number) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0,
  }).format(value)
}

export function formatCompactCurrency(value: number) {
  return new Intl.NumberFormat('ko-KR', {
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value)
}

export function formatPercent(value: number) {
  const sign = value > 0 ? '+' : ''

  return `${sign}${(value * 100).toFixed(2)}%`
}

export function formatShortDateTime(value: string) {
  const date = new Date(value)

  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}
