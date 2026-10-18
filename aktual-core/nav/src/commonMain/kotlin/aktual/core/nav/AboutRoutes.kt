package aktual.core.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable data object InfoNavRoute : NavKey

@Serializable data object LicensesNavRoute : NavKey

@Serializable data object SearchLicensesNavRoute : NavKey

@Serializable data object ManageStorageNavRoute : NavKey

@Serializable data object LogsNavRoute : NavKey

@Serializable data class LogDayNavRoute(val date: LocalDate) : NavKey
