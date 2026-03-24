package aktual.test

import aktual.app.android.AktualActivityViewModel
import aktual.app.android.AktualApplication
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
  application = AktualApplication::class,
  minSdk = 28, // needed by MetroAppComponentFactory
  maxSdk = 35, // keep this for now, makes the tests take much less time
)
class AndroidViewModelSmokeTest : ViewModelSmokeTest<TestAndroidAppGraph>() {
  override fun buildGraph(container: TestContainer): TestAndroidAppGraph =
    createDynamicGraph<TestAndroidAppGraph>(container)

  @Test fun root() = testVm<AktualActivityViewModel>()
}
