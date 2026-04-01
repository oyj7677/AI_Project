import { createServer } from 'node:http'
import { createTtlCache } from './cache.mjs'
import { fetchChart, fetchMarkets } from './upbitAdapter.mjs'

const port = Number(process.env.PORT ?? '8787')
const upbitBaseUrl = process.env.UPBIT_API_BASE ?? 'https://api.upbit.com'
const allowedOrigins = process.env.ALLOWED_ORIGINS ?? '*'
const marketsCacheTtlMs = Number(process.env.MARKETS_CACHE_TTL_MS ?? '5000')
const chartCacheTtlMs = Number(process.env.CHART_CACHE_TTL_MS ?? '3000')
const cache = createTtlCache()

function writeJson(response, statusCode, body, origin) {
  response.writeHead(statusCode, {
    'access-control-allow-origin': origin,
    'access-control-allow-methods': 'GET,OPTIONS',
    'access-control-allow-headers': 'Content-Type',
    'cache-control': 'no-store',
    'content-type': 'application/json; charset=utf-8',
  })
  response.end(JSON.stringify(body))
}

function resolveOrigin(requestOrigin) {
  if (allowedOrigins === '*') {
    return '*'
  }

  const allowList = allowedOrigins.split(',').map((value) => value.trim())
  return allowList.includes(requestOrigin ?? '') ? requestOrigin : allowList[0] ?? '*'
}

const server = createServer(async (request, response) => {
  const origin = resolveOrigin(request.headers.origin)
  if (request.method === 'OPTIONS') {
    response.writeHead(204, {
      'access-control-allow-origin': origin,
      'access-control-allow-methods': 'GET,OPTIONS',
      'access-control-allow-headers': 'Content-Type',
    })
    response.end()
    return
  }

  if (!request.url || request.method !== 'GET') {
    writeJson(response, 404, { error: 'Not Found' }, origin)
    return
  }

  const url = new URL(request.url, `http://${request.headers.host}`)

  try {
    if (url.pathname === '/health') {
      writeJson(
        response,
        200,
        {
          status: 'ok',
          service: 'coin-proxy',
          upstream: 'upbit',
        },
        origin,
      )
      return
    }

    if (url.pathname === '/api/markets') {
      const quote = url.searchParams.get('quote') ?? 'KRW'
      const cacheKey = `markets:${quote}`
      const cached = cache.get(cacheKey)
      if (cached) {
        writeJson(response, 200, cached, origin)
        return
      }

      const markets = await fetchMarkets(upbitBaseUrl, quote)
      cache.set(cacheKey, markets, marketsCacheTtlMs)
      writeJson(response, 200, markets, origin)
      return
    }

    const chartMatch = url.pathname.match(/^\/api\/markets\/([^/]+)\/chart$/)
    if (chartMatch) {
      const market = decodeURIComponent(chartMatch[1])
      const range = url.searchParams.get('range') ?? '1D'
      const cacheKey = `chart:${market}:${range}`
      const cached = cache.get(cacheKey)
      if (cached) {
        writeJson(response, 200, cached, origin)
        return
      }

      const chart = await fetchChart(upbitBaseUrl, market, range)
      cache.set(cacheKey, chart, chartCacheTtlMs)
      writeJson(response, 200, chart, origin)
      return
    }

    writeJson(response, 404, { error: 'Not Found' }, origin)
  } catch (error) {
    writeJson(
      response,
      502,
      {
        error: 'Upstream Error',
        message: error instanceof Error ? error.message : 'Unknown error',
      },
      origin,
    )
  }
})

server.listen(port, () => {
  console.log(`coin-proxy listening on http://localhost:${port}`)
})
