/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress("ktlint:standard:indent")

package aktual.app.desktop

import aktual.app.nav.RootViewModel
import aktual.budget.model.BudgetFiles
import aktual.core.connection.ConnectionMonitor
import aktual.core.connection.ServerPinger
import aktual.core.connection.ServerVersionFetcher
import aktual.core.di.BudgetGraphHolder
import aktual.core.di.ViewModelKey
import aktual.core.di.ViewModelScope
import aktual.core.model.PingStateHolder
import aktual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope

@Inject
@ViewModelKey(AktualDesktopViewModel::class)
@ContributesIntoMap(ViewModelScope::class, binding<ViewModel>())
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
