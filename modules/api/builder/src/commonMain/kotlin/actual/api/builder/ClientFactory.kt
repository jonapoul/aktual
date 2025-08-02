package actual.api.builder

import alakazam.kotlin.core.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

fun interface ClientFactory {
  fun build(json: Json): HttpClient
}

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<ClientFactory>())
class ClientFactoryImpl(private val buildConfig: BuildConfig) : ClientFactory {
  override fun build(json: Json) = buildKtorClient(json, isDebug = buildConfig.debug, tag = "ACTUAL")
}
