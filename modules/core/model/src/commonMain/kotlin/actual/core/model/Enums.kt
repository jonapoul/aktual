package actual.core.model

import kotlin.reflect.KClass

@Suppress("UnusedReceiverParameter")
inline fun <reified E : Enum<E>> KClass<E>.parse(string: String): E = enumValues<E>()
  .firstOrNull { it.toString() == string }
  ?: error("No ${E::class.qualifiedName} matching '$string'")
