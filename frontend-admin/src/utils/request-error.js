export function isBackendUnavailableError(error) {
  if (!error) return false
  if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') return true

  const status = error.response?.status
  return status >= 500
}
