import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { dirname } from 'node:path'

const DEFAULT_INITIAL_CASH = Number(process.env.PAPER_INITIAL_CASH ?? '10000000')
const DEFAULT_FEE_RATE = Number(process.env.PAPER_FEE_RATE ?? '0.0005')

function nowIso() {
  return new Date().toISOString()
}

function makeDefaultState() {
  return {
    initialCashBalance: DEFAULT_INITIAL_CASH,
    cashBalance: DEFAULT_INITIAL_CASH,
    realizedPnl: 0,
    feeRate: DEFAULT_FEE_RATE,
    positions: [],
    orders: [],
    updatedAt: nowIso(),
  }
}

export async function loadPaperState(filePath) {
  try {
    const raw = await readFile(filePath, 'utf-8')
    return JSON.parse(raw)
  } catch {
    return makeDefaultState()
  }
}

export async function savePaperState(filePath, state) {
  await mkdir(dirname(filePath), { recursive: true })
  await writeFile(filePath, JSON.stringify(state, null, 2))
}

function nextOrderId(state) {
  return `paper-${state.orders.length + 1}`
}

function round(value) {
  return Number(value.toFixed(8))
}

export function getPosition(state, market) {
  return state.positions.find((position) => position.market === market) ?? null
}

function upsertPosition(state, nextPosition) {
  const index = state.positions.findIndex((position) => position.market === nextPosition.market)
  if (index === -1) {
    state.positions.push(nextPosition)
    return
  }

  state.positions[index] = nextPosition
}

export function applyBuyOrder(state, { market, amountKrw, price }) {
  const feeAmount = amountKrw * state.feeRate
  const totalCost = amountKrw + feeAmount
  if (amountKrw <= 0) {
    throw new Error('Buy amount must be greater than zero')
  }
  if (state.cashBalance < totalCost) {
    throw new Error('Insufficient cash balance')
  }

  const quantity = round(amountKrw / price)
  const position = getPosition(state, market)

  if (!position) {
    upsertPosition(state, {
      market,
      quantity,
      averageEntryPrice: price,
      costBasis: amountKrw,
      updatedAt: nowIso(),
    })
  } else {
    const nextQuantity = round(position.quantity + quantity)
    const nextCostBasis = round(position.costBasis + amountKrw)
    upsertPosition(state, {
      ...position,
      quantity: nextQuantity,
      averageEntryPrice: round(nextCostBasis / nextQuantity),
      costBasis: nextCostBasis,
      updatedAt: nowIso(),
    })
  }

  state.cashBalance = round(state.cashBalance - totalCost)

  const order = {
    id: nextOrderId(state),
    market,
    side: 'buy',
    orderType: 'market',
    requestedAt: nowIso(),
    filledAt: nowIso(),
    status: 'filled',
    fillPrice: price,
    quantity,
    grossAmount: round(amountKrw),
    feeAmount: round(feeAmount),
    netAmount: round(totalCost),
  }

  state.orders.unshift(order)
  state.updatedAt = nowIso()
  return order
}

export function applySellOrder(state, { market, quantity, price }) {
  if (quantity <= 0) {
    throw new Error('Sell quantity must be greater than zero')
  }

  const position = getPosition(state, market)
  if (!position) {
    throw new Error('No position exists for this market')
  }
  if (position.quantity < quantity) {
    throw new Error('Sell quantity exceeds held position')
  }

  const grossAmount = round(quantity * price)
  const feeAmount = round(grossAmount * state.feeRate)
  const netAmount = round(grossAmount - feeAmount)
  const realizedPnl = round(netAmount - position.averageEntryPrice * quantity)
  const nextQuantity = round(position.quantity - quantity)
  const nextCostBasis = round(position.costBasis - position.averageEntryPrice * quantity)

  if (nextQuantity <= 0.00000001) {
    state.positions = state.positions.filter((entry) => entry.market !== market)
  } else {
    upsertPosition(state, {
      ...position,
      quantity: nextQuantity,
      costBasis: nextCostBasis,
      averageEntryPrice: round(nextCostBasis / nextQuantity),
      updatedAt: nowIso(),
    })
  }

  state.cashBalance = round(state.cashBalance + netAmount)
  state.realizedPnl = round(state.realizedPnl + realizedPnl)

  const order = {
    id: nextOrderId(state),
    market,
    side: 'sell',
    orderType: 'market',
    requestedAt: nowIso(),
    filledAt: nowIso(),
    status: 'filled',
    fillPrice: price,
    quantity: round(quantity),
    grossAmount,
    feeAmount,
    netAmount,
  }

  state.orders.unshift(order)
  state.updatedAt = nowIso()
  return order
}

export function resetPaperState() {
  return makeDefaultState()
}
