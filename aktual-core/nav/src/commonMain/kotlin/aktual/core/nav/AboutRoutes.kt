package aktual.core.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object InfoNavRoute : NavKey

@Serializable data object LicensesNavRoute : NavKey

@Serializable data object SearchLicensesNavRoute : NavKey

@Serializable data object ManageStorageNavRoute : NavKey
