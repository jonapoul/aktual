package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable @Serializable data object InfoNavRoute : NavKey

@Immutable @Serializable data object LicensesNavRoute : NavKey

@Immutable @Serializable data object ManageStorageNavRoute : NavKey
