import type { MarketDataGateway } from '../contracts/marketData'
import { fetchMarketChart, fetchMarkets } from '../lib/marketApi'

export const marketDataGateway: MarketDataGateway = {
  fetchMarkets,
  fetchChartPoints: fetchMarketChart,
}
