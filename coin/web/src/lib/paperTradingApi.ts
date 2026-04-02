import type {
  PaperOrder,
  PaperPortfolio,
} from '../types/paperTrading'
import type { SubmitPaperOrderInput } from '../contracts/paperTrading'

const API_BASE = import.meta.env.VITE_MARKET_API_BASE ?? '/api'

async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })
  if (!response.ok) {
    throw new Error(`Paper trading API request failed: ${response.status}`)
  }
  return response.json() as Promise<T>
}

export function fetchPaperPortfolio() {
  return fetchJson<PaperPortfolio>('/paper/portfolio')
}

export async function fetchPaperOrders() {
  const payload = await fetchJson<{ orders: PaperOrder[] }>('/paper/orders')
  return payload.orders
}

export function submitPaperOrder(input: SubmitPaperOrderInput) {
  return fetchJson<{ order: PaperOrder; portfolio: PaperPortfolio }>(
    '/paper/orders',
    {
      method: 'POST',
      body: JSON.stringify(input),
    },
  )
}

export async function resetPaperPortfolio() {
  await fetchJson('/paper/reset', {
    method: 'POST',
    body: JSON.stringify({}),
  })
}
