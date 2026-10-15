package aktual.budget.home.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

enum class HomeCardType {
  ThisMonth,
  Attention,
  Upcoming,
  Accounts,
}

// Saved names that don't match a card are dropped, and cards missing from the saved order go at
// the end
internal fun homeCardOrder(saved: List<String>?): ImmutableList<HomeCardType> {
  val known = saved.orEmpty().mapNotNull { name -> HomeCardType.entries.find { it.name == name } }
  return (known + HomeCardType.entries).distinct().toImmutableList()
}

internal fun List<HomeCardType>.swapping(a: HomeCardType, b: HomeCardType): List<HomeCardType> =
  map { card ->
    when (card) {
      a -> b
      b -> a
      else -> card
    }
  }
