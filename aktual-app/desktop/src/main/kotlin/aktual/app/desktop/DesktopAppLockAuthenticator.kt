package aktual.app.desktop

import aktual.di.AppScope
import aktual.prefs.AppLockAuthenticator
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

// The app lock is Android only
@Inject
@ContributesBinding(AppScope::class)
class DesktopAppLockAuthenticator : AppLockAuthenticator {
  override fun isAvailable(): Boolean = false

  override suspend fun authenticate(): Boolean = false
}
