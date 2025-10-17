@file:Suppress("ktlint:standard:indent")

package actual.app.desktop

import actual.app.nav.RootViewModel
import actual.budget.model.BudgetFiles
import actual.core.connection.ConnectionMonitor
import actual.core.connection.ServerPinger
import actual.core.connection.ServerVersionFetcher
import actual.core.di.BudgetGraphHolder
import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import actual.core.model.PingStateHolder
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope

@Inject
@ViewModelKey(ActualDesktopViewModel::class)
@ContributesIntoMap(ViewModelScope::class, binding<ViewModel>())
class ActualDesktopViewModel(
  appScope: CoroutineScope,
  contexts: CoroutineContexts,
  connectionMonitor: ConnectionMonitor,
  serverPinger: ServerPinger,
  pingStateHolder: PingStateHolder,
  serverVersionFetcher: ServerVersionFetcher,
  files: BudgetFiles,
  budgetComponents: BudgetGraphHolder,
  preferences: AppGlobalPreferences,
) : RootViewModel(
  appScope = appScope,
  contexts = contexts,
  connectionMonitor = connectionMonitor,
  serverPinger = serverPinger,
  pingStateHolder = pingStateHolder,
  serverVersionFetcher = serverVersionFetcher,
  files = files,
  budgetComponents = budgetComponents,
  preferences = preferences,
)
