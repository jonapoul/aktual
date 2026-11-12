package aktual.budget.payees.ui

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.Address
import aktual.budget.payees.domain.AddressResult
import aktual.budget.payees.domain.AddressResult.Failed
import aktual.budget.payees.domain.AddressResult.Found
import aktual.budget.payees.domain.AddressResult.Loading
import aktual.budget.payees.domain.AddressResult.NotFound
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.payees_deleted_undo
import aktual.core.l10n.payees_location_delete_failed
import aktual.core.l10n.payees_location_deleted
import aktual.core.l10n.payees_location_restore_failed
import aktual.core.model.Coordinates
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ShimmerLine
import aktual.core.ui.showErrorSnackbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
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

// Starts the lookup when shown, so only rows on screen are looked up
@Composable
internal fun AddressText(
  address: AddressResult?,
  coordinates: Coordinates,
  onRequest: (Coordinates) -> Unit,
  style: TextStyle,
  modifier: Modifier = Modifier,
) {
  // Repeat requests are ignored, so this can run on every recomposition while it loads
  if (address == Loading) SideEffect { onRequest(coordinates) }
  when (address) {
    Loading ->
      ShimmerLine(
        style = style,
        modifier = modifier.fillMaxWidth(ADDRESS_SHIMMER_FRACTION).shimmer(rememberShimmer(Window)),
      )
    is Found ->
      Text(
        modifier = modifier,
        text = address.address.text(),
        style = style,
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )
    NotFound,
    Failed,
    null -> Unit
  }
}

@Composable
internal fun OpenStreetMapAttribution(modifier: Modifier = Modifier) {
  val uriHandler = LocalUriHandler.current
  Text(
    modifier = modifier.clickable { uriHandler.openUri(OSM_COPYRIGHT_URL) },
    text = Strings.payeesLocationsAttribution,
    style = typography.bodySmall,
    color = colors.pageTextSubdued,
  )
}

private fun Address.text(): String = listOfNotNull(title, subtitle).joinToString(", ")

private fun Double.fiveDecimals(): String {
  val scaled = (this * FIVE_DECIMALS).roundToLong()
  val whole = abs(scaled) / FIVE_DECIMALS.toLong()
  val fraction = (abs(scaled) % FIVE_DECIMALS.toLong()).toString().padStart(length = 5, '0')
  return "${if (scaled < 0) "-" else ""}$whole.$fraction"
}

private const val MAP_ZOOM = 18
private const val ADDRESS_SHIMMER_FRACTION = 0.5f
private const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
private const val FIVE_DECIMALS = 100_000.0
