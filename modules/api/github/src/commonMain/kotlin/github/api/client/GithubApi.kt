package github.api.client

import github.api.model.GithubReleases
import retrofit2.http.GET

/**
 * From https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 */
fun interface GithubApi {
  @GET("/repos/jonapoul/actual-android/releases")
  suspend fun getReleases(): GithubReleases
}
