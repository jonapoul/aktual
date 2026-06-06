package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object EditNavGridNavRoute : NavKey

@Immutable
class EditNavGridNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke() = stack.push(EditNavGridNavRoute)
}
