import type { MarketDataGateway } from '../contracts/marketData'
import { fetchChartPoints, fetchDashboardMarkets } from '../lib/upbit'

export const marketDataGateway: MarketDataGateway = {
  fetchMarkets: fetchDashboardMarkets,
  fetchChartPoints,
}
