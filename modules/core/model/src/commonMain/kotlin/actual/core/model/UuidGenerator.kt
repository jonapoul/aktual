package actual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

fun interface UuidGenerator {
  operator fun invoke(): Uuid
}

@Inject
@ContributesBinding(AppScope::class)
class RandomUuidGenerator : UuidGenerator {
  override fun invoke(): Uuid = Uuid.random()
}
