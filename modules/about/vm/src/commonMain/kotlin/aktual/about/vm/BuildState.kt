/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.vm

import aktual.core.model.AktualVersions
import androidx.compose.runtime.Immutable

@Immutable
data class BuildState(
  val versions: AktualVersions,
  val buildDate: String,
  val year: Int,
)
