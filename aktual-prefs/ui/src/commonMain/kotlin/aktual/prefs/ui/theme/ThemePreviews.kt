package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.MoonStars
import aktual.core.icons.Sun
import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.prefs.vm.theme.ThemePreview
import aktual.prefs.vm.theme.ThemePreviewMode
import aktual.prefs.vm.theme.ThemeSwatch
import aktual.prefs.vm.theme.swatch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

// Mock screens drawn in the chosen themes' colours
@Composable
internal fun ThemePreviews(
  previews: ImmutableList<ThemePreview>,
  customThemeNames: ImmutableMap<ThemeId, String>,
  modifier: Modifier = Modifier,
) {
  // Keep showing the second preview while it fades out
  val second = previews.getOrNull(1)
  var lastSecond by remember { mutableStateOf(second) }
  SideEffect { if (second != null) lastSecond = second }

  // Centred, so the first preview slides across as the second one's space grows or shrinks
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    previews.firstOrNull()?.let { PreviewItem(it, customThemeNames) }

    AnimatedVisibility(
      visible = second != null,
      enter = fadeIn() + expandHorizontally(expandFrom = Start, clip = false),
      exit = fadeOut() + shrinkHorizontally(shrinkTowards = Start, clip = false),
    ) {
      (second ?: lastSecond)?.let { preview ->
        PreviewItem(preview, customThemeNames, Modifier.padding(start = PreviewSpacing))
      }
    }
  }
}

@Composable
private fun PreviewItem(
  preview: ThemePreview,
  customThemeNames: ImmutableMap<ThemeId, String>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(ThumbnailWidth),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Thumbnail(preview.swatch)
    Label(name = themeName(preview.id, customThemeNames), mode = preview.mode)
  }
}

@Composable
private fun Thumbnail(swatch: ThemeSwatch) {
  val background by animateColorAsState(swatch.background)
  val header by animateColorAsState(swatch.header)
  val surface by animateColorAsState(swatch.surface)
  val accent by animateColorAsState(swatch.accent)

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .height(ThumbnailHeight)
        .clip(ThumbnailShape)
        .border(1.dp, colors.settingsCardBorder, ThumbnailShape)
        .background(background),
  ) {
    Box(Modifier.fillMaxWidth().height(16.dp).background(header))
    Column(
      modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Bar(surface, Modifier.fillMaxWidth())
      Bar(surface, Modifier.fillMaxWidth(fraction = 0.7f))
      Spacer(Modifier.weight(1f))
      Bar(accent, Modifier.fillMaxWidth(fraction = 0.44f).align(End))
    }
  }
}

@Composable
private fun Bar(color: Color, modifier: Modifier = Modifier) =
  Box(modifier.height(12.dp).background(color, BarShape))

@Composable
private fun Label(name: String, mode: ThemePreviewMode) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(4.dp, CenterHorizontally),
    verticalAlignment = CenterVertically,
  ) {
    val icon =
      when (mode) {
        Always -> null
        Day -> AktualIcons.Sun
        Night -> AktualIcons.MoonStars
      }
    if (icon != null) {
      Icon(
        modifier = Modifier.size(14.dp),
        imageVector = icon,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
    }
    Text(
      text = name,
      style = typography.bodyMedium,
      color = colors.pageText,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }
}

private val ThumbnailWidth = 148.dp
private val PreviewSpacing = 12.dp
private val ThumbnailHeight = 92.dp
private val ThumbnailShape = RoundedCornerShape(6.dp)
private val BarShape = RoundedCornerShape(3.dp)

@Preview
@Composable
private fun PreviewThemePreviewsSingle(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ThemePreviews(
      previews =
        persistentListOf(
          ThemePreview(MidnightColors.id, Always, MidnightColors.swatch()),
        ),
      customThemeNames = persistentMapOf(),
    )
  }

@Preview
@Composable
private fun PreviewThemePreviewsSplit(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ThemePreviews(
      previews =
        persistentListOf(
          ThemePreview(LightColors.id, Day, LightColors.swatch()),
          ThemePreview(DarkColors.id, Night, DarkColors.swatch()),
        ),
      customThemeNames = persistentMapOf(),
    )
  }
