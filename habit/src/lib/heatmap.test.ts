import { describe, expect, it } from 'vitest'
import { buildContributionHeatmap } from './heatmap'

describe('buildContributionHeatmap', () => {
  it('builds a rolling monday-first week grid', () => {
    const heatmap = buildContributionHeatmap(
      [
        { date: '2026-04-06' },
        { date: '2026-04-07' },
        { date: '2026-04-07' },
      ],
      {
        weeks: 2,
        referenceDate: new Date(2026, 3, 8),
      },
    )

    expect(heatmap.weeks).toHaveLength(2)
    expect(heatmap.weeks[1]?.cells[0]?.date).toBe('2026-04-06')
    expect(heatmap.weeks[1]?.cells[1]?.date).toBe('2026-04-07')
    expect(heatmap.totalCompletions).toBe(3)
    expect(heatmap.maxCount).toBe(2)
    expect(heatmap.streakDaysActive).toBe(2)
  })

  it('assigns github-like intensity buckets and flags today', () => {
    const heatmap = buildContributionHeatmap(
      [
        { date: '2026-04-08' },
        { date: '2026-04-08' },
        { date: '2026-04-08' },
        { date: '2026-04-08' },
      ],
      {
        weeks: 1,
        referenceDate: new Date(2026, 3, 8),
      },
    )

    const todayCell = heatmap.weeks[0]?.cells[2]
    expect(todayCell).toMatchObject({
      date: '2026-04-08',
      count: 4,
      intensity: 4,
      isToday: true,
      isFuture: false,
    })
  })

  it('marks days after the reference date as future cells', () => {
    const heatmap = buildContributionHeatmap([], {
      weeks: 1,
      referenceDate: new Date(2026, 3, 8),
    })

    expect(heatmap.weeks[0]?.cells[3]?.isFuture).toBe(true)
    expect(heatmap.weeks[0]?.cells[0]?.isFuture).toBe(false)
  })

  it('adds month labels only when the month changes', () => {
    const heatmap = buildContributionHeatmap([], {
      weeks: 5,
      referenceDate: new Date(2026, 3, 8),
    })

    expect(heatmap.monthLabels[0]).not.toBe('')
    const hasMonthBoundary = heatmap.monthLabels.some((label, index, labels) => {
      return index > 0 && label !== '' && label !== labels[index - 1]
    })

    expect(hasMonthBoundary).toBe(true)
  })
})
