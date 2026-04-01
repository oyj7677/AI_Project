import { describe, expect, it } from 'vitest'
import { buildChartGeometry, getNearestPointIndex } from './chartMath'

describe('buildChartGeometry', () => {
  it('returns geometry for chart rendering', () => {
    const geometry = buildChartGeometry([
      { timestamp: 1, open: 1, high: 2, low: 1, close: 2, volume: 10 },
      { timestamp: 2, open: 2, high: 3, low: 2, close: 3, volume: 10 },
    ])

    expect(geometry?.points).toHaveLength(2)
    expect(geometry?.line.startsWith('M')).toBe(true)
    expect(geometry?.max).toBe(3)
    expect(geometry?.min).toBe(2)
  })
})

describe('getNearestPointIndex', () => {
  it('finds the closest point to the pointer x coordinate', () => {
    const geometry = buildChartGeometry([
      { timestamp: 1, open: 1, high: 2, low: 1, close: 2, volume: 10 },
      { timestamp: 2, open: 2, high: 3, low: 2, close: 3, volume: 10 },
      { timestamp: 3, open: 3, high: 4, low: 3, close: 4, volume: 10 },
    ])

    expect(geometry).not.toBeNull()
    expect(getNearestPointIndex(geometry!, geometry!.points[1].x + 4)).toBe(1)
  })
})
