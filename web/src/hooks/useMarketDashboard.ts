import { useEffect, useMemo, useState } from 'react'
import { fetchChartPoints, fetchDashboardMarkets } from '../lib/upbit'
import type { ChartPoint, ChartRange, DashboardMarket } from '../types/market'

const DEFAULT_RANGE: ChartRange = '1D'

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
  const [chartData, setChartData] = useState<ChartPoint[]>([])
  const [isMarketLoading, setIsMarketLoading] = useState(true)
  const [isChartLoading, setIsChartLoading] = useState(false)
  const [marketError, setMarketError] = useState<string | null>(null)
  const [chartError, setChartError] = useState<string | null>(null)
  const [lastUpdatedAt, setLastUpdatedAt] = useState<number | null>(null)

  const selectedMarket = useMemo(
    () => markets.find((market) => market.market === selectedMarketCode) ?? null,
    [markets, selectedMarketCode],
  )

  async function refreshMarkets() {
    setIsMarketLoading(true)
    setMarketError(null)

    try {
      const nextMarkets = await fetchDashboardMarkets()
      setMarkets(nextMarkets)
      setSelectedMarketCode((current) => {
        if (current && nextMarkets.some((market) => market.market === current)) {
          return current
        }

        return nextMarkets[0]?.market ?? null
      })
      setLastUpdatedAt(Date.now())
    } catch (error) {
      setMarketError(asMessage(error))
    } finally {
      setIsMarketLoading(false)
    }
  }

  async function refreshChart(marketCode: string, range: ChartRange) {
    setIsChartLoading(true)
    setChartError(null)

    try {
      const nextChartData = await fetchChartPoints(marketCode, range)
      setChartData(nextChartData)
    } catch (error) {
      setChartError(asMessage(error))
    } finally {
      setIsChartLoading(false)
    }
  }

  async function refreshAll() {
    await refreshMarkets()
    if (selectedMarketCode) {
      await refreshChart(selectedMarketCode, chartRange)
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
    markets,
    refreshAll,
    selectedMarket,
    selectMarket: setSelectedMarketCode,
    setChartRange,
  }
}
