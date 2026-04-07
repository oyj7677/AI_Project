import { startTransition } from 'react'
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
  query: string
  resultCount: number
  selectedMarketCode: string | null
  totalCount: number
}

export function MarketList({
  error,
  isLoading,
  marketSort,
  markets,
  onSelect,
  onSortChange,
  onToggleFavorite,
  query,
  resultCount,
  selectedMarketCode,
  totalCount,
}: MarketListProps) {
  const normalizedQuery = query.trim()

  return (
    <>
      <div className="market-toolbar">
        <div className="market-toolbar-copy">
          <p className="market-sort-label">표시 중인 종목</p>
          <p className="market-toolbar-caption">
            {normalizedQuery
              ? `"${normalizedQuery}" 결과 ${resultCount}개`
              : `전체 ${totalCount}개 종목`}
          </p>
        </div>

        <div className="market-sort-shell">
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

        {!isLoading && !error && markets.length === 0 ? (
          <div className="empty-state">
            <p>
              {normalizedQuery
                ? '검색 결과가 없습니다.'
                : '표시할 종목이 없습니다.'}
            </p>
          </div>
        ) : null}

        {!isLoading && !error
          ? markets.map((market) => (
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
