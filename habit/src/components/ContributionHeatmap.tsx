import type { HeatmapData } from '../lib/heatmap'

interface ContributionHeatmapProps {
  heatmap: HeatmapData
}

const DAY_LABELS = ['월', '화', '수', '목', '금', '토', '일']

export function ContributionHeatmap({ heatmap }: ContributionHeatmapProps) {
  return (
    <section className="heatmap-panel" aria-labelledby="heatmap-title">
      <div className="heatmap-panel__header">
        <div>
          <p className="eyebrow">활동 잔디</p>
          <h2 id="heatmap-title">최근 흐름을 잔디처럼 살펴보세요.</h2>
        </div>
        <p className="section-note">최근 {heatmap.weeks.length}주 기준</p>
      </div>

      <div className="heatmap-panel__footer">
        <div className="heatmap-summary__item">
          <span className="metric__label">누적 체크인</span>
          <strong>{heatmap.totalCompletions}</strong>
        </div>
        <div className="heatmap-summary__item">
          <span className="metric__label">연속 활동</span>
          <strong>{heatmap.streakDaysActive}</strong>
        </div>
        <div className="heatmap-summary__item">
          <span className="metric__label">가장 바빴던 날</span>
          <strong>{heatmap.maxCount}</strong>
        </div>
      </div>

      <div className="heatmap-grid">
        <div className="heatmap-grid__months">
          <span />
          {heatmap.weeks.map((week, index) => (
            <span key={`${heatmap.monthLabels[index] ?? 'blank'}-${week.index}`}>
              {heatmap.monthLabels[index] ?? ''}
            </span>
          ))}
        </div>

        <div className="heatmap-grid__body">
          <div className="heatmap-grid__day-labels" aria-hidden="true">
            {DAY_LABELS.map((label, index) => (
              <span key={label}>{index % 2 === 0 ? label : ''}</span>
            ))}
          </div>

          <div className="heatmap-grid__weeks">
            {heatmap.weeks.map((week) => (
              <div key={`week-${week.index}`} className="heatmap-grid__week">
                {week.cells.map((day) => (
                  <div
                    key={day.date}
                    className={`heatmap-cell heatmap-cell--${day.intensity} ${
                      day.isToday ? 'heatmap-cell--today' : ''
                    } ${day.isFuture ? 'heatmap-cell--future' : ''}`}
                    title={`${day.date} · ${day.count}회`}
                    aria-label={`${day.date}에 ${day.count}회 완료`}
                  />
                ))}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="heatmap-legend" aria-hidden="true">
        <span>적음</span>
        {[0, 1, 2, 3, 4].map((level) => (
          <span
            key={level}
            className={`heatmap-legend__swatch heatmap-cell heatmap-cell--${level}`}
          />
        ))}
        <span>많음</span>
      </div>
    </section>
  )
}
