/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import alakazam.kotlin.core.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

enum class PingState {
  Unknown,
  Failure,
  Success,
}

@Inject
@SingleIn(AppScope::class)
class PingStateHolder : StateHolder<PingState>(initialState = PingState.Unknown)
