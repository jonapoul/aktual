/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.di

import aktual.core.model.BuildConfig
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import kotlin.time.Instant

fun buildConfig(isDebug: Boolean) = BuildConfig(
  isDebug = isDebug,
  versionCode = VERSION_CODE,
  versionName = VERSION_NAME,
  gitHash = GIT_HASH,
  buildTime = Instant.fromEpochMilliseconds(BUILD_TIME_MS),
  defaultPassword = DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty,
  defaultServerUrl = DEFAULT_URL?.let(::ServerUrl),
)
