package aktual.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

@BindingContainer
@ContributesTo(AppScope::class)
object RandomContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun random(): Random = SecureRandom.getInstanceStrong().asKotlinRandom()
}
