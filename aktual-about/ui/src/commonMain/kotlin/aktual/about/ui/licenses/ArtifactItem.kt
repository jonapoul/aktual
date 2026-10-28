package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.lineHeights
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.rememberHighlighted
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.persistentListOf

/** Keep in sync with [ShimmerArtifactItem] */
@Composable
internal fun ArtifactItem(
  artifact: ArtifactDetail,
  onLaunchUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  val interactionSource = remember { MutableInteractionSource() }
  Row(
    modifier =
      modifier
        .padding(horizontal = Dimens.Large, vertical = Dimens.Small)
        .background(colors.buttonNormalBackground, CardShape)
        .border(Hairline, colors.pillBorderDark, CardShape)
        .clickableIfNeeded(artifact, onLaunchUrl, interactionSource)
        .padding(Dimens.Large),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
      Text(
        text = artifact.name ?: artifact.artifactId,
        fontWeight = W700,
        color = colors.pageTextPositive,
        fontSize = textSizes.mediumPlus,
      )

      val headerStyle = LocalTextStyle.current.copy(fontWeight = Bold, fontSize = TextSize)

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
        query = query,
      )
      LibraryTableRow(
        title = Strings.licensesItemVersion,
        value = artifact.version,
        headerStyle = headerStyle,
        headerWidth = headerWidth,
        query = query,
      )
      LibraryTableRow(
        title = Strings.licensesItemLicense,
        value = artifact.license(),
        headerStyle = headerStyle,
        headerWidth = headerWidth,
        query = query,
      )
    }

    val url = artifact.scm?.url
    if (url != null) {
      NormalIconButton(
        modifier = Modifier.clip(CardShape),
        imageVector = AktualIcons.CheveronRight,
        onClick = { onLaunchUrl(url) },
        contentDescription = Strings.licensesItemLaunch,
      )
    }
  }
}

/** Keep in sync with [ArtifactItem] */
@Composable
internal fun ShimmerArtifactItem(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.pageText, CardShape)

  Row(
    modifier =
      modifier
        .padding(horizontal = Dimens.Large, vertical = Dimens.Small)
        .background(colors.buttonNormalBackground, CardShape)
        .border(Hairline, colors.pillBorderDark, CardShape)
        .padding(Dimens.Large)
        .shimmer(rememberShimmer(Window)),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(ShimmerRowSpacing),
    ) {
      Box(bar.fillMaxWidth(fraction = 0.5f).height(ShimmerTitleHeight))
      repeat(times = 3) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Large)) {
          Box(bar.size(width = ShimmerHeaderWidth, height = ShimmerTextHeight))
          Box(bar.weight(1f).height(ShimmerTextHeight))
        }
      }
    }

    Box(bar.size(ShimmerButtonSize))
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
  val textStyle = remember(style) { style.copy(fontSize = TextSize, fontWeight = Bold) }
  val widthInPx =
    remember(text, textStyle, density, resolver) {
      ParagraphIntrinsics(
          text = text,
          style = textStyle,
          annotations = emptyList(),
          density = density,
          fontFamilyResolver = resolver,
          placeholders = emptyList(),
          softWrap = true,
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
  query: String,
  modifier: Modifier = Modifier,
) {
  if (value.isNullOrEmpty()) {
    // Nothing to show
    return
  }

  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
    Text(
      modifier = Modifier.width(headerWidth),
      text = title,
      textAlign = Start,
      color = colors.pageTextLight,
      lineHeight = LineHeight,
      style = headerStyle,
    )

    HorizontalSpacer(Dimens.Large)

    Text(
      modifier = Modifier.weight(1f),
      text = rememberHighlighted(value, query),
      textAlign = Start,
      color = colors.pageText,
      lineHeight = LineHeight,
      fontSize = TextSize,
    )
  }
}

private val LineHeight = lineHeights.mediumPlus
private val TextSize = textSizes.small

private val ShimmerTitleHeight = 18.dp
private val ShimmerTextHeight = 12.dp
private val ShimmerRowSpacing = 3.dp
private val ShimmerHeaderWidth = 50.dp
private val ShimmerButtonSize = 40.dp

@Preview
@Composable
private fun PreviewArtifactItem(
  @PreviewParameter(ArtifactItemProvider::class) params: ColorParams<ArtifactDetail>,
) = PreviewWithColorParams(params) { ArtifactItem(artifact = this, onLaunchUrl = {}) }

private class ArtifactItemProvider :
  ColorParamsProvider<ArtifactDetail>(
    AlakazamAndroidCore,
    ComposeMaterialRipple,
    FragmentKtx,
    Slf4jApi,
  )

@Preview
@Composable
private fun PreviewShimmerArtifactItem(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerArtifactItem() }
