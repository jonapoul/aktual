/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ktlint:standard:indent")

package actual.app.android

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
@ViewModelKey(ActualActivityViewModel::class)
@ContributesIntoMap(ViewModelScope::class, binding<ViewModel>())
class ActualActivityViewModel(
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
