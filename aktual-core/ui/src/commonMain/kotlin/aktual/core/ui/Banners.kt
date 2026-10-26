package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.AlertTriangle
import aktual.core.icons.CheveronRight
import aktual.core.icons.ExclamationOutline
import aktual.core.icons.InformationOutline
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
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
  icon: ImageVector? = AktualIcons.InformationOutline,
  onClick: (() -> Unit)? = null,
) =
  Banner(
    text = text,
    icon = icon,
    background = colors.noticeBackgroundLight,
    content = colors.noticeText,
    modifier = modifier,
    onClick = onClick,
  )

@Composable
fun WarningBanner(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = AktualIcons.AlertTriangle,
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
  icon: ImageVector? = AktualIcons.ExclamationOutline,
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
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) {}
        .clip(RowShape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .background(background)
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
    if (onClick != null) {
      Icon(
        modifier = Modifier.size(BannerIconSize),
        imageVector = AktualIcons.CheveronRight,
        contentDescription = null,
        tint = content,
      )
    }
  }
}

private val BannerIconSize = 24.dp

@Preview
@Composable
private fun PreviewBanners(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Large)) {
      NoticeBanner(text = "This screen is read-only")
      WarningBanner(text = "Some values in this rule aren't supported yet")
      ErrorBanner(text = "Server error 404: Resource not found")
      NoticeBanner(text = "No icon on this one", icon = null)
      NoticeBanner(text = "Tap me to learn more", onClick = {})
    }
  }
