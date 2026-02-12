package aktual.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okio.FileSystem

@BindingContainer
@ContributesTo(AppScope::class)
object FileSystemContainer {
  @Provides @SingleIn(AppScope::class) fun provides(): FileSystem = FileSystem.Companion.SYSTEM
}
