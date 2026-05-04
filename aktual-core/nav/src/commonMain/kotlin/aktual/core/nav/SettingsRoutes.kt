package aktual.core.nav

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object SettingsNavRoute : NavKey

@Immutable @Serializable data object ThemeSettingsNavRoute : NavKey

@Immutable @Serializable data object CustomThemeSettingsNavRoute : NavKey

@Immutable @Serializable data class InspectThemeNavRoute(val id: ThemeId) : NavKey
