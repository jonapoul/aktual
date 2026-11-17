package aktual.core.network

import aktual.core.model.ServerUrl
import aktual.core.utils.LocalNetworkPermission
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class JvmLocalNetworkPermission : LocalNetworkPermission {
  override fun isGranted(): Boolean = true

  override suspend fun isRequiredFor(url: ServerUrl): Boolean = false

  override suspend fun isRequiredForHost(host: String): Boolean = false
}
