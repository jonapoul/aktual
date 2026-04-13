package aktual.core.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun <T> immutableList(size: Int, init: (index: Int) -> T): ImmutableList<T> =
  List(size, init).toImmutableList()
