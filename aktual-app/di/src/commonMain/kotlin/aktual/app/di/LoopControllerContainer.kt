/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.di

import alakazam.kotlin.core.InfiniteLoopController
import alakazam.kotlin.core.LoopController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
interface LoopControllerContainer {
  @Binds val InfiniteLoopController.loopController: LoopController
}
