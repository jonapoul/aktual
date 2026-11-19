package aktual.core.updates

import aktual.di.AppScope
import aktual.updates.domain.ApkInstaller
import dev.zacsweers.metro.ContributesBinding
import okio.Path

@ContributesBinding(AppScope::class)
class JvmApkInstaller : ApkInstaller {
  override val isSupported: Boolean = false

  override suspend fun install(apk: Path) = error("Can't install an APK on desktop")
}
