package actual.api.builder

import actual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

fun interface ClientFactory {
  operator fun invoke(json: Json): HttpClient
}

@Inject
@ContributesBinding(AppScope::class)
class ClientFactoryImpl(private val buildConfig: BuildConfig) : ClientFactory {
  override fun invoke(json: Json) = buildKtorClient(json, isDebug = buildConfig.isDebug, tag = "ACTUAL")
}
