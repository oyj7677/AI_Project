import { chartRanges } from '../lib/upbit'
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

function buildLinePath(data: ChartPoint[]) {
  if (data.length === 0) {
    return null
  }

  const width = 760
  const height = 320
  const padding = 22
  const prices = data.map((point) => point.close)
  const min = Math.min(...prices)
  const max = Math.max(...prices)
  const span = Math.max(max - min, 1)

  const points = data.map((point, index) => {
    const x =
      padding +
      (index / Math.max(data.length - 1, 1)) * (width - padding * 2)
    const y =
      height -
      padding -
      ((point.close - min) / span) * (height - padding * 2)

    return { x, y }
  })

  const line = points
    .map((point, index) =>
      `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`,
    )
    .join(' ')

  const area = `${line} L ${points.at(-1)?.x ?? padding} ${height - padding} L ${points[0]?.x ?? padding} ${height - padding} Z`

  return {
    area,
    height,
    line,
    max,
    min,
    points,
    width,
  }
}

export function PriceChart({
  data,
  error,
  isLoading,
  marketLabel,
  onRangeChange,
  range,
}: PriceChartProps) {
  const chart = buildLinePath(data)

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

        <div className="range-buttons">
          {chartRanges.map((option) => (
            <button
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

      <div className="chart-card">
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
              <circle
                className="last-dot"
                cx={chart.points.at(-1)?.x ?? 0}
                cy={chart.points.at(-1)?.y ?? 0}
                r="7"
              />
            </svg>

            <div className="chart-meta">
              <span>저점 {formatCurrency(chart.min)}</span>
              <span>고점 {formatCurrency(chart.max)}</span>
            </div>

            <div className="legend-row">
              <span>
                <span className="legend-bullet"></span>
                {data.length > 0
                  ? `${formatShortDateTime(data[0].timestamp)} ~ ${formatShortDateTime(
                      data.at(-1)?.timestamp ?? data[0].timestamp,
                    )}`
                  : '데이터 없음'}
              </span>
              <span>
                마지막 체결가{' '}
                {data.length > 0
                  ? formatCurrency(data.at(-1)?.close ?? 0)
                  : '-'}
              </span>
            </div>
          </>
        ) : null}
      </div>
    </>
  )
}
