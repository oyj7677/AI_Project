import { useContext } from 'react'
import { DeveloperSettingsContext } from './DeveloperSettingsContext'

export function useDeveloperSettings() {
  const context = useContext(DeveloperSettingsContext)
  if (!context) {
    throw new Error('useDeveloperSettings must be used within DeveloperSettingsProvider')
  }
  return context
}
