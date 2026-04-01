type MetricTone = 'neutral' | 'rise' | 'fall'

interface MetricCardProps {
  label: string
  tone: MetricTone
  value: string
}

export function MetricCard({ label, tone, value }: MetricCardProps) {
  return (
    <section className={`metric-card ${tone}`}>
      <div className="metric-label">
        <span>{label}</span>
      </div>
      <div className="metric-value">{value}</div>
    </section>
  )
}
