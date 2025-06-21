package actual.about.ui.licenses

import actual.about.data.ArtifactDetail
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.defaultHazeStyle
import actual.l10n.Dimens
import actual.l10n.Strings
import alakazam.android.ui.compose.HorizontalSpacer
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
import androidx.compose.ui.tooling.preview.Preview
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
      .shadow(Dimens.medium)
      .padding(Dimens.small)
      .background(Color.Transparent, CardShape)
      .hazeEffect(hazeState, hazeStyle)
      .clickableIfNeeded(artifact, onLaunchUrl, interactionSource)
      .padding(Dimens.huge),
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

    HorizontalSpacer(Dimens.medium)

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

@Preview
@Composable
private fun PreviewItem() = PreviewColumn {
  ArtifactItem(
    artifact = AlakazamAndroidCore,
    onLaunchUrl = {},
  )
}
