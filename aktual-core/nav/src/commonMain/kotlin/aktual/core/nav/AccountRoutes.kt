package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object LoginNavRoute : NavKey

@Immutable @Serializable data object ServerUrlNavRoute : NavKey

@Immutable @Serializable data object ChangePasswordNavRoute : NavKey
