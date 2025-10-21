/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.api.model.metrics.GetMetricsResponse
import aktual.codegen.GET
import aktual.codegen.KtorApi
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface MetricsApi {
  @GET("/metrics")
  suspend fun getMetrics(): GetMetricsResponse
}

expect fun MetricsApi(serverUrl: ServerUrl, client: HttpClient): MetricsApi
