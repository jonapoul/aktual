package aktual.api.client

import aktual.core.model.ServerUrl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import io.ktor.client.HttpClient
import okio.FileSystem

@AssistedInject
class ApiBuilderImpl(
  @param:AktualClient private val client: HttpClient,
  private val fileSystem: FileSystem,
  @Assisted private val serverUrl: ServerUrl,
) : ApiBuilder {
  override fun account(): AccountApi = AccountApiImpl(client, serverUrl)

  override fun base(): BaseApi = BaseApiImpl(client, serverUrl)

  override fun health(): HealthApi = HealthApiImpl(client, serverUrl)

  override fun metrics(): MetricsApi = MetricsApiImpl(client, serverUrl)

  override fun sync(): SyncApi = SyncApiImpl(client, fileSystem, serverUrl)

  @AssistedFactory
  @ContributesBinding(AppScope::class, binding<ApiBuilder.Factory>())
  interface Factory : ApiBuilder.Factory {
    override fun create(@Assisted serverUrl: ServerUrl): ApiBuilderImpl
  }
}
