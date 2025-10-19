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
package aktual.app.nav

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudWarning
import aktual.core.model.PingState
import aktual.core.ui.BottomBarState
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BottomStatusBar(
  state: BottomBarState.Visible,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier
    .background(theme.cardBackground)
    .padding(vertical = 3.dp, horizontal = 8.dp)
    .fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
) {
  val (pingState, budgetName) = state

  if (budgetName != null) {
    Text(
      text = loadedString(budgetName),
      fontSize = 10.sp,
      color = theme.pageText,
    )
  }

  val text = pingState.text()
  val tint = pingState.tint(theme)

  HorizontalSpacer(weight = 1f)

  Image(
    modifier = Modifier.size(12.dp),
    imageVector = pingState.icon(),
    contentDescription = text,
    colorFilter = ColorFilter.tint(tint),
  )

  HorizontalSpacer(5.dp)

  Text(
    text = text,
    fontSize = 10.sp,
    color = tint,
  )
}

@Stable
private fun PingState.icon() = when (this) {
  PingState.Success -> AktualIcons.CloudCheck
  PingState.Failure -> AktualIcons.CloudWarning
  PingState.Unknown -> AktualIcons.CloudWarning
}

@Composable
private fun PingState.text() = when (this) {
  PingState.Success -> Strings.connectionConnected
  PingState.Failure -> Strings.connectionDisconnected
  PingState.Unknown -> Strings.connectionUnknown
}

@Stable
private fun PingState.tint(theme: Theme) = when (this) {
  PingState.Success -> theme.successText
  PingState.Failure -> theme.warningText
  PingState.Unknown -> theme.pageTextSubdued
}

@Stable
@Composable
private fun loadedString(budgetName: String): AnnotatedString = buildAnnotatedString {
  append(Strings.budgetLoaded)
  append("  ")
  withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
    append(budgetName)
  }
}
