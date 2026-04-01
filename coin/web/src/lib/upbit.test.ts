import { describe, expect, it } from 'vitest'
import { chunk, normalizeCandles, normalizeMarkets } from './upbit'

describe('chunk', () => {
  it('splits an array into fixed-size groups', () => {
    expect(chunk([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]])
  })
})

describe('normalizeMarkets', () => {
  it('filters KRW markets and sorts by 24h volume descending', () => {
    const markets = [
      { market: 'BTC-XRP', korean_name: '리플', english_name: 'Ripple' },
      { market: 'KRW-BTC', korean_name: '비트코인', english_name: 'Bitcoin' },
      { market: 'KRW-ETH', korean_name: '이더리움', english_name: 'Ethereum' },
    ]

    const tickers = [
      {
        market: 'KRW-ETH',
        trade_price: 1000,
        signed_change_rate: -0.01,
        signed_change_price: -10,
        acc_trade_price_24h: 100000,
        high_price: 1100,
        low_price: 900,
      },
      {
        market: 'KRW-BTC',
        trade_price: 2000,
        signed_change_rate: 0.02,
        signed_change_price: 20,
        acc_trade_price_24h: 300000,
        high_price: 2100,
        low_price: 1900,
      },
    ]

    expect(normalizeMarkets(markets, tickers)).toEqual([
      {
        market: 'KRW-BTC',
        koreanName: '비트코인',
        englishName: 'Bitcoin',
        tradePrice: 2000,
        signedChangeRate: 0.02,
        signedChangePrice: 20,
        accTradePrice24h: 300000,
        highPrice: 2100,
        lowPrice: 1900,
      },
      {
        market: 'KRW-ETH',
        koreanName: '이더리움',
        englishName: 'Ethereum',
        tradePrice: 1000,
        signedChangeRate: -0.01,
        signedChangePrice: -10,
        accTradePrice24h: 100000,
        highPrice: 1100,
        lowPrice: 900,
      },
    ])
  })
})

describe('normalizeCandles', () => {
  it('reverses the API order into ascending chart order', () => {
    const candles = [
      {
        timestamp: 200,
        opening_price: 12,
        high_price: 15,
        low_price: 11,
        trade_price: 14,
        candle_acc_trade_volume: 120,
      },
      {
        timestamp: 100,
        opening_price: 10,
        high_price: 13,
        low_price: 9,
        trade_price: 12,
        candle_acc_trade_volume: 80,
      },
    ]

    expect(normalizeCandles(candles)).toEqual([
      {
        timestamp: 100,
        open: 10,
        high: 13,
        low: 9,
        close: 12,
        volume: 80,
      },
      {
        timestamp: 200,
        open: 12,
        high: 15,
        low: 11,
        close: 14,
        volume: 120,
      },
    ])
  })
})
