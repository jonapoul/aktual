package aktual.core.nav

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
class SettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(SettingsNavRoute)
}

@Immutable
class InspectThemeNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(id: ThemeId) = stack.push(InspectThemeNavRoute(id))
}

@Immutable
class ThemeSettingsNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(ThemeSettingsNavRoute)
}

@Immutable
class CustomThemesNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(CustomThemeSettingsNavRoute)
}
