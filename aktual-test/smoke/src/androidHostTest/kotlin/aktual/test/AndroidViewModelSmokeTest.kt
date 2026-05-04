package aktual.test

import aktual.app.android.AktualActivityViewModel
import aktual.app.android.AktualApplication
import android.os.Build
import dev.zacsweers.metro.createDynamicGraph
import kotlin.test.Test
import org.junit.Assume.assumeFalse
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Single SDK version, otherwise we get UnsatisfiedLinkError
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = AktualApplication::class)
class AndroidViewModelSmokeTest : ViewModelSmokeTest<TestAndroidAppGraph>() {
  override fun buildGraph(container: TestContainer): TestAndroidAppGraph =
    createDynamicGraph<TestAndroidAppGraph>(container)

  @Test fun root() = testVm<AktualActivityViewModel>()

  override fun optionallySkip() {
    assumeFalse(
      "Robolectric has issues with SDK 35, specifically for the root VM",
      Build.VERSION.SDK_INT == 35,
    )
  }
}
