package aktual.test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object TestContextContainer {
  @Provides fun context(): Context = ApplicationProvider.getApplicationContext()
}
