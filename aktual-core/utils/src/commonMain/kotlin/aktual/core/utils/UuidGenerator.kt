package aktual.core.utils

fun interface UuidGenerator {
  operator fun invoke(): String

  operator fun <T> invoke(mapper: (String) -> T): T = mapper(invoke())
}
