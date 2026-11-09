package aktual.core.network

import aktual.di.AppScope
import aktual.di.Initializable
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import logcat.logcat

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding<@ForScope(AppScope::class) Initializable>())
class AndroidNetworkLogger internal constructor(private val context: Context) : Initializable {
  override fun initialize() {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    if (manager == null) {
      logcat.w { "No ConnectivityManager" }
      return
    }

    logcat.d { "Initial default network: ${manager.activeNetwork}" }
    manager.registerDefaultNetworkCallback(
      object : NetworkCallback() {
        override fun onAvailable(network: Network) {
          logcat.d { "onAvailable $network" }
        }

        override fun onLost(network: Network) {
          logcat.d { "onLost $network" }
        }

        override fun onUnavailable() {
          logcat.d { "onUnavailable" }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
          logcat.d { "onBlockedStatusChanged $network blocked=$blocked" }
        }

        override fun onLinkPropertiesChanged(network: Network, props: LinkProperties) {
          logcat.d {
            "onLinkPropertiesChanged $network dns=${props.dnsServers} " +
              "privateDns=${props.isPrivateDnsActive}/${props.privateDnsServerName} " +
              "domains=${props.domains}"
          }
        }
      },
    )
  }
}
