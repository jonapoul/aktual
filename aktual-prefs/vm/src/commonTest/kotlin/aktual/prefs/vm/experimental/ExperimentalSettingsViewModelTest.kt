package aktual.prefs.vm.experimental

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.test.FakeBudgetFeatureFlags
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class ExperimentalSettingsViewModelTest {
  @Test
  fun `Unsupported flags show their synced value but are disabled`() = runTest {
    val flags = FakeBudgetFeatureFlags()
    flags[ActionTemplating].set(true)
    val viewModel = viewModel(flags)

    viewModel.state.test {
      val feature = awaitItem()[ActionTemplating]
      assertThat(feature.isSupported).isFalse()
      assertThat(feature.preference.enabled).isFalse()
      assertThat(feature.preference.value).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Toggling a supported flag syncs it`() = runTest {
    val flags = FakeBudgetFeatureFlags()
    val viewModel = viewModel(flags)

    viewModel.state.test {
      val feature = awaitItem()[SankeyReport]
      assertThat(feature.preference.enabled).isTrue()
      feature.preference.onChange(true)
      assertThat(awaitItem()[SankeyReport].preference.value).isTrue()
      assertThat(SankeyReport in flags).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Goal templates UI only shows while goal templates are on`() = runTest {
    val flags = FakeBudgetFeatureFlags()
    val viewModel = viewModel(flags)

    viewModel.state.test {
      assertThat(awaitItem()[GoalTemplatesUi].preference.visible).isFalse()
      flags[GoalTemplates].set(true)
      assertThat(awaitItem()[GoalTemplatesUi].preference.visible).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Features keep upstream's order`() = runTest {
    viewModel(FakeBudgetFeatureFlags()).state.test {
      assertThat(awaitItem().features.map { it.flag }).isEqualTo(FeatureFlag.entries)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private operator fun ExperimentalSettingsState.get(flag: FeatureFlag) = features.first {
    it.flag == flag
  }

  private fun TestScope.viewModel(flags: FakeBudgetFeatureFlags) =
    ExperimentalSettingsViewModel(flags = flags, viewModelContext = viewModelContext)
}
