import test from 'node:test'
import assert from 'node:assert/strict'
import {
  applyBuyOrder,
  applySellOrder,
  resetPaperState,
} from '../src/paperState.mjs'

test('applyBuyOrder creates a position and reduces cash', () => {
  const state = resetPaperState()
  const order = applyBuyOrder(state, {
    market: 'KRW-BTC',
    amountKrw: 1000000,
    price: 100000000,
  })

  assert.equal(order.side, 'buy')
  assert.equal(state.positions.length, 1)
  assert.ok(state.cashBalance < state.initialCashBalance)
})

test('applySellOrder realizes pnl and reduces held quantity', () => {
  const state = resetPaperState()
  applyBuyOrder(state, {
    market: 'KRW-BTC',
    amountKrw: 1000000,
    price: 100000000,
  })

  const order = applySellOrder(state, {
    market: 'KRW-BTC',
    quantity: 0.005,
    price: 110000000,
  })

  assert.equal(order.side, 'sell')
  assert.ok(state.realizedPnl > 0)
  assert.ok(state.positions[0].quantity < 0.01)
})
