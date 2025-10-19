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
package aktual.about.ui.licenses

import aktual.about.vm.LicensesViewModel
import aktual.core.ui.metroViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LicensesScreen(
  nav: LicensesNavigator,
  viewModel: LicensesViewModel = metroViewModel(),
) {
  val licensesState by viewModel.licensesState.collectAsStateWithLifecycle()
  val searchBarState by viewModel.searchBarState.collectAsStateWithLifecycle()

  LicensesScaffold(
    state = licensesState,
    searchBarState = searchBarState,
    onAction = { action ->
      when (action) {
        LicensesAction.NavBack -> nav.back()
        LicensesAction.Reload -> viewModel.load()
        LicensesAction.ToggleSearchBar -> viewModel.toggleSearchBar()
        is LicensesAction.EditSearchText -> viewModel.setSearchText(action.text)
        is LicensesAction.LaunchUrl -> viewModel.openUrl(action.url)
      }
    },
  )
}
