interface StatCardProps {
  label: string
  value: string
  helper: string
  tone?: 'default' | 'accent'
}

export function StatCard({
  label,
  value,
  helper,
  tone = 'default',
}: StatCardProps) {
  return (
    <article className={`stat-card ${tone === 'accent' ? 'stat-card--accent' : ''}`}>
      <span className="stat-card__label">{label}</span>
      <strong className="stat-card__value">{value}</strong>
      <p className="stat-card__helper">{helper}</p>
    </article>
  )
}
