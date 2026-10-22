package aktual.prefs

interface AppLockAuthenticator {
  // False when the device has no screen lock, or can't authenticate at all
  fun isAvailable(): Boolean

  // Shows the system prompt, returning true if the user unlocked
  suspend fun authenticate(): Boolean
}
