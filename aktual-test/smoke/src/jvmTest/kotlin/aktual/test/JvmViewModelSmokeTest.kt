package aktual.test

import aktual.app.desktop.AktualDesktopViewModel
import aktual.app.desktop.JvmAppGraph
import aktual.core.di.AppGraph
import dev.zacsweers.metro.createGraph
import kotlin.test.Test

class JvmViewModelSmokeTest : ViewModelSmokeTest() {
  override fun buildGraph(): AppGraph = createGraph<JvmAppGraph>()

  @Test
  fun root() = testVm<AktualDesktopViewModel>()
}
