package aktual.test

import aktual.app.android.AktualActivityViewModel
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidViewModelSmokeTest : ViewModelSmokeTest<TestAndroidAppGraph>() {
  override fun buildGraph(container: TestContainer): TestAndroidAppGraph =
    createDynamicGraph<TestAndroidAppGraph>(container)

  @Test fun root() = testVm<AktualActivityViewModel>()
}
