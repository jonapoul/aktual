package aktual.app.nav

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class SettingsNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(SettingsNavRoute)
}

@Immutable @Serializable data object SettingsNavRoute : NavKey

@Immutable
class InspectThemeNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke(id: ThemeId) = stack.push(InspectThemeNavRoute(id))
}

@Immutable @Serializable data class InspectThemeNavRoute(val id: ThemeId) : NavKey

@Immutable
class ThemeSettingsNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(ThemeSettingsNavRoute)
}

@Immutable @Serializable data object ThemeSettingsNavRoute : NavKey
