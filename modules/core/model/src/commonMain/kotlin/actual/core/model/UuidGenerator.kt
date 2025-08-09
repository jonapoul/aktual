package actual.core.model

import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

fun interface UuidGenerator {
  operator fun invoke(): Uuid
}

@Inject
class RandomUuidGenerator : UuidGenerator {
  override fun invoke(): Uuid = Uuid.random()
}
