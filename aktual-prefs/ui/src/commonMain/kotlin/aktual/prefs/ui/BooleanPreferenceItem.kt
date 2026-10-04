package aktual.prefs.ui

import aktual.prefs.ui.core.BooleanPreferenceItem
import aktual.prefs.vm.BooleanPreference
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun BooleanPreferenceItem(
  preference: BooleanPreference,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  includeBackground: Boolean = true,
  bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  if (!preference.visible) return
  BooleanPreferenceItem(
    value = preference.value,
    onValueChange = preference.onChange,
    enabled = preference.enabled,
    title = title,
    subtitle = subtitle,
    icon = icon,
    modifier = modifier,
    includeBackground = includeBackground,
    bottomContent = bottomContent,
  )
}
