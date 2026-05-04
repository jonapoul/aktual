package aktual.core.model

import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.uuid.Uuid

fun interface UuidGenerator {
  operator fun invoke(): String

  operator fun <T> invoke(mapper: (String) -> T): T = mapper(invoke())
}

@ContributesBinding(AppScope::class)
class RandomUuidGenerator : UuidGenerator {
  override fun invoke(): String = Uuid.random().toString()
}
