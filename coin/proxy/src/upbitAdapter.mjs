const RANGE_TO_UPBIT = {
  '1H': { unit: 1, count: 60 },
  '4H': { unit: 5, count: 48 },
  '1D': { unit: 30, count: 48 },
  '1W': { unit: 240, count: 42 },
}

function chunk(items, size) {
  const groups = []
  for (let index = 0; index < items.length; index += size) {
    groups.push(items.slice(index, index + size))
  }
  return groups
}

async function fetchJson(baseUrl, path) {
  const response = await fetch(`${baseUrl}${path}`)
  if (!response.ok) {
    throw new Error(`Upbit request failed: ${response.status}`)
  }
  return response.json()
}

export function normalizeMarkets(markets, tickers, quote) {
  const filtered = markets.filter((market) => market.market.startsWith(`${quote}-`))
  const tickerMap = new Map(tickers.map((ticker) => [ticker.market, ticker]))

  return filtered
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
    .filter(Boolean)
    .sort((left, right) => right.accTradePrice24h - left.accTradePrice24h)
}

export function normalizeCandles(candles) {
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

async function fetchTickers(baseUrl, markets) {
  const groups = chunk(markets, 80)
  const responses = await Promise.all(
    groups.map((group) => {
      const params = new URLSearchParams()
      params.set('markets', group.join(','))
      return fetchJson(baseUrl, `/v1/ticker?${params.toString()}`)
    }),
  )

  return responses.flat()
}

export async function fetchMarkets(baseUrl, quote) {
  const markets = await fetchJson(baseUrl, '/v1/market/all?isDetails=false')
  const quoteMarkets = markets
    .filter((market) => market.market.startsWith(`${quote}-`))
    .map((market) => market.market)
  const tickers = await fetchTickers(baseUrl, quoteMarkets)

  return normalizeMarkets(markets, tickers, quote)
}

export async function fetchChart(baseUrl, market, range) {
  const config = RANGE_TO_UPBIT[range]
  if (!config) {
    throw new Error(`Unsupported range: ${range}`)
  }

  const params = new URLSearchParams({
    market,
    count: String(config.count),
  })
  const candles = await fetchJson(
    baseUrl,
    `/v1/candles/minutes/${config.unit}?${params.toString()}`,
  )

  return normalizeCandles(candles)
}
