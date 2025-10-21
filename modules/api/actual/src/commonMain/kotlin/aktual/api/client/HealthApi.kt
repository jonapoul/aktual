/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.api.model.health.GetHealthResponse
import aktual.codegen.GET
import aktual.codegen.KtorApi
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface HealthApi {
  @GET("/health")
  suspend fun getHealth(): GetHealthResponse
}

expect fun HealthApi(serverUrl: ServerUrl, client: HttpClient): HealthApi
