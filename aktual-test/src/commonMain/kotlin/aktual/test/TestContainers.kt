package aktual.test

import alakazam.kotlin.CoroutineContexts
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope

@BindingContainer
class TestCoroutineContainer(
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
) {
  @Provides fun scope(): CoroutineScope = scope

  @Provides fun contexts(): CoroutineContexts = contexts
}

fun TestScope.coroutineContainer(
  contexts: CoroutineContexts = TestCoroutineContexts(standardDispatcher)
): TestCoroutineContainer = TestCoroutineContainer(scope = this, contexts)

@BindingContainer
class TestPreferencesContainer(private val preferences: Preferences) {
  @Provides fun preferences(): Preferences = preferences
}
