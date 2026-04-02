import { formatCompactCurrency, formatCurrency, formatPercent } from '../lib/format'

interface PaperTradingSummaryProps {
  summary: {
    cashBalance: number
    initialCashBalance: number
    marketValue: number
    realizedPnl: number
    totalEquity: number
    unrealizedPnl: number
    updatedAt: string
  } | null
}

export function PaperTradingSummary({ summary }: PaperTradingSummaryProps) {
  if (!summary) {
    return null
  }

  const totalPnl = summary.realizedPnl + summary.unrealizedPnl
  const totalPnlRate =
    summary.initialCashBalance > 0 ? totalPnl / summary.initialCashBalance : 0

  return (
    <section className="paper-summary-grid">
      <article className="paper-summary-card">
        <span>현금 잔고</span>
        <strong>{formatCurrency(summary.cashBalance)}</strong>
      </article>
      <article className="paper-summary-card">
        <span>평가 자산</span>
        <strong>{formatCompactCurrency(summary.totalEquity)}</strong>
      </article>
      <article className="paper-summary-card">
        <span>미실현 손익</span>
        <strong>{formatCurrency(summary.unrealizedPnl)}</strong>
      </article>
      <article className="paper-summary-card">
        <span>총 손익률</span>
        <strong>{formatPercent(totalPnlRate)}</strong>
      </article>
    </section>
  )
}
