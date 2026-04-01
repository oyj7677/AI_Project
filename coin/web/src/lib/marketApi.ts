import type { ChartPoint, ChartRange, DashboardMarket } from '../types/market'

const API_BASE = import.meta.env.VITE_MARKET_API_BASE ?? '/api'

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`)
  if (!response.ok) {
    throw new Error(`Market API request failed: ${response.status}`)
  }

  return response.json() as Promise<T>
}

export function fetchMarkets(quote = 'KRW') {
  const params = new URLSearchParams({ quote })
  return fetchJson<DashboardMarket[]>(`/markets?${params.toString()}`)
}

export function fetchMarketChart(market: string, range: ChartRange) {
  const params = new URLSearchParams({ range })
  return fetchJson<ChartPoint[]>(
    `/markets/${encodeURIComponent(market)}/chart?${params.toString()}`,
  )
}
