package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ArtifactItem(
  artifact: ArtifactDetail,
  onLaunchUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val interactionSource = remember { MutableInteractionSource() }
  Row(
    modifier =
      modifier
        .padding(horizontal = Dimens.Large, vertical = Dimens.Small)
        .background(theme.buttonNormalBackground, CardShape)
        .border(Dp.Hairline, theme.pillBorderDark, CardShape)
        .clickableIfNeeded(artifact, onLaunchUrl, interactionSource)
        .padding(Dimens.Large),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
      Text(
        text = artifact.name ?: artifact.artifactId,
        fontWeight = FontWeight.W700,
        color = theme.pageTextPositive,
        fontSize = 15.sp,
      )

      val headerStyle =
        LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = TextSize)

      val headerWidth =
        persistentListOf(
            Strings.licensesItemArtifact,
            Strings.licensesItemVersion,
            Strings.licensesItemLicense,
          )
          .maxOf { estimateTextWidth(it) }

      LibraryTableRow(
        title = Strings.licensesItemArtifact,
        value = artifact.fullArtifact,
        headerStyle = headerStyle,
        headerWidth = headerWidth,
      )
      LibraryTableRow(
        title = Strings.licensesItemVersion,
        value = artifact.version,
        headerStyle = headerStyle,
        headerWidth = headerWidth,
      )
      LibraryTableRow(
        title = Strings.licensesItemLicense,
        value = artifact.license(),
        headerStyle = headerStyle,
        headerWidth = headerWidth,
      )
    }

    val url = artifact.scm?.url
    if (url != null) {
      NormalIconButton(
        modifier = Modifier.clip(CardShape),
        imageVector = MaterialIcons.ArrowRight,
        onClick = { onLaunchUrl(url) },
        contentDescription = Strings.licensesItemLaunch,
      )
    }
  }
}

@Stable
@Composable
private fun estimateTextWidth(
  text: String,
  density: Density = LocalDensity.current,
  resolver: FontFamily.Resolver = LocalFontFamilyResolver.current,
  style: TextStyle = LocalTextStyle.current,
): Dp {
  val textStyle = remember(style) { style.copy(fontSize = TextSize, fontWeight = FontWeight.Bold) }
  val widthInPx =
    remember(text, textStyle, density, resolver) {
      ParagraphIntrinsics(
          text = text,
          style = textStyle,
          annotations = emptyList(),
          density = density,
          fontFamilyResolver = resolver,
        )
        .maxIntrinsicWidth
    }

  return with(density) { widthInPx.toDp() }
}

@get:Stable
private val ArtifactDetail.fullArtifact
  get() = "$groupId:$artifactId"

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

@Composable
private fun LibraryTableRow(
  title: String,
  value: String?,
  headerStyle: TextStyle,
  headerWidth: Dp,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (value.isNullOrEmpty()) {
    // Nothing to show
    return
  }

  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
    Text(
      modifier = Modifier.width(headerWidth),
      text = title,
      textAlign = TextAlign.Start,
      color = theme.pageTextLight,
      lineHeight = LineHeight,
      style = headerStyle,
    )

    HorizontalSpacer(Dimens.Large)

    Text(
      modifier = Modifier.weight(1f),
      text = value,
      textAlign = TextAlign.Start,
      color = theme.pageText,
      lineHeight = LineHeight,
      fontSize = TextSize,
    )
  }
}

private val LineHeight = 15.sp
private val TextSize = 12.sp

@Preview
@Composable
private fun PreviewArtifactItem(
  @PreviewParameter(ArtifactItemProvider::class) params: ThemedParams<ArtifactDetail>
) = PreviewWithTheme(params.theme) { ArtifactItem(artifact = params.data, onLaunchUrl = {}) }

private class ArtifactItemProvider :
  ThemedParameterProvider<ArtifactDetail>(
    AlakazamAndroidCore,
    ComposeMaterialRipple,
    FragmentKtx,
    Slf4jApi,
  )
