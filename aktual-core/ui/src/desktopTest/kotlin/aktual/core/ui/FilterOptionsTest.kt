package aktual.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf

class FilterOptionsTest {
  private val options = persistentListOf("gbp", "usd", "eur")
  private val labels = listOf("Pound Sterling", "US Dollar", "Euro")
  private val terms = mapOf("gbp" to listOf("£"), "usd" to listOf("$"), "eur" to listOf("€"))

  private fun filter(query: String) = filterOptions(options, labels, query) { terms.getValue(it) }

  @Test
  fun `Empty query returns all options`() {
    assertEquals(options, filter(""))
  }

  @Test
  fun `Matches label ignoring case`() {
    assertEquals(listOf("usd"), filter("dollar"))
  }

  @Test
  fun `Matches search terms`() {
    assertEquals(listOf("eur"), filter("€"))
  }

  @Test
  fun `Matches partial label across options`() {
    assertEquals(listOf("gbp", "usd", "eur"), filter("u"))
  }

  @Test
  fun `No match returns empty`() {
    assertEquals(emptyList(), filter("yen"))
  }
}
