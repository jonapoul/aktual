/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.utils.io.core.Closeable

data class AktualApis(
  val serverUrl: ServerUrl,
  val client: HttpClient,
  val account: AccountApi,
  val base: BaseApi,
  val health: HealthApi,
  val metrics: MetricsApi,
  val sync: SyncApi,
  val syncDownload: SyncDownloadApi,
) : Closeable by client
