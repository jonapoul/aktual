package aktual.updates.domain

import aktual.test.TestBuildConfig
import aktual.test.emptyMockEngine
import aktual.test.enqueueResponse
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.ktor.client.engine.mock.MockEngine
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

class NightlyInstallerImplTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var root: Path
  private lateinit var apkInstaller: FakeApkInstaller

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    root = createTempDirectory().toOkioPath()
    apkInstaller = FakeApkInstaller()
  }

  @AfterTest
  fun after() {
    mockEngine.close()
    FileSystem.SYSTEM.deleteRecursively(root)
  }

  @Test
  fun `Downloads the run's artifact and installs the APK inside`() = runTest {
    mockEngine.enqueueResponse(
      zip("aktual-android-mapping.txt" to "mapping", "aktual.2026.11.19.release.apk" to "apk"),
    )
    val progress = mutableListOf<Float?>()

    buildInstaller().install(BUILD) { progress += it }

    assertThat(mockEngine.requestHistory.single().url.toString())
      .isEqualTo("https://nightly.link/jonapoul/aktual/actions/runs/123/android.zip")
    assertThat(apkInstaller.installed).containsExactly("apk")
    assertThat(progress.last()).isEqualTo(1f)
    assertThat(FileSystem.SYSTEM.exists(root / "updates")).isFalse()
  }

  @Test
  fun `Fails without an APK in the artifact`() = runTest {
    mockEngine.enqueueResponse(zip("aktual-android-mapping.txt" to "mapping"))

    assertFailure { buildInstaller().install(BUILD) {} }
    assertThat(apkInstaller.installed).containsExactly()
    assertThat(FileSystem.SYSTEM.exists(root / "updates")).isFalse()
  }

  private fun TestScope.buildInstaller() =
    NightlyInstallerImpl(
      client = testHttpClient(mockEngine),
      installer = apkInstaller,
      buildConfig = TestBuildConfig,
      appDirectory = { root },
      fileSystem = SYSTEM,
      contexts = TestCoroutineContexts(unconfinedDispatcher),
    )

  private fun zip(vararg files: Pair<String, String>): ByteArray {
    val bytes = ByteArrayOutputStream()
    ZipOutputStream(bytes).use { zip ->
      files.forEach { (name, content) ->
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray())
        zip.closeEntry()
      }
    }
    return bytes.toByteArray()
  }

  private companion object {
    val BUILD =
      NightlyBuild(
        runId = 123,
        gitHash = "dbc10893",
        commitTime = Instant.parse("2026-11-18T21:23:52Z"),
        commitsAhead = 3,
      )
  }
}
