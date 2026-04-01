import type { ChartPoint, ChartRange, DashboardMarket } from '../types/market'

const API_BASE = import.meta.env.VITE_UPBIT_API_BASE ?? '/api/upbit'

const RANGE_CONFIG: Record<
  ChartRange,
  { label: string; path: (market: string) => string }
> = {
  '1H': {
    label: '1시간',
    path: (market) => `/v1/candles/minutes/1?market=${market}&count=60`,
  },
  '4H': {
    label: '4시간',
    path: (market) => `/v1/candles/minutes/5?market=${market}&count=48`,
  },
  '1D': {
    label: '1일',
    path: (market) => `/v1/candles/minutes/30?market=${market}&count=48`,
  },
  '1W': {
    label: '1주',
    path: (market) => `/v1/candles/minutes/240?market=${market}&count=42`,
  },
}

interface UpbitMarket {
  market: string
  korean_name: string
  english_name: string
}

interface UpbitTicker {
  market: string
  trade_price: number
  signed_change_rate: number
  signed_change_price: number
  acc_trade_price_24h: number
  high_price: number
  low_price: number
}

interface UpbitCandle {
  timestamp: number
  opening_price: number
  high_price: number
  low_price: number
  trade_price: number
  candle_acc_trade_volume: number
}

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`)
  if (!response.ok) {
    throw new Error(`Upbit request failed: ${response.status}`)
  }

  return response.json() as Promise<T>
}

export function chunk<T>(items: T[], size: number) {
  const groups: T[][] = []

  for (let index = 0; index < items.length; index += size) {
    groups.push(items.slice(index, index + size))
  }

  return groups
}

async function fetchTickers(markets: string[]) {
  const groups = chunk(markets, 80)
  const responses = await Promise.all(
    groups.map((group) =>
      fetchJson<UpbitTicker[]>(`/v1/ticker?markets=${group.join(',')}`),
    ),
  )

  return responses.flat()
}

export function normalizeMarkets(
  markets: UpbitMarket[],
  tickers: UpbitTicker[],
): DashboardMarket[] {
  const krwMarkets = markets.filter((market) => market.market.startsWith('KRW-'))
  const tickerMap = new Map(tickers.map((ticker) => [ticker.market, ticker]))

  return krwMarkets
    .map((market) => {
      const ticker = tickerMap.get(market.market)
      if (!ticker) {
        return null
      }

      return {
        market: market.market,
        koreanName: market.korean_name,
        englishName: market.english_name,
        tradePrice: ticker.trade_price,
        signedChangeRate: ticker.signed_change_rate,
        signedChangePrice: ticker.signed_change_price,
        accTradePrice24h: ticker.acc_trade_price_24h,
        highPrice: ticker.high_price,
        lowPrice: ticker.low_price,
      }
    })
    .filter((market): market is DashboardMarket => Boolean(market))
    .sort((left, right) => right.accTradePrice24h - left.accTradePrice24h)
}

export async function fetchDashboardMarkets(): Promise<DashboardMarket[]> {
  const markets = await fetchJson<UpbitMarket[]>('/v1/market/all?isDetails=false')
  const krwMarkets = markets.filter((market) => market.market.startsWith('KRW-'))
  const tickers = await fetchTickers(krwMarkets.map((market) => market.market))

  return normalizeMarkets(markets, tickers)
}

export function normalizeCandles(candles: UpbitCandle[]): ChartPoint[] {
  return candles
    .slice()
    .reverse()
    .map((candle) => ({
      timestamp: candle.timestamp,
      open: candle.opening_price,
      high: candle.high_price,
      low: candle.low_price,
      close: candle.trade_price,
      volume: candle.candle_acc_trade_volume,
    }))
}

export async function fetchChartPoints(
  market: string,
  range: ChartRange,
): Promise<ChartPoint[]> {
  const candles = await fetchJson<UpbitCandle[]>(RANGE_CONFIG[range].path(market))

  return normalizeCandles(candles)
}

export const chartRanges = Object.entries(RANGE_CONFIG).map(([value, config]) => ({
  value: value as ChartRange,
  label: config.label,
}))
