package aktual.core.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object LoginNavRoute : NavKey

@Serializable data object ServerUrlNavRoute : NavKey

@Serializable data object ChangePasswordNavRoute : NavKey
