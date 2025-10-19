/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
