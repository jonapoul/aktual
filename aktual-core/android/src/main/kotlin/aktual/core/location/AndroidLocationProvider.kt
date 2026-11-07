package aktual.core.location

import aktual.core.model.Coordinates
import aktual.core.utils.LocationProvider
import aktual.di.AppScope
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dev.zacsweers.metro.ContributesBinding
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@ContributesBinding(AppScope::class)
class AndroidLocationProvider internal constructor(private val context: Context) :
  LocationProvider {
  private val manager: LocationManager? = context.getSystemService(LocationManager::class.java)

  override val isSupported: Boolean = manager != null

  override fun hasPermission(): Boolean =
    listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).any {
      ContextCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED
    }

  override suspend fun currentLocation(): Coordinates? {
    val manager = manager ?: return null
    if (!hasPermission()) return null
    val provider = providers().firstOrNull(manager::isProviderEnabled) ?: return null
    val location = withTimeoutOrNull(TIMEOUT) { manager.current(provider) } ?: return null
    return Coordinates.orNull(location.latitude, location.longitude)
  }

  // The fused provider mixes in wifi and cell towers, so it's quicker indoors than GPS alone
  private fun providers(): List<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
    add(LocationManager.NETWORK_PROVIDER)
    add(LocationManager.GPS_PROVIDER)
  }

  @SuppressLint("MissingPermission")
  private suspend fun LocationManager.current(provider: String): Location? =
    suspendCancellableCoroutine { continuation ->
      val signal = CancellationSignal()
      continuation.invokeOnCancellation { signal.cancel() }
      LocationManagerCompat.getCurrentLocation(
        this,
        provider,
        signal,
        ContextCompat.getMainExecutor(context),
      ) { location ->
        continuation.resume(location)
      }
    }

  private companion object {
    val TIMEOUT = 15.seconds
  }
}
