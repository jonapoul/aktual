package aktual.prefs.vm.appicon

import aktual.prefs.AppIcon
import aktual.prefs.AppIconController
import aktual.test.unconfinedViewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class AppIconSettingsViewModelTest {
  @Test
  fun `Selecting an icon passes it to the controller`() = runTest {
    val controller = FakeAppIconController()
    val viewModel = AppIconSettingsViewModel(controller, unconfinedViewModelContext)

    viewModel.selected.test {
      assertThat(awaitItem()).isEqualTo(Default)

      viewModel.select(Dark)
      assertThat(awaitItem()).isEqualTo(Dark)
      assertThat(controller.requested).containsExactly(AppIcon.Dark)
    }
  }
}

private class FakeAppIconController : AppIconController {
  val requested = mutableListOf<AppIcon>()
  private val mutableCurrent = MutableStateFlow(AppIcon.Default)

  override val current: StateFlow<AppIcon> = mutableCurrent

  override suspend fun set(icon: AppIcon) {
    requested += icon
    mutableCurrent.update { icon }
  }
}
