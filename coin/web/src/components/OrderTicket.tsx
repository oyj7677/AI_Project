import { useMemo, useState } from 'react'
import { formatCurrency } from '../lib/format'
import type { DashboardMarket } from '../types/market'
import type { PaperPosition } from '../types/paperTrading'

interface OrderTicketProps {
  isSubmitting: boolean
  selectedMarket: DashboardMarket | null
  selectedPosition: PaperPosition | null
  onBuy: (amountKrw: number) => Promise<void>
  onSell: (quantity: number) => Promise<void>
  onReset: () => Promise<void>
}

export function OrderTicket({
  isSubmitting,
  onBuy,
  onReset,
  onSell,
  selectedMarket,
  selectedPosition,
}: OrderTicketProps) {
  const [side, setSide] = useState<'buy' | 'sell'>('buy')
  const [amountKrw, setAmountKrw] = useState('100000')
  const [quantity, setQuantity] = useState('')
  const estimatedQuantity = useMemo(() => {
    if (!selectedMarket || side !== 'buy') {
      return 0
    }
    return Number(amountKrw || '0') / selectedMarket.tradePrice
  }, [amountKrw, selectedMarket, side])

  return (
    <section className="panel trade-panel">
      <header className="panel-header">
        <div>
          <p className="panel-eyebrow">Paper Trading</p>
          <h2>주문 티켓</h2>
        </div>
        <button className="ghost-button" onClick={() => void onReset()} type="button">
          리셋
        </button>
      </header>

      <div className="trade-side-toggle" role="tablist" aria-label="주문 방향 선택">
        {(['buy', 'sell'] as const).map((value) => (
          <button
            aria-pressed={side === value}
            className={`trade-side-button ${side === value ? 'active' : ''}`}
            key={value}
            onClick={() => setSide(value)}
            type="button"
          >
            {value === 'buy' ? '매수' : '매도'}
          </button>
        ))}
      </div>

      <div className="trade-form">
        <div className="trade-field">
          <label>선택 종목</label>
          <div className="trade-static">
            {selectedMarket
              ? `${selectedMarket.koreanName} (${selectedMarket.market})`
              : '종목을 먼저 선택해 주세요'}
          </div>
        </div>

        <div className="trade-field">
          <label>현재가</label>
          <div className="trade-static">
            {selectedMarket ? formatCurrency(selectedMarket.tradePrice) : '-'}
          </div>
        </div>

        {side === 'buy' ? (
          <>
            <div className="trade-field">
              <label htmlFor="buy-amount">주문 금액 (KRW)</label>
              <input
                id="buy-amount"
                min="1000"
                onChange={(event) => setAmountKrw(event.target.value)}
                step="1000"
                type="number"
                value={amountKrw}
              />
            </div>
            <div className="trade-helper">
              예상 수량 {estimatedQuantity > 0 ? estimatedQuantity.toFixed(8) : '-'}
            </div>
            <button
              className="submit-order buy"
              disabled={!selectedMarket || isSubmitting || Number(amountKrw) <= 0}
              onClick={() => void onBuy(Number(amountKrw))}
              type="button"
            >
              시장가 매수
            </button>
          </>
        ) : (
          <>
            <div className="trade-field">
              <label htmlFor="sell-quantity">매도 수량</label>
              <input
                id="sell-quantity"
                min="0"
                onChange={(event) => setQuantity(event.target.value)}
                step="0.00000001"
                type="number"
                value={quantity}
              />
            </div>
            <div className="trade-helper">
              보유 수량 {selectedPosition ? selectedPosition.quantity.toFixed(8) : '0'}
            </div>
            <button
              className="submit-order sell"
              disabled={
                !selectedMarket ||
                !selectedPosition ||
                isSubmitting ||
                Number(quantity) <= 0
              }
              onClick={() => void onSell(Number(quantity))}
              type="button"
            >
              시장가 매도
            </button>
          </>
        )}
      </div>
    </section>
  )
}
