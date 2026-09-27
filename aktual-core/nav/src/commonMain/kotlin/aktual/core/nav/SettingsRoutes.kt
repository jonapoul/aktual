package aktual.core.nav

import aktual.core.model.ThemeId
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SettingsNavRoute : NavKey

@Serializable data object ThemeSettingsNavRoute : NavKey

@Serializable data object CustomThemeSettingsNavRoute : NavKey

@Serializable data class InspectThemeNavRoute(val id: ThemeId) : NavKey
