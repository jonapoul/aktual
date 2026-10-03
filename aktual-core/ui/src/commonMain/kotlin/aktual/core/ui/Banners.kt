package aktual.core.ui

import aktual.core.icons.material.Error
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun NoticeBanner(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = MaterialIcons.Info,
) =
  Banner(
    text = text,
    icon = icon,
    background = colors.noticeBackgroundLight,
    content = colors.noticeText,
    modifier = modifier,
  )

@Composable
fun WarningBanner(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = MaterialIcons.Warning,
) =
  Banner(
    text = text,
    icon = icon,
    background = colors.warningBackground,
    content = colors.warningTextDark,
    modifier = modifier,
  )

@Composable
fun ErrorBanner(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = MaterialIcons.Error,
) =
  Banner(
    text = text,
    icon = icon,
    background = colors.errorBackground,
    content = colors.errorTextDarker,
    modifier = modifier,
  )

@Composable
private fun Banner(
  text: String,
  icon: ImageVector?,
  background: Color,
  content: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) {}
        .background(background, RowShape)
        .padding(Dimens.VeryLarge),
    horizontalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
    verticalAlignment = CenterVertically,
  ) {
    if (icon != null) {
      Icon(
        modifier = Modifier.size(BannerIconSize),
        imageVector = icon,
        contentDescription = null,
        tint = content,
      )
    }
    Text(
      modifier = Modifier.weight(1f),
      text = text,
      style = typography.bodySmall,
      color = content,
    )
  }
}

private val BannerIconSize = 24.dp

@Preview
@Composable
private fun PreviewBanners(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Large)) {
      NoticeBanner(text = "This screen is read-only")
      WarningBanner(text = "Some values in this rule aren't supported yet")
      ErrorBanner(text = "Server error 404: Resource not found")
      NoticeBanner(text = "No icon on this one", icon = null)
    }
  }
