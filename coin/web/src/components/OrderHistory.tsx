import { formatCurrency } from '../lib/format'
import type { PaperOrder } from '../types/paperTrading'

interface OrderHistoryProps {
  orders: PaperOrder[]
}

export function OrderHistory({ orders }: OrderHistoryProps) {
  return (
    <section className="panel orders-panel">
      <header className="panel-header">
        <div>
          <p className="panel-eyebrow">History</p>
          <h2>주문 이력</h2>
        </div>
      </header>

      {orders.length === 0 ? (
        <div className="empty-state">
          <p>아직 체결된 paper order가 없습니다.</p>
        </div>
      ) : (
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>종목</th>
                <th>구분</th>
                <th>체결가</th>
                <th>수량</th>
                <th>금액</th>
                <th>시간</th>
              </tr>
            </thead>
            <tbody>
              {orders.slice(0, 12).map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.market}</td>
                  <td>{order.side === 'buy' ? '매수' : '매도'}</td>
                  <td>{formatCurrency(order.fillPrice)}</td>
                  <td>{order.quantity.toFixed(8)}</td>
                  <td>{formatCurrency(order.grossAmount)}</td>
                  <td>{new Date(order.filledAt).toLocaleString('ko-KR')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
