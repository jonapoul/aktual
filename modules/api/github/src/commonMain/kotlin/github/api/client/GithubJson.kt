/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package github.api.client

import kotlinx.serialization.json.Json

val GithubJson = Json {
  ignoreUnknownKeys = true
}
