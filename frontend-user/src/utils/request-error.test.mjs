import assert from 'node:assert/strict'
import { isBackendUnavailableError } from './request-error.js'

assert.equal(isBackendUnavailableError({ code: 'ERR_NETWORK' }), true)
assert.equal(isBackendUnavailableError({ code: 'ECONNREFUSED' }), true)
assert.equal(isBackendUnavailableError({ response: { status: 500 } }), true)
assert.equal(isBackendUnavailableError({ response: { status: 502 } }), true)
assert.equal(isBackendUnavailableError({ response: { status: 401 } }), false)
assert.equal(isBackendUnavailableError({ response: { status: 400 } }), false)

console.log('request-error tests passed')
