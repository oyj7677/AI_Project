import type { CategorySummary, HabitWithMetrics } from '../hooks/useHabitTracker'

interface InsightsPanelProps {
  consistencyLabel: string
  streakLeader: HabitWithMetrics | null
  categorySummary: CategorySummary[]
}

export function InsightsPanel({
  consistencyLabel,
  streakLeader,
  categorySummary,
}: InsightsPanelProps) {
  return (
    <section className="insights-panel" aria-labelledby="insights-panel-title">
      <div className="section-heading">
        <div>
          <p className="eyebrow">인사이트</p>
          <h2 id="insights-panel-title">루틴의 흐름을 놓치기 전에 확인하세요.</h2>
        </div>
      </div>

      {categorySummary.length === 0 ? (
        <div className="empty-state">
          <h3>아직 인사이트가 없어요</h3>
          <p>습관을 만들면 카테고리별 진행 현황이 자동으로 정리됩니다.</p>
        </div>
      ) : (
        <ul className="insight-list">
          {categorySummary.map((category) => (
            <li key={category.label} className="insight-list__item">
              <div className="insight-copy">
                <strong>{category.label}</strong>
                <span className="insight-list__meta">
                  {category.habitCount}개 중 {category.onTrackCount}개 진행 중
                </span>
              </div>
              <span className="insight-list__count">{category.habitCount}</span>
            </li>
          ))}
        </ul>
      )}

      <div className="insight-callout">
        <strong>
          {streakLeader ? `${streakLeader.title} 습관이 가장 앞서고 있어요.` : '꾸준함은 작은 반복에서 시작돼요.'}
        </strong>
        <p>
          {streakLeader
            ? `현재 ${streakLeader.currentStreak}회 연속, 최고 ${streakLeader.longestStreak}회 연속 기록이에요. ${consistencyLabel}.`
            : '첫 체크인을 몇 번만 쌓아도 이 패널이 가장 강한 루틴을 바로 보여줄 거예요.'}
        </p>
      </div>
    </section>
  )
}
