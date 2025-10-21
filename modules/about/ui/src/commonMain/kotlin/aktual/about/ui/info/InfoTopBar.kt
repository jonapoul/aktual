/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.info

import aktual.core.ui.Theme
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun InfoTopBar(
  theme: Theme,
  onAction: (InfoAction) -> Unit,
) {
  TopAppBar(
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = {
      IconButton(onClick = { onAction(InfoAction.NavBack) }) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = Strings.infoToolbarBack,
        )
      }
    },
    title = {
      Text(
        text = Strings.infoToolbarTitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
  )
}
