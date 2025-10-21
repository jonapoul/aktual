/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import alakazam.kotlin.core.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.update

data class AktualVersions(
  val app: String,
  val server: String?,
) {
  override fun toString() = "App: ${app.optionalPrefixed()} | Server: ${server.optionalPrefixed()}"

  private fun String?.optionalPrefixed(): String = when {
    this == null -> "Unknown"
    this.startsWith(prefix = "v") -> this
    else -> "v$this"
  }

  companion object {
    val Dummy = AktualVersions(app = "1.2.3", server = "24.3.0")
  }
}

@Inject
@SingleIn(AppScope::class)
class AktualVersionsStateHolder(
  private val buildConfig: BuildConfig,
) : StateHolder<AktualVersions>(from(buildConfig, server = null)) {
  fun set(serverVersion: String?) = update { from(buildConfig, serverVersion) }

  private companion object {
    fun from(build: BuildConfig, server: String?) = AktualVersions(app = build.versionName, server = server)
  }
}
