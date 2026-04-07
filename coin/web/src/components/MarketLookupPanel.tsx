import { startTransition } from 'react'
import { formatCompactCurrency, formatCurrency, formatPercent } from '../lib/format'
import type { DashboardMarket, MarketListItem } from '../types/market'

interface MarketLookupPanelProps {
  error: string | null
  isLoading: boolean
  isSelectedFavorite: boolean
  onQueryChange: (value: string) => void
  onSelect: (marketCode: string) => void
  query: string
  resultCount: number
  selectedMarket: DashboardMarket | null
  suggestions: MarketListItem[]
  totalCount: number
}

export function MarketLookupPanel({
  error,
  isLoading,
  isSelectedFavorite,
  onQueryChange,
  onSelect,
  query,
  resultCount,
  selectedMarket,
  suggestions,
  totalCount,
}: MarketLookupPanelProps) {
  const normalizedQuery = query.trim()
  const firstSuggestion = suggestions[0] ?? null

  return (
    <>
      <header className="panel-header">
        <div>
          <p className="panel-eyebrow">Coin Lookup</p>
          <h2>코인 조회</h2>
          <p className="market-subtitle">
            종목명을 입력하면 빠르게 후보를 좁히고 바로 차트까지 이동할 수 있습니다.
          </p>
        </div>

        {selectedMarket ? (
          <div
            className={`change-badge ${
              selectedMarket.signedChangeRate >= 0 ? 'rise' : 'fall'
            }`}
          >
            {formatPercent(selectedMarket.signedChangeRate)}
          </div>
        ) : null}
      </header>

      <div className="lookup-grid">
        <section className="lookup-search-shell">
          <label className="market-search-label" htmlFor="coin-lookup-search">
            코인명 또는 심볼 검색
          </label>
          <input
            aria-label="코인 조회 검색"
            className="lookup-search"
            id="coin-lookup-search"
            onChange={(event) => onQueryChange(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && firstSuggestion) {
                event.preventDefault()
                startTransition(() => {
                  onSelect(firstSuggestion.market)
                })
              }
            }}
            placeholder="BTC, 비트코인, Ethereum 검색"
            type="search"
            value={query}
          />

          <div className="lookup-meta">
            <span>
              {normalizedQuery
                ? `"${normalizedQuery}" 검색 결과 ${resultCount}개 / 전체 ${totalCount}개`
                : `전체 ${totalCount}개 종목 중 바로 조회할 대상을 고르세요.`}
            </span>
            {normalizedQuery ? (
              <button
                className="lookup-clear"
                onClick={() => onQueryChange('')}
                type="button"
              >
                검색 지우기
              </button>
            ) : firstSuggestion ? (
              <span>Enter로 첫 결과 선택</span>
            ) : null}
          </div>

          {error && !isLoading ? (
            <div className="lookup-feedback error">
              시장 데이터를 불러오지 못했습니다: {error}
            </div>
          ) : null}

          <div className="lookup-chip-list">
            {isLoading
              ? Array.from({ length: 4 }).map((_, index) => (
                  <div className="skeleton-block lookup-skeleton" key={index} />
                ))
              : null}

            {!isLoading && suggestions.length === 0 ? (
              <div className="empty-state lookup-empty">
                <p>
                  {normalizedQuery
                    ? '조건에 맞는 코인을 찾지 못했습니다.'
                    : '표시할 조회 후보가 없습니다.'}
                </p>
              </div>
            ) : null}

            {!isLoading
              ? suggestions.map((market) => (
                  <button
                    className={`lookup-chip ${
                      selectedMarket?.market === market.market ? 'active' : ''
                    }`}
                    key={market.market}
                    onClick={() =>
                      startTransition(() => {
                        onSelect(market.market)
                      })
                    }
                    type="button"
                  >
                    <div className="lookup-chip-head">
                      <div>
                        <strong>{market.koreanName}</strong>
                        <span className="lookup-chip-code">{market.market}</span>
                      </div>
                      <span
                        className={`lookup-chip-change ${
                          market.signedChangeRate >= 0 ? 'rise' : 'fall'
                        }`}
                      >
                        {formatPercent(market.signedChangeRate)}
                      </span>
                    </div>
                    <span>{market.englishName}</span>
                    <span>{formatCurrency(market.tradePrice)}</span>
                  </button>
                ))
              : null}
          </div>
        </section>

        <aside className="lookup-selection-card">
          <span className="lookup-selection-label">현재 조회 중인 코인</span>
          <strong className="lookup-selection-name">
            {selectedMarket
              ? `${selectedMarket.koreanName} ${selectedMarket.market}`
              : '종목을 고르면 요약이 표시됩니다.'}
          </strong>
          <p className="lookup-selection-meta">
            {selectedMarket
              ? selectedMarket.englishName
              : '좌측 검색 결과나 종목 목록에서 코인을 선택해 주세요.'}
          </p>
          {isSelectedFavorite ? (
            <p className="lookup-selection-meta">관심 종목으로 고정된 상태입니다.</p>
          ) : null}

          <div className="lookup-selection-metrics">
            <div className="lookup-selection-metric">
              <span>현재가</span>
              <strong>
                {selectedMarket ? formatCurrency(selectedMarket.tradePrice) : '-'}
              </strong>
            </div>
            <div className="lookup-selection-metric">
              <span>24시간 거래대금</span>
              <strong>
                {selectedMarket
                  ? formatCompactCurrency(selectedMarket.accTradePrice24h)
                  : '-'}
              </strong>
            </div>
            <div className="lookup-selection-metric">
              <span>당일 저가</span>
              <strong>
                {selectedMarket ? formatCurrency(selectedMarket.lowPrice) : '-'}
              </strong>
            </div>
            <div className="lookup-selection-metric">
              <span>당일 고가</span>
              <strong>
                {selectedMarket ? formatCurrency(selectedMarket.highPrice) : '-'}
              </strong>
            </div>
          </div>
        </aside>
      </div>
    </>
  )
}
