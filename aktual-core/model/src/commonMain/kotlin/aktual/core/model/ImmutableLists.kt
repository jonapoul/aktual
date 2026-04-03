package aktual.core.model

import kotlin.collections.groupBy as stdlibGroupBy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList

fun <T> immutableList(n: Int, init: (index: Int) -> T): ImmutableList<T> =
  List(n, init).toImmutableList()

fun <T> persistentList(n: Int, init: (index: Int) -> T): PersistentList<T> =
  List(n, init).toPersistentList()

fun <T, K> ImmutableList<T>.groupBy(keySelector: (T) -> K): ImmutableMap<K, ImmutableList<T>> =
  stdlibGroupBy(keySelector)
    .map { (key, values) -> key to values.toImmutableList() }
    .toMap()
    .toImmutableMap()
