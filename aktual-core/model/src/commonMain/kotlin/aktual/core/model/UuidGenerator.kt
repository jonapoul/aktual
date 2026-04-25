package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.uuid.Uuid

fun interface UuidGenerator {
  operator fun invoke(): String
}

@ContributesBinding(AppScope::class)
class RandomUuidGenerator : UuidGenerator {
  override fun invoke(): String = Uuid.random().toString()
}
