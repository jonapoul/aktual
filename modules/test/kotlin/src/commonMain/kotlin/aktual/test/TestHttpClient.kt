/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import aktual.api.builder.buildKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

fun testHttpClient(
  engine: HttpClientEngine,
  json: Json,
): HttpClient = buildKtorClient(json, tag = null, engine, isDebug = false)
