package aktual.test

import aktual.app.android.AktualActivityViewModel
import aktual.app.android.AktualApplication
import aktual.app.android.AndroidAppGraph
import aktual.core.di.AppGraph
import aktual.core.di.BudgetGraph
import aktual.prefs.EncryptedPreferences
import alakazam.kotlin.core.CoroutineContexts
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createDynamicGraphFactory
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(
  application = AktualApplication::class,
  minSdk = 28, // needed by MetroAppComponentFactory
  maxSdk = 28, // keep this for now, makes the tests take much less time
)
class AndroidViewModelSmokeTest : ViewModelSmokeTest<AndroidAppGraph>() {
  override fun buildGraph() =
    createDynamicGraphFactory<AndroidAppGraph.Factory>(TestBindings)
      .create(context = ApplicationProvider.getApplicationContext())

  override fun AndroidAppGraph.buildBudgetGraph(): BudgetGraph {

  }

  @Test
  fun root() = testVm<AktualActivityViewModel>()
}

@BindingContainer
internal object TestBindings {
  @Provides
  fun encryptedPrefs(
    prefs: SharedPreferences,
    contexts: CoroutineContexts,
  ): EncryptedPreferences = object : EncryptedPreferences,
    Preferences by AndroidSharedPreferences(prefs, contexts.io) {}
}
