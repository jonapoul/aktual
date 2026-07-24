package aktual.di

import aktual.core.UuidGenerator
import dev.zacsweers.metro.ContributesBinding
import kotlin.uuid.Uuid

@ContributesBinding(AppScope::class)
class RandomUuidGenerator : UuidGenerator {
  override fun invoke(): String = Uuid.random().toString()
}
