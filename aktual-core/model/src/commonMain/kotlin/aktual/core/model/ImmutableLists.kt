package aktual.core.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun <T> immutableList(n: Int, init: (index: Int) -> T): ImmutableList<T> =
  List(n, init).toImmutableList()
