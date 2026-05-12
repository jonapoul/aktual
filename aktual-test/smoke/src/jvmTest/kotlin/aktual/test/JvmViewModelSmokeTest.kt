package aktual.test

import aktual.app.desktop.AktualDesktopViewModel
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test

class JvmViewModelSmokeTest : ViewModelSmokeTest<TestJvmAppGraph>() {
  override fun buildGraph(): TestJvmAppGraph =
    createDynamicGraph<TestJvmAppGraph>(
      TestAppDirectoryContainer(rootDir),
      TestBudgetFilesContainer(rootDir),
    )

  @Test fun root() = testVm<AktualDesktopViewModel>()
}
