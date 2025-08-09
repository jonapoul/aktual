package actual.app.di

import actual.core.model.RandomUuidGenerator
import actual.core.model.UuidGenerator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
interface UuidGeneratorContainer {
  @Binds val RandomUuidGenerator.binds: UuidGenerator
}
