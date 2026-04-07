import { useDeveloperSettings } from '../app/useDeveloperSettings'

export function ConnectionBanner() {
  const { connectivity } = useDeveloperSettings()

  if (connectivity.phase !== 'offline') {
    return null
  }

  return (
    <div className="connection-banner">
      <strong>백엔드에 연결할 수 없습니다.</strong> {connectivity.message}
    </div>
  )
}
