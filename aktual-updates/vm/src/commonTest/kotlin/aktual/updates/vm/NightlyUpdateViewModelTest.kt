package aktual.updates.vm

import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import aktual.updates.domain.NightlyBuild
import aktual.updates.domain.NightlyUpdates
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class NightlyUpdateViewModelTest {
  @Test
  fun `Shows both versions once a nightly is found`() = runTest {
    val latest = MutableStateFlow<NightlyBuild?>(null)
    val viewModel =
      NightlyUpdateViewModel(FakeNightlyUpdates(latest), TestBuildConfig, viewModelContext)

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(None)

      latest.update { BUILD }
      assertThatNextEmissionIsEqualTo(
        NightlyUpdateState.Available(
          installed = Version(name = "1.2.3", gitHash = "abcd1234"),
          nightly = Version(name = "2026.11.18", gitHash = "dbc10893"),
          commitsAhead = 3,
          apkUrl = APK_URL,
        ),
      )

      latest.update { null }
      assertThatNextEmissionIsEqualTo(None)
    }
  }

  private class FakeNightlyUpdates(override val latest: StateFlow<NightlyBuild?>) : NightlyUpdates {
    override val isSupported = true
  }

  private companion object {
    const val APK_URL = "https://example.com/aktual-nightly.apk"

    val BUILD =
      NightlyBuild(
        gitHash = "dbc10893",
        commitTime = Instant.parse("2026-11-18T21:23:52Z"),
        commitsAhead = 3,
        apkUrl = APK_URL,
      )
  }
}
