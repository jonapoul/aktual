package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class ShoveSortOrdersTest {
  @Test
  fun `No target goes one increment after the last item`() {
    val shoved = shoveSortOrders(listOf("a" to 100.0, "b" to 200.0), target = null)
    assertThat(shoved).isEqualTo(ShovedSortOrders(200.0 + SORT_INCREMENT, emptyMap()))
  }

  @Test
  fun `No items starts at one increment`() {
    val shoved = shoveSortOrders(emptyList(), target = "a")
    assertThat(shoved).isEqualTo(ShovedSortOrders(SORT_INCREMENT.toDouble(), emptyMap()))
  }

  @Test
  fun `A target that isn't listed goes at the end`() {
    val shoved = shoveSortOrders(listOf("a" to 100.0), target = "missing")
    assertThat(shoved).isEqualTo(ShovedSortOrders(100.0 + SORT_INCREMENT, emptyMap()))
  }

  @Test
  fun `Before a target goes halfway between it and the item above`() {
    val shoved = shoveSortOrders(listOf("a" to 100.0, "b" to 200.0), target = "b")
    assertThat(shoved).isEqualTo(ShovedSortOrders(150.0, emptyMap()))
  }

  @Test
  fun `Before the first item goes halfway to zero`() {
    val shoved = shoveSortOrders(listOf("a" to 100.0, "b" to 200.0), target = "a")
    assertThat(shoved).isEqualTo(ShovedSortOrders(50.0, emptyMap()))
  }

  @Test
  fun `Items from the target on are pushed up when there's no room`() {
    val items = listOf("a" to 100.0, "b" to 101.0, "c" to 102.0, "d" to 50_000.0)
    val shoved = shoveSortOrders(items, target = "b")
    val updates = mapOf("b" to 101.0 + SORT_INCREMENT, "c" to 101.0 + SORT_INCREMENT * 2)
    assertThat(shoved).isEqualTo(ShovedSortOrders(100.5, updates))
  }
}
