package aktual.test

import aktual.app.desktop.AktualDesktopViewModel
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test

class JvmViewModelSmokeTest : ViewModelSmokeTest<TestJvmAppGraph>() {
  override fun buildGraph(container: TestContainer): TestJvmAppGraph =
    createDynamicGraph<TestJvmAppGraph>(container)

  @Test fun root() = testVm<AktualDesktopViewModel>()

  override fun optionallySkip() = Unit
}
