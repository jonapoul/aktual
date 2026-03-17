package aktual.core.theme

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol.Companion.HTTPS
import io.ktor.http.path
import kotlinx.serialization.builtins.ListSerializer

@Inject
@ContributesBinding(AppScope::class)
class ThemeApiImpl(@param:ThemeClient private val client: HttpClient) : ThemeApi {
  override suspend fun fetchCatalog(): List<CustomThemeSummary> {
    val httpResponse = client.get {
      url {
        protocol = HTTPS
        host = BASE_URL
        path(CATALOG_PATH)
      }
    }

    // Responses come with "Content-Type: text/plain", so we need to manually parse as JSON - can't
    // use automatic content negotiation as set up in buildKtorClient
    val serializer = ListSerializer(CustomThemeSummary.serializer())
    return ThemeJson.decodeFromString(serializer, httpResponse.bodyAsText())
  }

  override suspend fun fetchTheme(summary: CustomThemeSummary): CustomTheme {
    val httpResponse = client.get {
      url {
        protocol = HTTPS
        host = BASE_URL
        path("/${summary.repo.userName}/${summary.repo.repoName}/refs/heads/main/actual.css")
      }
    }
    val css = httpResponse.bodyAsText()
    val theme = parseTheme(summary, css)
    return theme
  }

  private companion object {
    const val BASE_URL = "raw.githubusercontent.com"
    const val CATALOG_PATH =
      "/actualbudget/actual/master/packages/desktop-client/src/data/customThemeCatalog.json"
  }
}
