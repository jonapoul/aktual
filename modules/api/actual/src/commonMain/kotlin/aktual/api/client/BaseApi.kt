/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.api.model.base.InfoResponse
import aktual.codegen.GET
import aktual.codegen.KtorApi
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface BaseApi {
  @GET("/info")
  suspend fun fetchInfo(): InfoResponse
}

expect fun BaseApi(serverUrl: ServerUrl, client: HttpClient): BaseApi
