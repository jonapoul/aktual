package aktual.core.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// sessionEnded shows a dialog explaining the server ended the session, e.g. an expired token
@Serializable data class LoginNavRoute(val sessionEnded: Boolean = false) : NavKey

@Serializable data object ServerUrlNavRoute : NavKey

@Serializable data object ChangePasswordNavRoute : NavKey
