import { useEffect, useEffectEvent, useRef, useState } from 'react'
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
const DEFAULT_REFRESH_INTERVAL_MS = 15000

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
  const [refreshIntervalMs, setRefreshIntervalMs] = useState(
    DEFAULT_REFRESH_INTERVAL_MS,
  )
  const [chartData, setChartData] = useState<ChartPoint[]>([])
  const [isMarketLoading, setIsMarketLoading] = useState(true)
  const [isChartLoading, setIsChartLoading] = useState(false)
  const [marketError, setMarketError] = useState<string | null>(null)
  const [chartError, setChartError] = useState<string | null>(null)
  const [lastUpdatedAt, setLastUpdatedAt] = useState<number | null>(null)
  const [favoriteCodes, setFavoriteCodes] = useState<string[]>([])
  const [isPageVisible, setIsPageVisible] = useState(
    typeof document === 'undefined' ? true : document.visibilityState === 'visible',
  )
  const chartRequestIdRef = useRef(0)
  const pollingInFlightRef = useRef(false)

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

  useEffect(() => {
    const handleVisibilityChange = () => {
      setIsPageVisible(document.visibilityState === 'visible')
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
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

  async function refreshMarkets(options: { silent?: boolean } = {}) {
    if (!options.silent) {
      setIsMarketLoading(true)
    }
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
      return nextSelectedMarketCode
    } catch (error) {
      setMarketError(asMessage(error))
      return null
    } finally {
      if (!options.silent) {
        setIsMarketLoading(false)
      }
    }
  }

  async function refreshChart(
    marketCode: string,
    range: ChartRange,
    options: { silent?: boolean } = {},
  ) {
    const requestId = ++chartRequestIdRef.current
    if (!options.silent) {
      setIsChartLoading(true)
    }
    setChartError(null)

    try {
      const nextChartData = await marketDataGateway.fetchChartPoints(marketCode, range)
      if (requestId !== chartRequestIdRef.current) {
        return
      }
      setChartData(nextChartData)
      setLastUpdatedAt(Date.now())
    } catch (error) {
      if (requestId !== chartRequestIdRef.current) {
        return
      }
      setChartError(asMessage(error))
    } finally {
      if (requestId === chartRequestIdRef.current && !options.silent) {
        setIsChartLoading(false)
      }
    }
  }

  async function refreshAll(options: { silent?: boolean } = {}) {
    const nextSelectedMarketCode = await refreshMarkets(options)
    if (nextSelectedMarketCode) {
      await refreshChart(nextSelectedMarketCode, chartRange, options)
    }
  }

  const runRefreshAll = useEffectEvent(
    async (options: { silent?: boolean } = {}) => {
      await refreshAll(options)
    },
  )

  useEffect(() => {
    void refreshMarkets()
  }, [])

  useEffect(() => {
    if (!selectedMarketCode) {
      return
    }

    void refreshChart(selectedMarketCode, chartRange)
  }, [selectedMarketCode, chartRange])

  useEffect(() => {
    if (!selectedMarketCode || refreshIntervalMs === 0 || !isPageVisible) {
      return
    }

    const intervalId = window.setInterval(() => {
      if (pollingInFlightRef.current) {
        return
      }

      pollingInFlightRef.current = true
      void runRefreshAll({ silent: true }).finally(() => {
        pollingInFlightRef.current = false
      })
    }, refreshIntervalMs)

    return () => {
      window.clearInterval(intervalId)
    }
  }, [chartRange, isPageVisible, refreshIntervalMs, selectedMarketCode])

  return {
    chartData,
    chartError,
    chartRange,
    favoriteCodes,
    isLivePolling: refreshIntervalMs > 0,
    isPageVisible,
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
    refreshIntervalMs,
    refreshAll,
    selectedMarket,
    selectMarket: setSelectedMarketCode,
    setChartRange,
    setRefreshIntervalMs,
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
