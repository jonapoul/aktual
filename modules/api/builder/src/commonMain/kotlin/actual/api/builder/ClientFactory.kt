package actual.api.builder

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

fun interface ClientFactory {
  fun build(json: Json): HttpClient
}
