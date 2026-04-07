import { describe, expect, it } from 'vitest'
import {
  buildChartSummary,
  filterMarkets,
  resolveSelectedMarketCode,
} from './marketView'

describe('filterMarkets', () => {
  const markets = [
    {
      market: 'KRW-BTC',
      koreanName: '비트코인',
      englishName: 'Bitcoin',
      tradePrice: 100,
      signedChangeRate: 0.01,
      signedChangePrice: 1,
      accTradePrice24h: 1000,
      highPrice: 120,
      lowPrice: 90,
      isFavorite: false,
    },
    {
      market: 'KRW-ETH',
      koreanName: '이더리움',
      englishName: 'Ethereum',
      tradePrice: 80,
      signedChangeRate: -0.02,
      signedChangePrice: -2,
      accTradePrice24h: 800,
      highPrice: 88,
      lowPrice: 76,
      isFavorite: true,
    },
  ]

  it('matches market code, korean name, and english name case-insensitively', () => {
    expect(filterMarkets(markets, 'btc')).toEqual([markets[0]])
    expect(filterMarkets(markets, ' 이더 ')).toEqual([markets[1]])
    expect(filterMarkets(markets, 'ethereum')).toEqual([markets[1]])
  })

  it('returns the original list when the query is empty', () => {
    expect(filterMarkets(markets, '   ')).toEqual(markets)
  })
})

describe('resolveSelectedMarketCode', () => {
  const markets = [
    {
      market: 'KRW-XRP',
      koreanName: '리플',
      englishName: 'XRP',
      tradePrice: 10,
      signedChangeRate: 0,
      signedChangePrice: 0,
      accTradePrice24h: 100,
      highPrice: 11,
      lowPrice: 9,
    },
    {
      market: 'KRW-BTC',
      koreanName: '비트코인',
      englishName: 'Bitcoin',
      tradePrice: 100,
      signedChangeRate: 0,
      signedChangePrice: 0,
      accTradePrice24h: 1000,
      highPrice: 110,
      lowPrice: 90,
    },
    {
      market: 'KRW-ETH',
      koreanName: '이더리움',
      englishName: 'Ethereum',
      tradePrice: 50,
      signedChangeRate: 0,
      signedChangePrice: 0,
      accTradePrice24h: 500,
      highPrice: 55,
      lowPrice: 45,
    },
  ]

  it('keeps the current selection when it still exists', () => {
    expect(resolveSelectedMarketCode(markets, 'KRW-ETH')).toBe('KRW-ETH')
  })

  it('falls back to KRW-BTC when the current selection is missing', () => {
    expect(resolveSelectedMarketCode(markets, 'KRW-DOGE')).toBe('KRW-BTC')
  })
})

describe('buildChartSummary', () => {
  it('summarizes the visible chart range', () => {
    const summary = buildChartSummary([
      { timestamp: 1, open: 95, high: 110, low: 94, close: 100, volume: 10 },
      { timestamp: 2, open: 100, high: 118, low: 99, close: 115, volume: 20 },
      { timestamp: 3, open: 115, high: 121, low: 110, close: 120, volume: 15 },
    ])

    expect(summary).toEqual({
      changeAmount: 20,
      changeRate: 0.2,
      endTimestamp: 3,
      max: 120,
      min: 100,
      pointCount: 3,
      startTimestamp: 1,
      latestClose: 120,
    })
  })

  it('returns null for empty data', () => {
    expect(buildChartSummary([])).toBeNull()
  })
})
