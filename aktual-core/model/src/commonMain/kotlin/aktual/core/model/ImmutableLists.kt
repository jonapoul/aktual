package aktual.core.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

fun <T> immutableList(n: Int, init: (index: Int) -> T): ImmutableList<T> = List(n, init).toImmutableList()

fun <T> persistentList(n: Int, init: (index: Int) -> T): PersistentList<T> = List(n, init).toPersistentList()
