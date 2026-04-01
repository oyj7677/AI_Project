import './App.css'
import { MarketList } from './components/MarketList'
import { MetricCard } from './components/MetricCard'
import { PriceChart } from './components/PriceChart'
import { useMarketDashboard } from './hooks/useMarketDashboard'
import { formatCompactCurrency, formatCurrency, formatPercent } from './lib/format'

function App() {
  const {
    chartData,
    chartError,
    chartRange,
    isChartLoading,
    isMarketLoading,
    lastUpdatedLabel,
    marketError,
    markets,
    refreshAll,
    selectedMarket,
    selectMarket,
    setChartRange,
  } = useMarketDashboard()

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <div>
          <p className="eyebrow">Phase 1</p>
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
            markets={markets}
            selectedMarketCode={selectedMarket?.market ?? null}
            onSelect={selectMarket}
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
                  {selectedMarket?.englishName ?? 'Upbit 공개 시세를 불러옵니다.'}
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
              현재 1차 구현은 Upbit 공개 시세를 기준으로 동작합니다. 브라우저
              CORS 제한 때문에 프론트에서는 <code>/api/upbit</code> 프록시
              경로를 사용하며, 운영 배포 시에는 동일한 역할의 얇은 백엔드
              프록시가 필요합니다.
            </p>
          </article>
        </section>
      </section>
    </main>
  )
}

export default App
