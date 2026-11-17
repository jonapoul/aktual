package aktual.core.utils

import aktual.core.model.ServerUrl
import okio.IOException

// Android 17 blocks connections to the local network for apps targeting it, unless the user gives
// the "Nearby devices" permission. Blocked TCP connections just time out, with no specific error
interface LocalNetworkPermission {
  // True when it's been given, or there's nothing to give: on desktop, and on Android versions
  // without the restriction. Cheap, so callers can check it again without resolving the host
  fun isGranted(): Boolean

  // True when reaching the server needs the permission. Can do a DNS lookup
  suspend fun isRequiredFor(url: ServerUrl): Boolean

  // Same as isRequiredFor, for a bare host such as a request's
  suspend fun isRequiredForHost(host: String): Boolean
}

// True when reaching the server needs the permission and it hasn't been given
suspend fun LocalNetworkPermission.isMissingFor(url: ServerUrl): Boolean =
  !isGranted() && isRequiredFor(url)

// Tracks the permission prompt shown at startup, so requests to the server wait for the user's
// answer instead of timing out while it's showing
interface LocalNetworkPrompt {
  // Returns straight away when no prompt is showing
  suspend fun awaitAnswer()

  fun onShowing()

  fun onAnswered()
}

// A request to a local network host failed while the permission is missing
class LocalNetworkBlockedException(message: String, cause: Throwable) : IOException(message, cause)
