package aktual.updates.domain

import okio.FileSystem
import okio.Path

internal class FakeApkInstaller(
  private val fileSystem: FileSystem = SYSTEM,
  override val isSupported: Boolean = true,
) : ApkInstaller {
  val installed = mutableListOf<String>()

  override suspend fun install(apk: Path) {
    installed += fileSystem.read(apk) { readUtf8() }
  }
}
