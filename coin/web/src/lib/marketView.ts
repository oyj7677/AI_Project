import type { ChartPoint, DashboardMarket, MarketListItem } from '../types/market'

const DEFAULT_MARKET_CODE = 'KRW-BTC'

export interface ChartSummary {
  changeAmount: number
  changeRate: number
  endTimestamp: number
  latestClose: number
  max: number
  min: number
  pointCount: number
  startTimestamp: number
}

export function filterMarkets(markets: MarketListItem[], query: string) {
  const normalizedQuery = query.trim().toLowerCase()
  if (!normalizedQuery) {
    return markets
  }

  return markets.filter((market) =>
    [market.market, market.koreanName, market.englishName].some((value) =>
      value.toLowerCase().includes(normalizedQuery),
    ),
  )
}

export function resolveSelectedMarketCode(
  markets: DashboardMarket[],
  currentMarketCode: string | null,
) {
  if (
    currentMarketCode &&
    markets.some((market) => market.market === currentMarketCode)
  ) {
    return currentMarketCode
  }

  if (markets.some((market) => market.market === DEFAULT_MARKET_CODE)) {
    return DEFAULT_MARKET_CODE
  }

  return markets[0]?.market ?? null
}

export function buildChartSummary(data: ChartPoint[]): ChartSummary | null {
  if (data.length === 0) {
    return null
  }

  const firstPoint = data[0]
  const lastPoint = data.at(-1) ?? firstPoint
  const closes = data.map((point) => point.close)
  const min = Math.min(...closes)
  const max = Math.max(...closes)
  const changeAmount = lastPoint.close - firstPoint.close

  return {
    changeAmount,
    changeRate: firstPoint.close === 0 ? 0 : changeAmount / firstPoint.close,
    endTimestamp: lastPoint.timestamp,
    max,
    min,
    pointCount: data.length,
    startTimestamp: firstPoint.timestamp,
    latestClose: lastPoint.close,
  }
}
