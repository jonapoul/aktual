package aktual.settings.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PreferenceGroup(
  title: String,
  subtitle: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  theme: Theme = LocalTheme.current,
  content: @Composable ColumnScope.() -> Unit,
) {
  BasicPreferenceItem(
    title = title,
    subtitle = subtitle,
    icon = null,
    onClick = null,
    modifier = modifier,
    enabled = enabled,
    includeBackground = true,
    theme = theme,
    rightContent = null,
    headerStyle = AktualTypography.titleMedium,
    bottomContent = content,
  )
}
