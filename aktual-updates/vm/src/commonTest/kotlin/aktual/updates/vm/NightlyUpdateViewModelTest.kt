package aktual.updates.vm

import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import aktual.updates.domain.NightlyBuild
import aktual.updates.domain.NightlyInstaller
import aktual.updates.domain.NightlyUpdates
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class NightlyUpdateViewModelTest {
  private val latest = MutableStateFlow<NightlyBuild?>(null)
  private val installer = FakeNightlyInstaller()

  @Test
  fun `Shows both versions once a nightly is found`() = runTest {
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(None)

      latest.update { BUILD }
      assertThatNextEmissionIsEqualTo(available(Idle))
    }
  }

  @Test
  fun `Reports download progress, then goes back to idle`() = runTest {
    latest.update { BUILD }
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(None)
      assertThatNextEmissionIsEqualTo(available(Idle))

      viewModel.install()
      assertThatNextEmissionIsEqualTo(available(InstallState.Downloading(progress = null)))

      runCurrent()
      installer.progress(0.5f)
      assertThatNextEmissionIsEqualTo(available(InstallState.Downloading(progress = 0.5f)))

      installer.finish.complete(Result.success(Unit))
      assertThatNextEmissionIsEqualTo(available(Idle))
    }
  }

  @Test
  fun `Shows a failed install`() = runTest {
    latest.update { BUILD }
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(None)
      assertThatNextEmissionIsEqualTo(available(Idle))

      viewModel.install()
      assertThatNextEmissionIsEqualTo(available(InstallState.Downloading(progress = null)))

      installer.finish.complete(Result.failure(IllegalStateException("No APK")))
      assertThatNextEmissionIsEqualTo(available(Failed))
    }
  }

  private fun TestScope.buildViewModel() =
    NightlyUpdateViewModel(
      updates = FakeNightlyUpdates(latest),
      installer = installer,
      buildConfig = TestBuildConfig,
      viewModelContext = viewModelContext,
    )

  private fun available(install: InstallState) =
    NightlyUpdateState.Available(
      installed = Version(name = "1.2.3", gitHash = "abcd1234"),
      nightly = Version(name = "2026.11.18", gitHash = "dbc10893"),
      commitsAhead = 3,
      install = install,
    )

  private class FakeNightlyUpdates(override val latest: StateFlow<NightlyBuild?>) : NightlyUpdates {
    override val isSupported = true
  }

  private class FakeNightlyInstaller : NightlyInstaller {
    val finish = CompletableDeferred<Result<Unit>>()
    private var onProgress: ((Float?) -> Unit)? = null

    fun progress(value: Float) = requireNotNull(onProgress).invoke(value)

    override suspend fun install(build: NightlyBuild, onProgress: (Float?) -> Unit) {
      this.onProgress = onProgress
      finish.await().getOrThrow()
    }
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
