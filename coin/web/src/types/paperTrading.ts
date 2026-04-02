export interface PaperPosition {
  market: string
  quantity: number
  averageEntryPrice: number
  costBasis: number
  updatedAt: string
}

export interface PaperOrder {
  id: string
  market: string
  side: 'buy' | 'sell'
  orderType: 'market'
  requestedAt: string
  filledAt: string
  status: 'filled'
  fillPrice: number
  quantity: number
  grossAmount: number
  feeAmount: number
  netAmount: number
}

export interface PaperPortfolio {
  initialCashBalance: number
  cashBalance: number
  realizedPnl: number
  feeRate: number
  positions: PaperPosition[]
  updatedAt: string
}

export interface PaperTradingSnapshot {
  portfolio: PaperPortfolio
  orders: PaperOrder[]
}
