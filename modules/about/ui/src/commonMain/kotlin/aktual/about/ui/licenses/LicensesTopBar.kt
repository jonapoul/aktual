/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.licenses

import aktual.about.vm.SearchBarState
import aktual.core.ui.Theme
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun LicensesTopBar(
  state: SearchBarState,
  theme: Theme,
  onAction: (LicensesAction) -> Unit,
) = TopAppBar(
  colors = theme.transparentTopAppBarColors(),
  navigationIcon = {
    IconButton(onClick = { onAction(LicensesAction.NavBack) }) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = Strings.licensesToolbarBack,
      )
    }
  },
  title = {
    Text(
      text = Strings.licensesToolbarTitle,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  },
  actions = {
    IconButton(onClick = { onAction(LicensesAction.ToggleSearchBar) }) {
      Icon(
        imageVector = when (state) {
          SearchBarState.Gone -> Icons.Filled.Search
          is SearchBarState.Visible -> Icons.Filled.SearchOff
        },
        contentDescription = Strings.licensesToolbarSearch,
      )
    }
  },
)
