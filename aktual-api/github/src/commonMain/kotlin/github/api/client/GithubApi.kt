package github.api.client

import aktual.codegen.GET
import aktual.codegen.KtorApi
import aktual.codegen.Path
import aktual.codegen.Query
import aktual.core.model.ServerUrl
import github.api.model.GithubRelease
import io.ktor.client.HttpClient

/**
 * From https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 */
@KtorApi
interface GithubApi : AutoCloseable {
  @GET("/repos/{user}/{repo}/releases/latest")
  suspend fun getReleases(
    @Path("user") user: String,
    @Path("repo") repo: String,
    @Query("per_page") perPage: Int? = null,
    @Query("page") pageNumber: Int? = null,
  ): List<GithubRelease>

  fun interface Factory {
    fun build(): GithubApi
  }

  companion object {
    val BASE_URL = ServerUrl("https://api.github.com")
  }
}

expect fun GithubApi(serverUrl: ServerUrl = GithubApi.BASE_URL, client: HttpClient): GithubApi
