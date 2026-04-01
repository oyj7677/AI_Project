import { startTransition, useDeferredValue, useState } from 'react'
import { formatCompactCurrency, formatCurrency, formatPercent } from '../lib/format'
import type { DashboardMarket } from '../types/market'

interface MarketListProps {
  error: string | null
  isLoading: boolean
  markets: DashboardMarket[]
  onSelect: (marketCode: string) => void
  selectedMarketCode: string | null
}

export function MarketList({
  error,
  isLoading,
  markets,
  onSelect,
  selectedMarketCode,
}: MarketListProps) {
  const [query, setQuery] = useState('')
  const deferredQuery = useDeferredValue(query.trim().toLowerCase())

  const filteredMarkets = markets.filter((market) => {
    if (!deferredQuery) {
      return true
    }

    return [
      market.market,
      market.koreanName,
      market.englishName,
    ].some((value) => value.toLowerCase().includes(deferredQuery))
  })

  return (
    <>
      <div className="market-toolbar">
        <input
          className="market-search"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="BTC, 비트코인, Ethereum 검색"
        />
      </div>

      <div className="market-list">
        {isLoading
          ? Array.from({ length: 6 }).map((_, index) => (
              <div className="skeleton-block" key={index} />
            ))
          : null}

        {!isLoading && error ? (
          <div className="error-state">
            <p>종목 목록을 불러오지 못했습니다.</p>
            <p>{error}</p>
          </div>
        ) : null}

        {!isLoading && !error && filteredMarkets.length === 0 ? (
          <div className="empty-state">
            <p>검색 결과가 없습니다.</p>
          </div>
        ) : null}

        {!isLoading && !error
          ? filteredMarkets.map((market) => (
              <button
                className={`market-row ${
                  selectedMarketCode === market.market ? 'active' : ''
                }`}
                key={market.market}
                onClick={() =>
                  startTransition(() => {
                    onSelect(market.market)
                  })
                }
                type="button"
              >
                <div className="market-row-top">
                  <div>
                    <div className="market-name">{market.koreanName}</div>
                    <div className="market-meta">{market.englishName}</div>
                  </div>
                  <div className="market-code">{market.market}</div>
                </div>

                <div className="market-row-bottom">
                  <div>
                    <div className="market-price">
                      {formatCurrency(market.tradePrice)}
                    </div>
                    <div
                      className={`mini-change ${
                        market.signedChangeRate >= 0 ? 'rise' : 'fall'
                      }`}
                    >
                      {formatPercent(market.signedChangeRate)}
                    </div>
                  </div>

                  <div className="market-volume">
                    24H {formatCompactCurrency(market.accTradePrice24h)}
                  </div>
                </div>
              </button>
            ))
          : null}
      </div>
    </>
  )
}
