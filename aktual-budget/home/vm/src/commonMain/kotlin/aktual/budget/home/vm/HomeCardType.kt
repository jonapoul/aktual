package aktual.budget.home.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

enum class HomeCardType {
  ThisMonth,
  Attention,
  Upcoming,
  Recent,
  Accounts,
  Reports,
}

// Saved names that don't match a card are dropped, and cards missing from the saved order go at
// the end
internal fun homeCardOrder(saved: List<String>?): ImmutableList<HomeCardType> {
  val known = saved.orEmpty().mapNotNull { name -> HomeCardType.entries.find { it.name == name } }
  return (known + HomeCardType.entries).distinct().toImmutableList()
}

internal fun List<HomeCardType>.swapping(a: HomeCardType, b: HomeCardType): List<HomeCardType> =
  map { card ->
    @Suppress("ElseCaseInsteadOfExhaustiveWhen")
    when (card) {
      a -> b
      b -> a
      else -> card
    }
  }

// Puts the given cards into the slots they already fill, in their new order. Cards not given stay
// where they are
internal fun List<HomeCardType>.reordering(cards: List<HomeCardType>): List<HomeCardType> {
  val remaining = cards.iterator()
  return map { card -> if (card in cards) remaining.next() else card }
}
