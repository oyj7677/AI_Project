import type { ChartRange } from '../types/market'

export const chartRanges: Array<{ value: ChartRange; label: string }> = [
  { value: '1H', label: '1시간' },
  { value: '4H', label: '4시간' },
  { value: '1D', label: '1일' },
  { value: '1W', label: '1주' },
]
