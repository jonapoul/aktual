/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.core.model.AktualVersions
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VersionsText(
  versions: AktualVersions,
  modifier: Modifier = Modifier,
  padding: Dp = 15.dp,
) = Text(
  modifier = modifier.padding(padding),
  text = versions.toString(),
  style = AktualTypography.labelMedium,
)
