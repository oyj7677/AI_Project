import { startTransition, useDeferredValue, useState } from 'react'
import { formatCompactCurrency, formatCurrency, formatPercent } from '../lib/format'
import type { MarketListItem, MarketSort } from '../types/market'

interface MarketListProps {
  error: string | null
  isLoading: boolean
  marketSort: MarketSort
  markets: MarketListItem[]
  onSelect: (marketCode: string) => void
  onSortChange: (value: MarketSort) => void
  onToggleFavorite: (marketCode: string) => void
  selectedMarketCode: string | null
}

export function MarketList({
  error,
  isLoading,
  marketSort,
  markets,
  onSelect,
  onSortChange,
  onToggleFavorite,
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
        <label className="market-search-label" htmlFor="market-search">
          종목 검색
        </label>
        <input
          aria-label="코인 종목 검색"
          className="market-search"
          id="market-search"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="BTC, 비트코인, Ethereum 검색"
        />
        <label className="market-sort-label" htmlFor="market-sort">
          정렬 기준
        </label>
        <select
          className="market-sort"
          id="market-sort"
          value={marketSort}
          onChange={(event) => onSortChange(event.target.value as MarketSort)}
        >
          <option value="volume">거래대금 순</option>
          <option value="change">등락률 순</option>
          <option value="name">이름 순</option>
        </select>
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
              <article
                className={`market-row ${
                  selectedMarketCode === market.market ? 'active' : ''
                }`}
                key={market.market}
              >
                <div className="market-row-top">
                  <button
                    className="market-select"
                    onClick={() =>
                      startTransition(() => {
                        onSelect(market.market)
                      })
                    }
                    type="button"
                  >
                    <div>
                      <div className="market-name">{market.koreanName}</div>
                      <div className="market-meta">{market.englishName}</div>
                    </div>
                    <div className="market-code">{market.market}</div>
                  </button>
                  <button
                    aria-label={
                      market.isFavorite
                        ? `${market.koreanName} 관심 종목 해제`
                        : `${market.koreanName} 관심 종목 추가`
                    }
                    className={`favorite-toggle ${market.isFavorite ? 'active' : ''}`}
                    onClick={() => onToggleFavorite(market.market)}
                    type="button"
                  >
                    ★
                  </button>
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
              </article>
            ))
          : null}
      </div>
    </>
  )
}
