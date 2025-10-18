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
package actual.about.ui.licenses

import actual.about.data.ArtifactDetail
import actual.core.ui.CardShape
import actual.core.ui.Dimens
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.defaultHazeStyle
import actual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun ArtifactItem(
  artifact: ArtifactDetail,
  onLaunchUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
) {
  val interactionSource = remember { MutableInteractionSource() }
  Column(
    modifier = modifier
      .shadow(Dimens.Medium)
      .padding(Dimens.Small)
      .background(Color.Transparent, CardShape)
      .hazeEffect(hazeState, hazeStyle)
      .clickableIfNeeded(artifact, onLaunchUrl, interactionSource)
      .padding(Dimens.Large),
    verticalArrangement = Arrangement.Top,
  ) {
    Text(
      text = artifact.name ?: artifact.artifactId,
      fontWeight = FontWeight.W700,
      color = theme.pageTextPositive,
      fontSize = 15.sp,
    )

    LibraryTableRow(title = Strings.licensesItemArtifact, value = artifact.fullArtifact)
    LibraryTableRow(title = Strings.licensesItemVersion, value = artifact.version)
    LibraryTableRow(title = Strings.licensesItemLicense, value = artifact.license())
  }
}

@get:Stable
private val ArtifactDetail.fullArtifact get() = "$groupId:$artifactId"

private fun Modifier.clickableIfNeeded(
  artifact: ArtifactDetail,
  onLaunchUrl: (String) -> Unit,
  interactionSource: MutableInteractionSource,
): Modifier {
  val url = artifact.scm?.url ?: return this
  return clickable(
    interactionSource = interactionSource,
    indication = ripple(),
    enabled = true,
    onClick = { onLaunchUrl(url) },
  )
}

@Stable
private fun ArtifactDetail.license(): String {
  val known = spdxLicenses.firstOrNull()
  if (known != null) return known.name
  val unknown = unknownLicenses.firstOrNull()
  if (unknown != null) return unknown.name ?: "Unknown"
  error("No licenses for $this?")
}

@Stable
@Composable
private fun LibraryTableRow(
  title: String,
  value: String?,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (value.isNullOrEmpty()) {
    // Nothing to show
    return
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      modifier = Modifier
        .weight(TITLE_WEIGHT)
        .wrapContentHeight(),
      text = title,
      textAlign = TextAlign.Start,
      color = theme.pageTextSubdued,
      lineHeight = LineHeight,
      fontSize = TextSize,
      fontWeight = FontWeight.Bold,
    )

    HorizontalSpacer(Dimens.Medium)

    Text(
      modifier = Modifier
        .weight(VALUE_WEIGHT)
        .wrapContentHeight(),
      text = value,
      textAlign = TextAlign.Start,
      color = theme.pageText,
      lineHeight = LineHeight,
      fontSize = TextSize,
    )
  }
}

private const val TITLE_WEIGHT = 1f
private const val VALUE_WEIGHT = 3f

private val LineHeight = 15.sp
private val TextSize = 12.sp
