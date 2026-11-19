package aktual.updates.domain

import aktual.api.client.GithubClient
import aktual.core.model.BuildConfig
import aktual.core.utils.AppDirectory
import aktual.di.AppScope
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

@ContributesBinding(AppScope::class)
class NightlyInstallerImpl
internal constructor(
  @param:GithubClient private val client: HttpClient,
  private val installer: ApkInstaller,
  private val buildConfig: BuildConfig,
  private val appDirectory: AppDirectory,
  private val fileSystem: FileSystem,
  private val contexts: CoroutineContexts,
) : NightlyInstaller {
  override suspend fun install(build: NightlyBuild, onProgress: (Float?) -> Unit) {
    val dir = appDirectory.get() / UPDATES_DIR
    withContext(contexts.io) {
      try {
        fileSystem.deleteRecursively(dir)
        fileSystem.createDirectories(dir)
        val zip = dir / "android.zip"
        download(build.downloadUrl(), zip, onProgress)
        val apk = dir / "aktual.apk"
        extractApk(fileSystem, zip, apk)
        installer.install(apk)
      } finally {
        fileSystem.deleteRecursively(dir)
      }
    }
  }

  private suspend fun download(url: String, target: Path, onProgress: (Float?) -> Unit) {
    client.prepareGet(url).execute { response ->
      val total = response.contentLength()?.takeIf { it > 0 }
      val channel = response.bodyAsChannel()
      val buffer = ByteArray(BUFFER_SIZE)
      var downloaded = 0L
      fileSystem.sink(target).buffer().use { sink ->
        while (true) {
          val read = channel.readAvailable(buffer)
          if (read < 0) break
          sink.write(buffer, 0, read)
          downloaded += read
          onProgress(total?.let { downloaded.toFloat() / it })
        }
      }
    }
  }

  // nightly.link serves workflow artifacts without needing a GitHub token
  private fun NightlyBuild.downloadUrl() =
    "https://nightly.link/${buildConfig.repoName}/actions/runs/$runId/android.zip"

  private companion object {
    const val UPDATES_DIR = "updates"
    const val BUFFER_SIZE = 64 * 1024
  }
}

// Copies the first APK in the zip to apk, failing if there isn't one
internal expect fun extractApk(fileSystem: FileSystem, zip: Path, apk: Path)
