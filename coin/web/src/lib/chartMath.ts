import type { ChartPoint } from '../types/market'

export interface ChartGeometry {
  area: string
  height: number
  line: string
  max: number
  min: number
  points: Array<{ x: number; y: number }>
  width: number
}

export function buildChartGeometry(data: ChartPoint[]): ChartGeometry | null {
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

export function getNearestPointIndex(
  geometry: ChartGeometry,
  pointerX: number,
): number {
  let nearestIndex = 0
  let nearestDistance = Infinity

  geometry.points.forEach((point, index) => {
    const distance = Math.abs(point.x - pointerX)
    if (distance < nearestDistance) {
      nearestDistance = distance
      nearestIndex = index
    }
  })

  return nearestIndex
}
