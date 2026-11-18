package aktual.budget.transactions.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TransactionTableColumnsTest {
  @Test
  fun `Nothing saved gives the defaults`() {
    assertThat(parseTransactionTableColumns(null)).isEqualTo(DEFAULTS)
    assertThat(parseTransactionTableColumns("")).isEqualTo(DEFAULTS)
  }

  @Test
  fun `Malformed JSON gives the defaults`() {
    assertThat(parseTransactionTableColumns("{not json")).isEqualTo(DEFAULTS)
    assertThat(parseTransactionTableColumns("""{"id":"date"}""")).isEqualTo(DEFAULTS)
  }

  @Test
  fun `Unknown and repeated columns are dropped`() {
    val raw =
      """[{"id":"payee","hidden":true},{"id":"mystery"},{"id":"payee","hidden":false},"date"]"""

    val columns = parseTransactionTableColumns(raw)

    // The first payee wins, and the rest fill in around it
    assertThat(columns)
      .isEqualTo(DEFAULTS.map { if (it.id == "payee") it.copy(hidden = true) else it })
  }

  @Test
  fun `Missing columns go back at their default position`() {
    val raw = """[{"id":"cleared","hidden":false},{"id":"date","hidden":false}]"""

    val ids = parseTransactionTableColumns(raw).map { it.id }

    assertThat(ids)
      .isEqualTo(
        listOf(
          "account",
          "payee",
          "notes",
          "group",
          "category",
          "payment",
          "deposit",
          "balance",
          "cleared",
          "date",
        ),
      )
  }

  @Test
  fun `The date can't be hidden`() {
    val columns = parseTransactionTableColumns("""[{"id":"date","hidden":true}]""")
    assertThat(columns.first { it.id == "date" }.hidden).isEqualTo(false)
  }

  @Test
  fun `Hiding both amounts shows both again`() {
    val raw = """[{"id":"payment","hidden":true},{"id":"deposit","hidden":true}]"""

    val columns = parseTransactionTableColumns(raw)

    assertThat(columns.filter { it.id == "payment" || it.id == "deposit" }.map { it.hidden })
      .isEqualTo(listOf(false, false))
  }

  @Test
  fun `Encoding round trips`() {
    val columns = DEFAULTS.map { if (it.id == "notes") it.copy(hidden = true) else it }.reversed()
    assertThat(parseTransactionTableColumns(columns.encode())).isEqualTo(columns)
  }

  private companion object {
    val DEFAULTS =
      listOf(
        TransactionTableColumn("date", hidden = false),
        TransactionTableColumn("account", hidden = false),
        TransactionTableColumn("payee", hidden = false),
        TransactionTableColumn("notes", hidden = false),
        TransactionTableColumn("group", hidden = true),
        TransactionTableColumn("category", hidden = false),
        TransactionTableColumn("payment", hidden = false),
        TransactionTableColumn("deposit", hidden = false),
        TransactionTableColumn("balance", hidden = true),
        TransactionTableColumn("cleared", hidden = false),
      )
  }
}
