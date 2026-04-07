import { createContext, useState } from 'react'
import { checkBackendConnectivity } from '../api/client'
import type { ConnectivityState } from '../types/api'
import {
  DEFAULT_BACKEND_URL,
  DEFAULT_OPERATOR_PASSWORD,
  DEFAULT_OPERATOR_USERNAME,
  loadBackendUrl,
  loadOperatorPassword,
  loadOperatorUsername,
  saveBackendUrl,
  saveOperatorPassword,
  saveOperatorUsername,
} from '../utils/storage'

interface DeveloperSettingsValue {
  backendUrl: string
  draftBackendUrl: string
  setDraftBackendUrl: (value: string) => void
  saveDraftBackendUrl: () => void
  operatorUsername: string
  operatorPassword: string
  draftOperatorUsername: string
  draftOperatorPassword: string
  setDraftOperatorUsername: (value: string) => void
  setDraftOperatorPassword: (value: string) => void
  saveOperatorCredentials: () => void
  connectivity: ConnectivityState
  checkConnectivity: () => Promise<void>
  refetchVersion: number
  triggerRefetch: () => void
}

const DeveloperSettingsContext = createContext<DeveloperSettingsValue | null>(null)

export function DeveloperSettingsProvider({
  children,
}: {
  children: React.ReactNode
}) {
  const [backendUrl, setBackendUrl] = useState(loadBackendUrl)
  const [draftBackendUrl, setDraftBackendUrl] = useState(loadBackendUrl)
  const [operatorUsername, setOperatorUsername] = useState(loadOperatorUsername)
  const [operatorPassword, setOperatorPassword] = useState(loadOperatorPassword)
  const [draftOperatorUsername, setDraftOperatorUsername] = useState(loadOperatorUsername)
  const [draftOperatorPassword, setDraftOperatorPassword] = useState(loadOperatorPassword)
  const [connectivity, setConnectivity] = useState<ConnectivityState>({ phase: 'idle' })
  const [refetchVersion, setRefetchVersion] = useState(0)

  async function checkConnectivity() {
    setConnectivity({ phase: 'checking' })
    const result = await checkBackendConnectivity(backendUrl)
    setConnectivity(result)
  }

  function saveDraftBackendUrl() {
    const next = draftBackendUrl.trim() || DEFAULT_BACKEND_URL
    setBackendUrl(next)
    saveBackendUrl(next)
    setConnectivity({ phase: 'idle' })
    setRefetchVersion((current) => current + 1)
  }

  function saveOperatorCredentials() {
    const nextUsername = draftOperatorUsername.trim() || DEFAULT_OPERATOR_USERNAME
    const nextPassword = draftOperatorPassword || DEFAULT_OPERATOR_PASSWORD
    setOperatorUsername(nextUsername)
    setOperatorPassword(nextPassword)
    saveOperatorUsername(nextUsername)
    saveOperatorPassword(nextPassword)
  }

  function triggerRefetch() {
    setRefetchVersion((current) => current + 1)
  }

  return (
    <DeveloperSettingsContext.Provider
      value={{
        backendUrl,
        draftBackendUrl,
        setDraftBackendUrl,
        saveDraftBackendUrl,
        operatorUsername,
        operatorPassword,
        draftOperatorUsername,
        draftOperatorPassword,
        setDraftOperatorUsername,
        setDraftOperatorPassword,
        saveOperatorCredentials,
        connectivity,
        checkConnectivity,
        refetchVersion,
        triggerRefetch,
      }}
    >
      {children}
    </DeveloperSettingsContext.Provider>
  )
}

export { DeveloperSettingsContext }
