/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.licenses

import aktual.about.vm.LicensesViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

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
