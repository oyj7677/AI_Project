import { useMemo, useState } from 'react'
import { buildChartGeometry, getNearestPointIndex } from '../lib/chartMath'
import { chartRanges } from '../lib/chartRanges'
import { formatCurrency, formatShortDateTime } from '../lib/format'
import type { ChartPoint, ChartRange } from '../types/market'

interface PriceChartProps {
  data: ChartPoint[]
  error: string | null
  isLoading: boolean
  marketLabel: string
  onRangeChange: (range: ChartRange) => void
  range: ChartRange
}

export function PriceChart({
  data,
  error,
  isLoading,
  marketLabel,
  onRangeChange,
  range,
}: PriceChartProps) {
  const [activeIndex, setActiveIndex] = useState<number | null>(null)
  const chart = useMemo(() => buildChartGeometry(data), [data])
  const effectiveIndex =
    activeIndex === null ? Math.max(data.length - 1, 0) : activeIndex
  const activePoint = data[effectiveIndex]
  const activeCoordinates = chart?.points[effectiveIndex]

  function updateActiveIndex(pointerX: number) {
    if (!chart) {
      return
    }
    setActiveIndex(getNearestPointIndex(chart, pointerX))
  }

  return (
    <>
      <header className="chart-header">
        <div>
          <p className="panel-eyebrow">Price Chart</p>
          <h2>{marketLabel} 그래프</h2>
          <p className="chart-subtitle">
            시장 흐름을 먼저 읽고, 이후 전략 패널을 이 영역에 연결할 수 있도록
            설계했습니다.
          </p>
        </div>

        <div
          aria-label="차트 시간 범위 선택"
          className="range-buttons"
          role="group"
        >
          {chartRanges.map((option) => (
            <button
              aria-pressed={option.value === range}
              className={`range-button ${option.value === range ? 'active' : ''}`}
              key={option.value}
              onClick={() => onRangeChange(option.value)}
              type="button"
            >
              {option.label}
            </button>
          ))}
        </div>
      </header>

      <div
        className="chart-card"
        onBlur={() => setActiveIndex(null)}
        onKeyDown={(event) => {
          if (!chart || data.length === 0) {
            return
          }

          if (event.key === 'ArrowLeft') {
            event.preventDefault()
            setActiveIndex((current) => Math.max((current ?? data.length - 1) - 1, 0))
          }

          if (event.key === 'ArrowRight') {
            event.preventDefault()
            setActiveIndex((current) =>
              Math.min((current ?? data.length - 1) + 1, data.length - 1),
            )
          }
        }}
        tabIndex={0}
      >
        {isLoading ? (
          <div className="empty-state">
            <p>차트 데이터를 불러오는 중입니다.</p>
          </div>
        ) : null}

        {!isLoading && error ? (
          <div className="error-state">
            <p>차트를 불러오지 못했습니다.</p>
            <p>{error}</p>
          </div>
        ) : null}

        {!isLoading && !error && chart ? (
          <>
            <svg
              className="chart-svg"
              role="img"
              aria-label={`${marketLabel} 가격 추이`}
              viewBox={`0 0 ${chart.width} ${chart.height}`}
            >
              <defs>
                <linearGradient
                  id="priceAreaGradient"
                  x1="0"
                  x2="0"
                  y1="0"
                  y2="1"
                >
                  <stop offset="0%" stopColor="rgba(101, 243, 255, 0.32)" />
                  <stop offset="100%" stopColor="rgba(101, 243, 255, 0.01)" />
                </linearGradient>
              </defs>

              {[0.2, 0.4, 0.6, 0.8].map((step) => (
                <line
                  className="grid-line"
                  key={step}
                  x1="0"
                  x2={chart.width}
                  y1={chart.height * step}
                  y2={chart.height * step}
                />
              ))}

              <path className="area-fill" d={chart.area} />
              <path className="price-line" d={chart.line} />
              {activeCoordinates ? (
                <>
                  <line
                    className="crosshair-line"
                    x1={activeCoordinates.x}
                    x2={activeCoordinates.x}
                    y1="0"
                    y2={chart.height}
                  />
                  <line
                    className="crosshair-line horizontal"
                    x1="0"
                    x2={chart.width}
                    y1={activeCoordinates.y}
                    y2={activeCoordinates.y}
                  />
                </>
              ) : null}
              <circle
                className="last-dot"
                cx={chart.points.at(-1)?.x ?? 0}
                cy={chart.points.at(-1)?.y ?? 0}
                r="7"
              />
              {activeCoordinates ? (
                <circle
                  className="hover-dot"
                  cx={activeCoordinates.x}
                  cy={activeCoordinates.y}
                  r="8"
                />
              ) : null}
              <rect
                className="chart-hitbox"
                fill="transparent"
                height={chart.height}
                onMouseLeave={() => setActiveIndex(null)}
                onMouseMove={(event) => {
                  const bounds = event.currentTarget.getBoundingClientRect()
                  const pointerX =
                    ((event.clientX - bounds.left) / bounds.width) * chart.width
                  updateActiveIndex(pointerX)
                }}
                width={chart.width}
                x="0"
                y="0"
              />
            </svg>

            {activePoint && activeCoordinates ? (
              <div
                className="chart-tooltip"
                style={{
                  left: `${Math.min(
                    Math.max((activeCoordinates.x / chart.width) * 100, 14),
                    86,
                  )}%`,
                }}
              >
                <strong>{formatCurrency(activePoint.close)}</strong>
                <span>{formatShortDateTime(activePoint.timestamp)}</span>
                <span>거래량 {activePoint.volume.toFixed(2)}</span>
              </div>
            ) : null}

            <div className="chart-meta">
              <span>저점 {formatCurrency(chart.min)}</span>
              <span>고점 {formatCurrency(chart.max)}</span>
            </div>

            <div className="legend-row">
              <span>좌우 화살표로 포인트 탐색 가능</span>
              <span>
                <span className="legend-bullet"></span>
                {data.length > 0
                  ? `${formatShortDateTime(data[0].timestamp)} ~ ${formatShortDateTime(
                      data.at(-1)?.timestamp ?? data[0].timestamp,
                    )}`
                  : '데이터 없음'}
              </span>
              <span>
                현재 선택값{' '}
                {activePoint ? formatCurrency(activePoint.close) : '-'}
              </span>
            </div>
          </>
        ) : null}
      </div>
    </>
  )
}
