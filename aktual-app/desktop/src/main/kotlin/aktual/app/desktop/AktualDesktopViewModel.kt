@file:Suppress("LongParameterList")

package aktual.app.desktop

import aktual.api.client.TokenExpiredEvent
import aktual.app.nav.RootViewModel
import aktual.budget.model.BudgetFiles
import aktual.core.connection.ConnectionMonitor
import aktual.core.connection.ServerPinger
import aktual.core.connection.ServerVersionFetcher
import aktual.core.di.BudgetGraphHolder
import aktual.core.model.PingStateHolder
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.theme.ThemeResolver
import alakazam.kotlin.CoroutineContexts
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CoroutineScope

@Inject
@ViewModelKey(AktualDesktopViewModel::class)
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class AktualDesktopViewModel(
  appScope: CoroutineScope,
  contexts: CoroutineContexts,
  connectionMonitor: ConnectionMonitor,
  serverPinger: ServerPinger,
  pingStateHolder: PingStateHolder,
  serverVersionFetcher: ServerVersionFetcher,
  files: BudgetFiles,
  budgetComponents: BudgetGraphHolder,
  preferences: AppGlobalPreferences,
  themeResolver: ThemeResolver,
  tokenExpiredEvent: TokenExpiredEvent,
) :
  RootViewModel(
    appScope = appScope,
    contexts = contexts,
    connectionMonitor = connectionMonitor,
    serverPinger = serverPinger,
    pingStateHolder = pingStateHolder,
    serverVersionFetcher = serverVersionFetcher,
    files = files,
    budgetComponents = budgetComponents,
    preferences = preferences,
    themeResolver = themeResolver,
    tokenExpiredEvent = tokenExpiredEvent,
  )
