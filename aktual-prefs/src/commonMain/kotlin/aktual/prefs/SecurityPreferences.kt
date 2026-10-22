package aktual.prefs

interface SecurityPreferences {
  val appLockEnabled: Preference<Boolean>
  val appLockTimeout: Preference<AppLockTimeout>
}
