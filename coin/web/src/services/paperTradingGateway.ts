import type { PaperTradingGateway } from '../contracts/paperTrading'
import {
  fetchPaperOrders,
  fetchPaperPortfolio,
  resetPaperPortfolio,
  submitPaperOrder,
} from '../lib/paperTradingApi'

export const paperTradingGateway: PaperTradingGateway = {
  fetchPortfolio: fetchPaperPortfolio,
  fetchOrders: fetchPaperOrders,
  submitOrder: submitPaperOrder,
  resetPortfolio: resetPaperPortfolio,
}
