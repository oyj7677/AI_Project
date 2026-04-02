import type { PaperOrder, PaperPortfolio } from '../types/paperTrading'

export interface SubmitPaperOrderInput {
  market: string
  side: 'buy' | 'sell'
  amountKrw?: number
  quantity?: number
}

export interface PaperTradingGateway {
  fetchPortfolio(): Promise<PaperPortfolio>
  fetchOrders(): Promise<PaperOrder[]>
  submitOrder(input: SubmitPaperOrderInput): Promise<{
    order: PaperOrder
    portfolio: PaperPortfolio
  }>
  resetPortfolio(): Promise<void>
}
