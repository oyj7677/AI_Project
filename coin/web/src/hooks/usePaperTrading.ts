import { useEffect, useMemo, useState } from 'react'
import type { DashboardMarket } from '../types/market'
import type {
  PaperOrder,
  PaperPortfolio,
} from '../types/paperTrading'
import { paperTradingGateway } from '../services/paperTradingGateway'

function asMessage(error: unknown) {
  if (error instanceof Error) {
    return error.message
  }
  return '알 수 없는 오류가 발생했습니다.'
}

function buildPriceMap(markets: DashboardMarket[]) {
  return new Map(markets.map((market) => [market.market, market.tradePrice]))
}

export function usePaperTrading(markets: DashboardMarket[], selectedMarket: DashboardMarket | null) {
  const [portfolio, setPortfolio] = useState<PaperPortfolio | null>(null)
  const [orders, setOrders] = useState<PaperOrder[]>([])
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const priceMap = useMemo(() => buildPriceMap(markets), [markets])
  const selectedPosition = useMemo(
    () => portfolio?.positions.find((position) => position.market === selectedMarket?.market) ?? null,
    [portfolio?.positions, selectedMarket?.market],
  )

  async function refreshPaperTrading() {
    setIsLoading(true)
    setError(null)
    try {
      const [nextPortfolio, nextOrders] = await Promise.all([
        paperTradingGateway.fetchPortfolio(),
        paperTradingGateway.fetchOrders(),
      ])
      setPortfolio(nextPortfolio)
      setOrders(nextOrders)
    } catch (error) {
      setError(asMessage(error))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void refreshPaperTrading()
  }, [])

  async function submitBuyOrder(amountKrw: number) {
    if (!selectedMarket) {
      setError('선택된 종목이 없습니다.')
      return
    }
    setIsSubmitting(true)
    setError(null)
    try {
      const result = await paperTradingGateway.submitOrder({
        market: selectedMarket.market,
        side: 'buy',
        amountKrw,
      })
      setPortfolio(result.portfolio)
      setOrders((current) => [result.order, ...current])
    } catch (error) {
      setError(asMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function submitSellOrder(quantity: number) {
    if (!selectedMarket) {
      setError('선택된 종목이 없습니다.')
      return
    }
    setIsSubmitting(true)
    setError(null)
    try {
      const result = await paperTradingGateway.submitOrder({
        market: selectedMarket.market,
        side: 'sell',
        quantity,
      })
      setPortfolio(result.portfolio)
      setOrders((current) => [result.order, ...current])
    } catch (error) {
      setError(asMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function resetPortfolio() {
    setIsSubmitting(true)
    setError(null)
    try {
      await paperTradingGateway.resetPortfolio()
      await refreshPaperTrading()
    } catch (error) {
      setError(asMessage(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const positions = useMemo(() => {
    if (!portfolio) {
      return []
    }

    return portfolio.positions.map((position) => {
      const currentPrice = priceMap.get(position.market) ?? position.averageEntryPrice
      const marketValue = currentPrice * position.quantity
      const unrealizedPnl = marketValue - position.costBasis
      return {
        ...position,
        currentPrice,
        marketValue,
        unrealizedPnl,
        unrealizedPnlRate:
          position.costBasis > 0 ? unrealizedPnl / position.costBasis : 0,
      }
    })
  }, [portfolio, priceMap])

  const accountSummary = useMemo(() => {
    if (!portfolio) {
      return null
    }
    const marketValue = positions.reduce((sum, position) => sum + position.marketValue, 0)
    const unrealizedPnl = positions.reduce((sum, position) => sum + position.unrealizedPnl, 0)
    const totalEquity = portfolio.cashBalance + marketValue
    return {
      cashBalance: portfolio.cashBalance,
      initialCashBalance: portfolio.initialCashBalance,
      marketValue,
      realizedPnl: portfolio.realizedPnl,
      totalEquity,
      unrealizedPnl,
      updatedAt: portfolio.updatedAt,
    }
  }, [portfolio, positions])

  return {
    accountSummary,
    error,
    isLoading,
    isSubmitting,
    orders,
    positions,
    refreshPaperTrading,
    resetPortfolio,
    selectedPosition,
    submitBuyOrder,
    submitSellOrder,
  }
}
