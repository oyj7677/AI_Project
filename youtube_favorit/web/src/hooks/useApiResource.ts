import { useEffect, useEffectEvent, useState } from 'react'
import { ApiClientError } from '../api/client'

type ResourcePhase = 'loading' | 'success' | 'error'

interface ResourceState<T> {
  phase: ResourcePhase
  data: T | null
  error: string | null
}

export function useApiResource<T>(
  loader: (signal: AbortSignal) => Promise<T>,
  dependencies: readonly unknown[],
) {
  const runLoader = useEffectEvent(loader)
  const dependencyKey = JSON.stringify(dependencies)
  const [state, setState] = useState<ResourceState<T>>({
    phase: 'loading',
    data: null,
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()
    runLoader(controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) {
          setState({
            phase: 'success',
            data,
            error: null,
          })
        }
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          setState({
            phase: 'error',
            data: null,
            error:
              error instanceof ApiClientError || error instanceof Error
                ? error.message
                : 'Unexpected request error.',
          })
        }
      })

    return () => controller.abort()
  }, [dependencyKey])

  return state
}
