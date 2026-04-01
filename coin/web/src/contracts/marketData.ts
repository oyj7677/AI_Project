import type { ChartPoint, ChartRange, DashboardMarket } from '../types/market'

export interface MarketDataGateway {
  fetchMarkets(): Promise<DashboardMarket[]>
  fetchChartPoints(market: string, range: ChartRange): Promise<ChartPoint[]>
}
