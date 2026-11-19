package aktual.updates.domain

import okio.Path

interface ApkInstaller {
  // False where APKs can't be installed, like desktop
  val isSupported: Boolean

  // Hands the APK to the system installer, which asks the user to confirm
  suspend fun install(apk: Path)
}
