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
