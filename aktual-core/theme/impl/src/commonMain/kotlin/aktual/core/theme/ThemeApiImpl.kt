package aktual.core.theme

import aktual.core.ui.Theme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.path

class ThemeApiImpl(private val client: HttpClient) : ThemeApi {
  override suspend fun fetchThemeList(): List<ThemeSummary> {
    val httpResponse =
      client.get {
        url {
          protocol = URLProtocol.HTTPS
          host = BASE_URL
          path(
            "/actualbudget/actual/master/packages/desktop-client/src/data/customThemeCatalog.json"
          )
        }
      }
    return httpResponse.body()
  }

  override suspend fun fetchTheme(repo: ThemeRepo): Theme {
    val httpResponse =
      client.get {
        url {
          protocol = URLProtocol.HTTPS
          host = BASE_URL
          path("/${repo.userName}/${repo.repoName}/refs/heads/main/actual.css")
        }
      }
    return parseTheme(css = httpResponse.body<String>())
  }

  private companion object {
    const val BASE_URL = "https://raw.githubusercontent.com/"
  }
}
