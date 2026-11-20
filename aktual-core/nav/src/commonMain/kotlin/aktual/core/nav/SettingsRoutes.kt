package aktual.core.nav

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SettingsNavRoute : NavKey

@Serializable data object SearchSettingsNavRoute : NavKey

@Serializable data class ThemeSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data object CustomThemeSettingsNavRoute : NavKey

@Serializable data class InspectThemeNavRoute(val id: ThemeId) : NavKey

@Serializable data class SearchThemeNavRoute(val id: ThemeId) : NavKey

@Serializable data class SystemUiSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class SecuritySettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class FormatSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class AppIconSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class AmountSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class BarSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class DialogSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data class UpdateSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

// A setting to scroll to and flash on opening a settings page. group is the title of the card
// holding it, or null for a card or setting at the top level of the page
@Immutable @Serializable data class SettingsHighlight(val group: String?, val title: String)

@Serializable data class DateSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable
data class TransactionSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey

@Serializable data object AdvancedSettingsNavRoute : NavKey

@Serializable
data class ExperimentalSettingsNavRoute(val highlight: SettingsHighlight? = null) : NavKey
