export type ChartRange = '1H' | '4H' | '1D' | '1W'

export interface DashboardMarket {
  market: string
  koreanName: string
  englishName: string
  tradePrice: number
  signedChangeRate: number
  signedChangePrice: number
  accTradePrice24h: number
  highPrice: number
  lowPrice: number
}

export interface ChartPoint {
  timestamp: number
  close: number
  open: number
  high: number
  low: number
  volume: number
}
