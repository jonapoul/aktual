package aktual.budget.payees.ui

import aktual.budget.model.PayeeLocationId
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.payees_deleted_undo
import aktual.core.l10n.payees_location_delete_failed
import aktual.core.l10n.payees_location_deleted
import aktual.core.l10n.payees_location_restore_failed
import aktual.core.model.Coordinates
import aktual.core.ui.showErrorSnackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlin.math.abs
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.getString

internal fun Coordinates.mapUrl(): String =
  "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=$MAP_ZOOM/$latitude/$longitude"

@Composable
internal fun Coordinates.text(): String =
  Strings.payeesEditLocationCoordinates(latitude.fiveDecimals(), longitude.fiveDecimals())

internal suspend fun SnackbarHostState.showLocationDeleted(
  id: PayeeLocationId,
  onUndo: (PayeeLocationId) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.payees_location_deleted),
      actionLabel = getString(Res.string.payees_deleted_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) {
    onUndo(id)
  }
}

internal suspend fun SnackbarHostState.showLocationDeleteFailed() =
  showErrorSnackbar(getString(Res.string.payees_location_delete_failed))

internal suspend fun SnackbarHostState.showLocationRestoreFailed() =
  showErrorSnackbar(getString(Res.string.payees_location_restore_failed))

private fun Double.fiveDecimals(): String {
  val scaled = (this * FIVE_DECIMALS).roundToLong()
  val whole = abs(scaled) / FIVE_DECIMALS.toLong()
  val fraction = (abs(scaled) % FIVE_DECIMALS.toLong()).toString().padStart(length = 5, '0')
  return "${if (scaled < 0) "-" else ""}$whole.$fraction"
}

private const val MAP_ZOOM = 18
private const val FIVE_DECIMALS = 100_000.0
