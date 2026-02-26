package aktual.settings.ui.theme

import aktual.core.theme.Theme
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ThemeSettingsAction {
  data object NavBack : ThemeSettingsAction

  data object RetryFetchCatalog : ThemeSettingsAction

  data object ClearCache : ThemeSettingsAction

  @JvmInline value class Inspect(val id: Theme.Id) : ThemeSettingsAction

  @JvmInline value class Select(val id: Theme.Id) : ThemeSettingsAction
}
