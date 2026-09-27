package aktual.test

import aktual.app.desktop.AktualDesktopViewModel
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking

class JvmViewModelSmokeTest : ViewModelSmokeTest<TestJvmAppGraph>() {
  override fun buildGraph(): TestJvmAppGraph =
    createDynamicGraph<TestJvmAppGraph>(
      TestAppDirectoryContainer(rootDir),
      TestBudgetFilesContainer(rootDir),
    )

  // Wait for in-flight DB queries to finish, otherwise they fail once the graph closes and the temp
  // dir is deleted, and
  // the exception gets reported against whichever test runs next
  override fun cancelViewModelScope(scope: CoroutineScope) = runBlocking {
    scope.coroutineContext.job.cancelAndJoin()
  }

  @Test fun root() = testVm<AktualDesktopViewModel>()
}
