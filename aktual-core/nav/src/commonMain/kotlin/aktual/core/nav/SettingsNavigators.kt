package aktual.core.nav

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
class SettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(SettingsNavRoute)
}

@Immutable
class SearchSettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(SearchSettingsNavRoute)
}

@Immutable
class InspectThemeNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(id: ThemeId) = stack.push(InspectThemeNavRoute(id))
}

@Immutable
class SearchThemeNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(id: ThemeId) = stack.push(SearchThemeNavRoute(id))
}

@Immutable
class ThemeSettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(highlight: SettingsHighlight? = null) =
    stack.push(ThemeSettingsNavRoute(highlight))
}

@Immutable
class CustomThemesNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(CustomThemeSettingsNavRoute)
}

@Immutable
class SystemUiSettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(highlight: SettingsHighlight? = null) =
    stack.push(SystemUiSettingsNavRoute(highlight))
}

@Immutable
class SecuritySettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(highlight: SettingsHighlight? = null) =
    stack.push(SecuritySettingsNavRoute(highlight))
}

@Immutable
class FormatSettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(highlight: SettingsHighlight? = null) =
    stack.push(FormatSettingsNavRoute(highlight))
}
