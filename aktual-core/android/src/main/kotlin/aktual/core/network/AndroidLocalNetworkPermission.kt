package aktual.core.network

import aktual.core.model.ServerUrl
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.host
import aktual.core.utils.isLocalNetworkAddress
import aktual.core.utils.isLocalNetworkHost
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppScope
import aktual.log
import alakazam.kotlin.CoroutineContexts
import android.Manifest.permission.ACCESS_LOCAL_NETWORK
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.ContributesBinding
import java.net.InetAddress
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
class AndroidLocalNetworkPermission
internal constructor(private val context: Context, private val contexts: CoroutineContexts) :
  LocalNetworkPermission {
  override fun isGranted(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN ||
      ContextCompat.checkSelfPermission(context, ACCESS_LOCAL_NETWORK) == PERMISSION_GRANTED

  override suspend fun isRequiredFor(url: ServerUrl): Boolean {
    val host = url.host ?: return false
    return isRequiredForHost(host)
  }

  override suspend fun isRequiredForHost(host: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) return false
    if (isLocalNetworkHost(host)) return true

    // A DNS server on the local network doesn't need the permission, so this still works
    val addresses =
      runCatchingCancellable(
        attempt = { withContext(contexts.io) { InetAddress.getAllByName(host).toList() } },
        onFailure = { e ->
          log.w(e) { "Failed resolving $host" }
          emptyList()
        },
      )
    return addresses.any { it.isLocalNetworkAddress() }
  }
}
