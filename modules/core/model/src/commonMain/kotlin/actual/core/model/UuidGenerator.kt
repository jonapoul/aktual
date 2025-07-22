package actual.core.model

import javax.inject.Inject
import kotlin.uuid.Uuid

fun interface UuidGenerator {
  operator fun invoke(): Uuid
}

class RandomUuidGenerator @Inject constructor() : UuidGenerator {
  override fun invoke(): Uuid = Uuid.random()
}
