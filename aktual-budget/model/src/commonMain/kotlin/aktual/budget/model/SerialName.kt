package aktual.budget.model

import kotlinx.serialization.serializer

inline fun <reified E : Enum<E>> E.serialName(): String =
  serializer<E>().descriptor.getElementName(ordinal)
