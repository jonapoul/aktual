/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.core.model.ServerUrl
import aktual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Stable
@Composable
fun UsingServerText(
  url: ServerUrl?,
  onClickChange: () -> Unit,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 16.sp,
  theme: Theme = LocalTheme.current,
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
) {
  Column(
    modifier = modifier
      .hazeEffect(hazeState, hazeStyle)
      .background(Color.Transparent, RounderCardShape)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = Strings.loginUsingServer,
      fontSize = fontSize,
      color = theme.pageText,
      textAlign = TextAlign.Center,
    )

    Text(
      text = url?.toString().orEmpty(),
      fontSize = fontSize,
      color = theme.pageText,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    )

    BareTextButton(
      text = Strings.loginServerChange,
      onClick = onClickChange,
    )
  }
}
