import './App.css'
import { MarketList } from './components/MarketList'
import { MetricCard } from './components/MetricCard'
import { OrderHistory } from './components/OrderHistory'
import { OrderTicket } from './components/OrderTicket'
import { PaperTradingSummary } from './components/PaperTradingSummary'
import { PriceChart } from './components/PriceChart'
import { PositionsTable } from './components/PositionsTable'
import { useMarketDashboard } from './hooks/useMarketDashboard'
import { usePaperTrading } from './hooks/usePaperTrading'
import { formatCompactCurrency, formatCurrency, formatPercent } from './lib/format'

const refreshOptions = [
  { label: '꺼짐', value: 0 },
  { label: '5초', value: 5000 },
  { label: '15초', value: 15000 },
  { label: '30초', value: 30000 },
]

function App() {
  const {
    chartData,
    chartError,
    chartRange,
    favoriteCodes,
    isLivePolling,
    isPageVisible,
    isChartLoading,
    isMarketLoading,
    lastUpdatedLabel,
    marketError,
    marketList,
    marketSort,
    refreshIntervalMs,
    refreshAll,
    selectedMarket,
    selectMarket,
    setChartRange,
    setRefreshIntervalMs,
    setMarketSort,
    toggleFavorite,
  } = useMarketDashboard()
  const {
    accountSummary,
    error: paperError,
    isLoading: isPaperLoading,
    isSubmitting,
    orders,
    positions,
    resetPortfolio,
    selectedPosition,
    submitBuyOrder,
    submitSellOrder,
  } = usePaperTrading(marketList, selectedMarket)

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <div>
          <p className="eyebrow">Phase 2</p>
          <h1>Crypto Market Board</h1>
          <p className="hero-copy">
            자동매매 시스템의 첫 화면으로, 종목을 훑고 가격 흐름을 빠르게
            읽을 수 있는 웹 대시보드입니다.
          </p>
        </div>

        <div className="hero-actions">
          <button className="refresh-button" onClick={() => void refreshAll()}>
            데이터 새로고침
          </button>
          <div className="live-controls">
            <label className="live-controls-label" htmlFor="refresh-interval">
              실시간 갱신 방식
            </label>
            <select
              className="refresh-interval"
              id="refresh-interval"
              value={refreshIntervalMs}
              onChange={(event) => setRefreshIntervalMs(Number(event.target.value))}
            >
              {refreshOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <p className="status-note">
              {isLivePolling
                ? isPageVisible
                  ? `Polling ${refreshIntervalMs / 1000}초`
                  : '백그라운드 탭에서 자동 갱신 일시 중지'
                : '자동 갱신 꺼짐'}
            </p>
          </div>
          <p className="status-note">
            {lastUpdatedLabel
              ? `마지막 갱신 ${lastUpdatedLabel}`
              : '아직 데이터를 불러오는 중입니다.'}
          </p>
        </div>
      </section>

      <section className="dashboard-grid">
        <aside className="panel sidebar-panel">
          <header className="panel-header">
            <div>
              <p className="panel-eyebrow">Markets</p>
              <h2>KRW 코인 종목</h2>
            </div>
          </header>

          <MarketList
            error={marketError}
            isLoading={isMarketLoading}
            marketSort={marketSort}
            markets={marketList}
            selectedMarketCode={selectedMarket?.market ?? null}
            onSelect={selectMarket}
            onSortChange={setMarketSort}
            onToggleFavorite={toggleFavorite}
          />
        </aside>

        <section className="content-column">
          <article className="panel spotlight-panel">
            <header className="spotlight-header">
              <div>
                <p className="panel-eyebrow">Selected Market</p>
                <h2>
                  {selectedMarket
                    ? `${selectedMarket.koreanName} ${selectedMarket.market}`
                    : '종목을 고르는 중'}
                </h2>
                <p className="market-subtitle">
                  {selectedMarket?.englishName ?? '시장 데이터 API를 불러옵니다.'}
                </p>
                {favoriteCodes.includes(selectedMarket?.market ?? '') ? (
                  <p className="market-subtitle">관심 종목으로 고정됨</p>
                ) : null}
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

            <div className="metrics-grid">
              <MetricCard
                label="현재가"
                value={
                  selectedMarket
                    ? formatCurrency(selectedMarket.tradePrice)
                    : '-'
                }
                tone="neutral"
              />
              <MetricCard
                label="전일 대비"
                value={
                  selectedMarket
                    ? formatPercent(selectedMarket.signedChangeRate)
                    : '-'
                }
                tone={
                  !selectedMarket
                    ? 'neutral'
                    : selectedMarket.signedChangeRate >= 0
                      ? 'rise'
                      : 'fall'
                }
              />
              <MetricCard
                label="24시간 거래대금"
                value={
                  selectedMarket
                    ? formatCompactCurrency(selectedMarket.accTradePrice24h)
                    : '-'
                }
                tone="neutral"
              />
              <MetricCard
                label="당일 범위"
                value={
                  selectedMarket
                    ? `${formatCurrency(selectedMarket.lowPrice)} ~ ${formatCurrency(
                        selectedMarket.highPrice,
                      )}`
                    : '-'
                }
                tone="neutral"
              />
            </div>
          </article>

          <article className="panel chart-panel">
            <PriceChart
              data={chartData}
              error={chartError}
              isLoading={isChartLoading}
              marketLabel={selectedMarket?.market ?? '시장 데이터'}
              onRangeChange={setChartRange}
              range={chartRange}
            />
          </article>

          <article className="panel footnote-panel">
            <p>
              현재 2차 구현은 프론트가 거래소 경로를 직접 호출하지 않고{' '}
              <code>/api/markets</code> 계약만 사용합니다. 운영 배포에서는
              `coin/proxy`와 동일한 책임의 API 프록시를 붙이면 프론트
              기술스택과 백엔드 구현을 독립적으로 바꿀 수 있습니다.
            </p>
          </article>

          <PaperTradingSummary summary={accountSummary} />

          <section className="trade-grid">
            <OrderTicket
              isSubmitting={isSubmitting}
              onBuy={submitBuyOrder}
              onReset={resetPortfolio}
              onSell={submitSellOrder}
              selectedMarket={selectedMarket}
              selectedPosition={selectedPosition}
            />
            <PositionsTable positions={positions} />
          </section>

          <OrderHistory orders={orders} />

          {paperError && !isPaperLoading ? (
            <article className="panel footnote-panel">
              <p>Paper trading 상태를 불러오지 못했습니다: {paperError}</p>
            </article>
          ) : null}
        </section>
      </section>
    </main>
  )
}

export default App
