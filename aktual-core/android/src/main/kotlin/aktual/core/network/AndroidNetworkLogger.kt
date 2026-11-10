package aktual.core.network

import aktual.di.AppScope
import aktual.di.Initializable
import aktual.log
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

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding<@ForScope(AppScope::class) Initializable>())
class AndroidNetworkLogger internal constructor(private val context: Context) : Initializable {
  override fun initialize() {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    if (manager == null) {
      log.w { "No ConnectivityManager" }
      return
    }

    log.d { "Initial default network: ${manager.activeNetwork}" }
    manager.registerDefaultNetworkCallback(
      object : NetworkCallback() {
        override fun onAvailable(network: Network) {
          log.d { "onAvailable $network" }
        }

        override fun onLost(network: Network) {
          log.d { "onLost $network" }
        }

        override fun onUnavailable() {
          log.d { "onUnavailable" }
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
          log.d { "onBlockedStatusChanged $network blocked=$blocked" }
        }

        override fun onLinkPropertiesChanged(network: Network, props: LinkProperties) {
          log.d {
            "onLinkPropertiesChanged $network dns=${props.dnsServers} " +
              "privateDns=${props.isPrivateDnsActive}/${props.privateDnsServerName} " +
              "domains=${props.domains}"
          }
        }
      },
    )
  }
}
