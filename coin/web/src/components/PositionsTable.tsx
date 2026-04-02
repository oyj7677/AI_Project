import { formatCurrency, formatPercent } from '../lib/format'

interface PositionsTableProps {
  positions: Array<{
    market: string
    quantity: number
    averageEntryPrice: number
    currentPrice: number
    marketValue: number
    unrealizedPnl: number
    unrealizedPnlRate: number
  }>
}

export function PositionsTable({ positions }: PositionsTableProps) {
  return (
    <section className="panel positions-panel">
      <header className="panel-header">
        <div>
          <p className="panel-eyebrow">Positions</p>
          <h2>보유 포지션</h2>
        </div>
      </header>

      {positions.length === 0 ? (
        <div className="empty-state">
          <p>아직 보유 중인 paper position이 없습니다.</p>
        </div>
      ) : (
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>종목</th>
                <th>수량</th>
                <th>평단</th>
                <th>현재가</th>
                <th>평가금액</th>
                <th>손익</th>
              </tr>
            </thead>
            <tbody>
              {positions.map((position) => (
                <tr key={position.market}>
                  <td>{position.market}</td>
                  <td>{position.quantity.toFixed(8)}</td>
                  <td>{formatCurrency(position.averageEntryPrice)}</td>
                  <td>{formatCurrency(position.currentPrice)}</td>
                  <td>{formatCurrency(position.marketValue)}</td>
                  <td>
                    {formatCurrency(position.unrealizedPnl)}
                    <br />
                    <span className="table-subtle">
                      {formatPercent(position.unrealizedPnlRate)}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
