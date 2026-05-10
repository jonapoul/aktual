package aktual.test

import aktual.budget.model.BudgetFiles
import alakazam.kotlin.CoroutineContexts
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import okio.FileSystem
import okio.Path

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
class TestPreferencesContainer(private val preferences: DataStore<Preferences>) {
  @Provides fun preferences(): DataStore<Preferences> = preferences
}

@BindingContainer
class TestContainer(private val directoryPath: Path) {
  @Provides
  fun budgetFiles(fileSystem: FileSystem): BudgetFiles = BudgetFiles(fileSystem, directoryPath)
}
