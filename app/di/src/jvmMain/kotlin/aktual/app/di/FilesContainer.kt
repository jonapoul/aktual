/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.di

import aktual.core.model.Files
import aktual.core.model.SystemFiles
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object FilesContainer {
  @Provides
  fun prefs(): Files = SystemFiles
}
