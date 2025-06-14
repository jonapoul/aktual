package actual.about.ui.licenses

import actual.about.data.ArtifactDetail
import actual.about.res.Strings
import actual.core.res.CoreDimens
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.NormalIconButton
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import alakazam.android.ui.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
internal fun ArtifactItem(
  artifact: ArtifactDetail,
  onLaunchUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .shadow(CoreDimens.medium)
      .padding(CoreDimens.small)
      .background(theme.cardBackground, CardShape)
      .padding(CoreDimens.huge),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      modifier = Modifier.weight(1f),
    ) {
      Text(
        text = artifact.name ?: artifact.artifactId,
        fontWeight = FontWeight.W700,
        color = theme.pageTextPositive,
        fontSize = 15.sp,
      )

      LibraryTableRow(title = Strings.licensesItemGroup, value = artifact.groupId)
      LibraryTableRow(title = Strings.licensesItemArtifact, value = artifact.artifactId)
      LibraryTableRow(title = Strings.licensesItemVersion, value = artifact.version)
      LibraryTableRow(title = Strings.licensesItemLicense, value = artifact.license())
    }

    val url = artifact.scm?.url
    if (url != null) {
      NormalIconButton(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = Strings.licensesItemLaunch,
        onClick = { onLaunchUrl.invoke(url) },
      )
    }
  }
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

    HorizontalSpacer(CoreDimens.medium)

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
