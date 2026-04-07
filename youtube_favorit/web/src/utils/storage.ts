const backendUrlKey = 'mystarnow.web.backendUrl'
const operatorUsernameKey = 'mystarnow.web.operatorUsername'
const operatorPasswordKey = 'mystarnow.web.operatorPassword'

export const DEFAULT_BACKEND_URL = 'http://localhost:8080'
export const DEFAULT_OPERATOR_USERNAME = 'operator'
export const DEFAULT_OPERATOR_PASSWORD = 'operator'

export function loadBackendUrl() {
  if (typeof window === 'undefined') {
    return DEFAULT_BACKEND_URL
  }
  return window.localStorage.getItem(backendUrlKey) || DEFAULT_BACKEND_URL
}

export function saveBackendUrl(value: string) {
  window.localStorage.setItem(backendUrlKey, value)
}

export function loadOperatorUsername() {
  if (typeof window === 'undefined') {
    return DEFAULT_OPERATOR_USERNAME
  }
  return window.localStorage.getItem(operatorUsernameKey) || DEFAULT_OPERATOR_USERNAME
}

export function saveOperatorUsername(value: string) {
  window.localStorage.setItem(operatorUsernameKey, value)
}

export function loadOperatorPassword() {
  if (typeof window === 'undefined') {
    return DEFAULT_OPERATOR_PASSWORD
  }
  return window.localStorage.getItem(operatorPasswordKey) || DEFAULT_OPERATOR_PASSWORD
}

export function saveOperatorPassword(value: string) {
  window.localStorage.setItem(operatorPasswordKey, value)
}
