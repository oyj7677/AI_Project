import { useEffect, useRef, useState } from 'react'
import { marketDataGateway } from '../services/marketDataGateway'
import type {
  ChartPoint,
  ChartRange,
  DashboardMarket,
  MarketListItem,
  MarketSort,
} from '../types/market'

const DEFAULT_RANGE: ChartRange = '1D'
const DEFAULT_SORT: MarketSort = 'volume'
const FAVORITES_STORAGE_KEY = 'coin-market-favorites'

function asMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message
  }

  return '알 수 없는 오류가 발생했습니다.'
}

export function useMarketDashboard() {
  const [markets, setMarkets] = useState<DashboardMarket[]>([])
  const [selectedMarketCode, setSelectedMarketCode] = useState<string | null>(null)
  const [chartRange, setChartRange] = useState<ChartRange>(DEFAULT_RANGE)
  const [marketSort, setMarketSort] = useState<MarketSort>(DEFAULT_SORT)
  const [chartData, setChartData] = useState<ChartPoint[]>([])
  const [isMarketLoading, setIsMarketLoading] = useState(true)
  const [isChartLoading, setIsChartLoading] = useState(false)
  const [marketError, setMarketError] = useState<string | null>(null)
  const [chartError, setChartError] = useState<string | null>(null)
  const [lastUpdatedAt, setLastUpdatedAt] = useState<number | null>(null)
  const [favoriteCodes, setFavoriteCodes] = useState<string[]>([])
  const chartRequestIdRef = useRef(0)

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(FAVORITES_STORAGE_KEY)
      if (!stored) {
        return
      }

      const parsed = JSON.parse(stored) as unknown
      if (Array.isArray(parsed)) {
        setFavoriteCodes(parsed.filter((value): value is string => typeof value === 'string'))
      }
    } catch {
      // Ignore malformed local storage and keep defaults.
    }
  }, [])

  const selectedMarket =
    markets.find((market) => market.market === selectedMarketCode) ?? null

  const marketList = [...markets]
    .sort((left, right) => {
      const favoriteDelta =
        Number(favoriteCodes.includes(right.market)) -
        Number(favoriteCodes.includes(left.market))
      if (favoriteDelta !== 0) {
        return favoriteDelta
      }

      switch (marketSort) {
        case 'change':
          return right.signedChangeRate - left.signedChangeRate
        case 'name':
          return left.koreanName.localeCompare(right.koreanName, 'ko')
        case 'volume':
        default:
          return right.accTradePrice24h - left.accTradePrice24h
      }
    })
    .map(
      (market): MarketListItem => ({
        ...market,
        isFavorite: favoriteCodes.includes(market.market),
      }),
    )

  async function refreshMarkets() {
    setIsMarketLoading(true)
    setMarketError(null)

    try {
      const nextMarkets = await marketDataGateway.fetchMarkets()
      let nextSelectedMarketCode: string | null = null

      setMarkets(nextMarkets)
      setSelectedMarketCode((current) => {
        if (current && nextMarkets.some((market) => market.market === current)) {
          nextSelectedMarketCode = current
          return current
        }

        nextSelectedMarketCode = nextMarkets[0]?.market ?? null
        return nextSelectedMarketCode
      })
      setLastUpdatedAt(Date.now())
      return nextSelectedMarketCode
    } catch (error) {
      setMarketError(asMessage(error))
      return null
    } finally {
      setIsMarketLoading(false)
    }
  }

  async function refreshChart(marketCode: string, range: ChartRange) {
    const requestId = ++chartRequestIdRef.current
    setIsChartLoading(true)
    setChartError(null)

    try {
      const nextChartData = await marketDataGateway.fetchChartPoints(marketCode, range)
      if (requestId !== chartRequestIdRef.current) {
        return
      }
      setChartData(nextChartData)
    } catch (error) {
      if (requestId !== chartRequestIdRef.current) {
        return
      }
      setChartError(asMessage(error))
    } finally {
      if (requestId === chartRequestIdRef.current) {
        setIsChartLoading(false)
      }
    }
  }

  async function refreshAll() {
    const nextSelectedMarketCode = await refreshMarkets()
    if (nextSelectedMarketCode) {
      await refreshChart(nextSelectedMarketCode, chartRange)
    }
  }

  useEffect(() => {
    void refreshMarkets()
  }, [])

  useEffect(() => {
    if (!selectedMarketCode) {
      return
    }

    void refreshChart(selectedMarketCode, chartRange)
  }, [selectedMarketCode, chartRange])

  return {
    chartData,
    chartError,
    chartRange,
    favoriteCodes,
    isChartLoading,
    isMarketLoading,
    lastUpdatedLabel: lastUpdatedAt
      ? new Intl.DateTimeFormat('ko-KR', {
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        }).format(lastUpdatedAt)
      : null,
    marketError,
    marketList,
    marketSort,
    refreshAll,
    selectedMarket,
    selectMarket: setSelectedMarketCode,
    setChartRange,
    setMarketSort,
    toggleFavorite: (marketCode: string) => {
      setFavoriteCodes((current) => {
        const next = current.includes(marketCode)
          ? current.filter((value) => value !== marketCode)
          : [...current, marketCode]
        window.localStorage.setItem(FAVORITES_STORAGE_KEY, JSON.stringify(next))
        return next
      })
    },
  }
}
