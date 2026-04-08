import { useEffect, useState, type CSSProperties } from 'react'
import './App.css'
import { ContributionHeatmap } from './components/ContributionHeatmap'
import { HabitForm } from './components/HabitForm'
import { HabitList } from './components/HabitList'
import { HistoryPanel } from './components/HistoryPanel'
import { InsightsPanel } from './components/InsightsPanel'
import { StatCard } from './components/StatCard'
import { ThemeToggle } from './components/ThemeToggle'
import { useHabitTracker } from './hooks/useHabitTracker'

function App() {
  const [isComposerOpen, setComposerOpen] = useState(false)
  const {
    theme,
    habits,
    dashboardStats,
    heatmap,
    recentActivity,
    categorySummary,
    streakLeader,
    consistencyLabel,
    syncBanner,
    addHabit,
    toggleHabitCompletion,
    deleteHabit,
    toggleTheme,
  } = useHabitTracker()

  useEffect(() => {
    if (!isComposerOpen) {
      return
    }

    function handleKeydown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setComposerOpen(false)
      }
    }

    window.addEventListener('keydown', handleKeydown)
    return () => window.removeEventListener('keydown', handleKeydown)
  }, [isComposerOpen])

  const completionRatio =
    dashboardStats.scheduledToday === 0
      ? 0
      : dashboardStats.onTrackToday / dashboardStats.scheduledToday

  const pendingCount = habits.filter(
    (habit) => habit.isScheduledToday && !habit.isCompleteToday,
  ).length

  const progressMessage =
    dashboardStats.totalHabits === 0
      ? '첫 습관을 추가하면 메인 화면이 바로 보드와 잔디로 채워져요.'
      : pendingCount === 0
        ? '오늘 예정된 습관을 모두 완료했어요.'
        : `아직 체크인이 필요한 습관이 ${pendingCount}개 있어요.`

  const progressStyle = {
    '--progress': `${Math.round(completionRatio * 360)}deg`,
  } as CSSProperties

  const todayHabits = habits.filter((habit) => habit.isScheduledToday)
  const focusHabits = (todayHabits.length > 0 ? todayHabits : habits).slice(0, 4)

  function openComposer() {
    setComposerOpen(true)
  }

  function closeComposer() {
    setComposerOpen(false)
  }

  async function handleCreateHabit(values: Parameters<typeof addHabit>[0]) {
    await addHabit(values)
    setComposerOpen(false)
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar__copy">
          <p className="eyebrow">리듬 습관 스튜디오</p>
          <h1>첫 화면은 습관과 흐름부터 보이게 만들었습니다.</h1>
          <p className="topbar__summary">
            해야 할 습관, 최근 활동 잔디, 전체 진행 상황을 먼저 보고 필요할 때만
            습관을 추가하세요.
          </p>
        </div>

        <div className="topbar__actions">
          <button type="button" className="primary-action" onClick={openComposer}>
            습관 추가
          </button>
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
        </div>
      </header>

      {syncBanner ? (
        <section
          className={`sync-banner sync-banner--${syncBanner.tone}`}
          role={syncBanner.tone === 'warning' ? 'alert' : 'status'}
        >
          <strong>{syncBanner.title}</strong>
          <p>{syncBanner.message}</p>
        </section>
      ) : null}

      <main className="layout">
        <section className="overview-grid">
          <section className="panel summary-panel">
            <div className="hero-copy">
              <p className="eyebrow">오늘의 포커스</p>
              <h2>지금 봐야 할 습관을 가장 위에 두었습니다.</h2>
              <p className="hero-text">
                체크인이 필요한 습관이 먼저 보이고, 연속 기록과 오늘 진행률이
                함께 보이도록 구성했습니다.
              </p>

              <div className="focus-habit-list">
                {focusHabits.length > 0 ? (
                  focusHabits.map((habit) => (
                    <div key={habit.id} className="focus-habit">
                      <div className="focus-habit__copy">
                        <strong>{habit.title}</strong>
                        <span>
                          {habit.category} ·{' '}
                          {habit.isCompleteToday
                            ? '완료됨'
                            : habit.isScheduledToday
                              ? '오늘 할 일'
                              : '대기 중'}
                        </span>
                      </div>
                      <span className="focus-habit__metric">
                        {habit.currentStreak}일 연속
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="focus-empty">
                    <strong>아직 습관이 없어요</strong>
                    <p>우측 상단의 버튼으로 첫 습관을 추가해 보세요.</p>
                  </div>
                )}
              </div>
            </div>

            <div className="hero-progress">
              <div className="progress-ring" style={progressStyle}>
                <span>{Math.round(completionRatio * 100)}%</span>
              </div>
              <div className="progress-copy">
                <strong>
                  {dashboardStats.onTrackToday}/{dashboardStats.scheduledToday || 0}
                </strong>
                <p>{progressMessage}</p>
              </div>
            </div>
          </section>

          <ContributionHeatmap heatmap={heatmap} />
        </section>

        <section className="stat-grid" aria-label="진행 현황">
          <StatCard
            label="전체 습관"
            value={dashboardStats.totalHabits.toString()}
            helper="현재 추적 중인 루틴 수"
            tone="accent"
          />
          <StatCard
            label="오늘 예정"
            value={dashboardStats.scheduledToday.toString()}
            helper="오늘 진행해야 하는 습관"
          />
          <StatCard
            label="완료됨"
            value={dashboardStats.onTrackToday.toString()}
            helper="오늘 체크인을 마친 습관"
          />
          <StatCard
            label="최고 연속"
            value={dashboardStats.bestCurrentStreak.toString()}
            helper={
              streakLeader
                ? `${streakLeader.title} 습관이 가장 앞서 있어요`
                : '반복 체크인이 쌓이면 연속 기록이 표시돼요'
            }
          />
          <StatCard
            label="최근 7일"
            value={dashboardStats.completionsLast7Days.toString()}
            helper="이번 주 누적 체크인"
          />
          <StatCard
            label="최근 30일"
            value={dashboardStats.completionsLast30Days.toString()}
            helper={consistencyLabel}
          />
        </section>

        <div className="content-grid">
          <div className="column">
            <section className="panel section-panel">
              <div className="section-heading">
                <div>
                  <p className="eyebrow">습관 보드</p>
                  <h2>오늘 해야 할 일부터 전체 습관까지 한눈에 살펴보세요.</h2>
                </div>

                <div className="section-actions">
                  <p className="section-note">
                    체크인이 필요한 습관이 위로 올라와요.
                  </p>
                  <button
                    type="button"
                    className="secondary-action"
                    onClick={openComposer}
                  >
                    새 습관
                  </button>
                </div>
              </div>

              <HabitList
                habits={habits}
                onToggleCompletion={toggleHabitCompletion}
                onDelete={deleteHabit}
                onCreateHabit={openComposer}
              />
            </section>
          </div>

          <div className="column column--wide">
            <HistoryPanel entries={recentActivity} />
            <InsightsPanel
              consistencyLabel={consistencyLabel}
              streakLeader={streakLeader}
              categorySummary={categorySummary}
            />
          </div>
        </div>
      </main>

      {isComposerOpen ? (
        <div className="modal-backdrop" onClick={closeComposer}>
          <div
            className="modal-shell"
            role="dialog"
            aria-modal="true"
            aria-labelledby="habit-compose-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="modal-shell__header">
              <div>
                <p className="eyebrow">새 습관</p>
                <h2 id="habit-compose-title">메인 화면을 벗어나지 않고 추가하세요.</h2>
                <p className="section-note">
                  첫 화면은 보는 데 집중하고, 추가는 필요할 때만 열리도록 바꿨습니다.
                </p>
              </div>

              <button
                type="button"
                className="modal-shell__close"
                onClick={closeComposer}
              >
                닫기
              </button>
            </div>

            <HabitForm
              onSubmit={handleCreateHabit}
              wrapInPanel={false}
              showHeading={false}
              onCancel={closeComposer}
            />
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default App
