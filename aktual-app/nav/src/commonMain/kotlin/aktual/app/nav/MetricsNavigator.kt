package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
class MetricsNavigator(private val stack: AktualNavStack<NavKey>) {
  operator fun invoke() = stack.push(MetricsNavRoute)
}

@Immutable @Serializable data object MetricsNavRoute : NavKey
