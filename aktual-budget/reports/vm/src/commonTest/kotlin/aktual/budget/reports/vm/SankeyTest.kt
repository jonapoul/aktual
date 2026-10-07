package aktual.budget.reports.vm

import aktual.budget.model.Amount
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.each
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.single
import kotlin.test.Test
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth

class SankeyTest {
  @Test
  fun `Income flows from payee through category and account into expense categories`() {
    val data =
      calculate(
        income(total = 1000, payeeId = "employer", payeeName = "Employer"),
        expense(total = -300, categoryId = "food", category = "Food"),
      )

    assertThat(data.nodes.map { it.key to it.column })
      .containsExactlyInAnyOrder(
        "employer" to 0,
        "salary" to 1,
        "checking" to 2,
        "bills" to 3,
        "food" to 4,
      )
    assertThat(data.flows())
      .containsExactlyInAnyOrder(
        Flow("employer", "salary", 1000),
        Flow("salary", "checking", 1000),
        Flow("checking", "bills", 300),
        Flow("bills", "food", 300),
      )
    assertThat(data.node("checking").value).isEqualTo(Amount(1000))
  }

  @Test
  fun `Refunded expense category flows into the account`() {
    val data = calculate(expense(total = 200, categoryId = "food", category = "Food"))

    assertThat(data.flows()).containsExactly(Flow("food__NEGATIVE", "checking", 200))
    assertThat(data.node("food__NEGATIVE").label).isEqualTo(SankeyLabel.Text("Food"))
  }

  @Test
  fun `Negative income flows out of the account and is coloured as negative`() {
    val data = calculate(income(total = -100))

    assertThat(data.flows()).containsExactly(Flow("checking", "salary__NEGATIVE", 100))
    assertThat(data.node("salary__NEGATIVE").color).isEqualTo(Negative)
    assertThat(data.links.single().color).isEqualTo(Negative)
  }

  @Test
  fun `Grouped accounts merge into one income node`() {
    val data =
      calculate(
        income(total = 1000, accountId = "a1"),
        income(total = 500, accountId = "a2"),
        params = SankeyParams(groupAccounts = true),
      )

    val account = data.node("all_income")
    assertThat(account.label).isEqualTo(Income)
    assertThat(account.value).isEqualTo(Amount(1500))
    assertThat(account.color).isEqualTo(Primary)
  }

  @Test
  fun `Smallest categories are grouped into Other per category group`() {
    val data =
      calculate(
        expense(total = -500, categoryId = "big"),
        expense(total = -100, categoryId = "small1"),
        expense(total = -50, categoryId = "small2"),
        params = SankeyParams(topN = 2),
      )

    assertThat(data.nodes.filter { it.column == 2 }.map { it.key })
      .containsExactly("big", "bills__OTHER_BUCKET")
    assertThat(data.node("bills__OTHER_BUCKET").label).isEqualTo(Other)
    assertThat(data.node("bills__OTHER_BUCKET").value).isEqualTo(Amount(150))
  }

  @Test
  fun `Links into Other nodes list the grouped categories, biggest first`() {
    val data =
      calculate(
        expense(total = -500, categoryId = "big", category = "Big"),
        expense(total = -50, categoryId = "small1", category = "Small 1"),
        expense(total = -100, categoryId = "small2", category = "Small 2"),
        params = SankeyParams(topN = 1),
      )

    val link = data.links.single { data.nodes[it.target].key == "bills__OTHER_BUCKET" }
    assertThat(link.grouped)
      .containsExactly(
        SankeyGroupedItem("Big", Amount(500)),
        SankeyGroupedItem("Small 2", Amount(100)),
        SankeyGroupedItem("Small 1", Amount(50)),
      )
    assertThat(data.links.filter { data.nodes[it.target].key != "bills__OTHER_BUCKET" }).each {
      it.transform { link -> link.grouped }.isEmpty()
    }
  }

  @Test
  fun `Grouped categories with the same name are summed`() {
    val data =
      calculate(
        expense(total = -500, categoryId = "big"),
        expense(total = -100, categoryId = "misc1", category = "Misc"),
        expense(total = -50, categoryId = "misc2", category = "Misc"),
        params = SankeyParams(topN = 1),
      )

    val link = data.links.single { data.nodes[it.target].key == "bills__OTHER_BUCKET" }
    assertThat(link.grouped)
      .containsExactly(
        SankeyGroupedItem("big", Amount(500)),
        SankeyGroupedItem("Misc", Amount(150)),
      )
  }

  @Test
  fun `Global sorting groups small categories into one Other node`() {
    val data =
      calculate(
        expense(total = -500, categoryId = "a", groupId = "g1"),
        expense(total = -100, categoryId = "b", groupId = "g1"),
        expense(total = -50, categoryId = "c", groupId = "g2"),
        params = SankeyParams(topN = 2, sort = Global),
      )

    assertThat(data.nodes.filter { it.column == 2 }.map { it.key })
      .containsExactly("a", "category__OTHER_BUCKET")
    assertThat(data.node("category__OTHER_BUCKET").value).isEqualTo(Amount(150))
  }

  @Test
  fun `Per group sorting keeps categories together under their group`() {
    val data =
      calculate(
        expense(total = -100, categoryId = "a1", groupId = "a"),
        expense(total = -500, categoryId = "b1", groupId = "b"),
        expense(total = -300, categoryId = "a2", groupId = "a"),
      )

    assertThat(data.nodes.filter { it.column == 1 }.map { it.key }).containsExactly("b", "a")
    assertThat(data.nodes.filter { it.column == 2 }.map { it.key })
      .containsExactly("b1", "a2", "a1")
  }

  @Test
  fun `Budget order sorting follows the given category order`() {
    val data =
      calculate(
        expense(total = -100, categoryId = "a1", groupId = "a"),
        expense(total = -400, categoryId = "b1", groupId = "b"),
        expense(total = -300, categoryId = "a2", groupId = "a"),
        params = SankeyParams(sort = BudgetOrder),
        categoryOrder = listOf("a", "a1", "a2", "b", "b1"),
      )

    assertThat(data.nodes.filter { it.column == 2 }.map { it.key })
      .containsExactly("a1", "a2", "b1")
  }

  @Test
  fun `Layer range drops layers outside it`() {
    val data =
      calculate(
        income(total = 1000, payeeId = "employer"),
        expense(total = -300, categoryId = "food"),
        params = SankeyParams(layerFrom = Account, layerTo = CategoryGroup),
      )

    assertThat(data.nodes.map { it.key to it.column })
      .containsExactlyInAnyOrder("checking" to 0, "bills" to 1)
  }

  @Test
  fun `Percentages are relative to the column total`() {
    val data =
      calculate(
        expense(total = -300, categoryId = "a"),
        expense(total = -100, categoryId = "b"),
      )

    assertThat(data.node("a").percent.intValue).isEqualTo(75)
    assertThat(data.node("b").percent.intValue).isEqualTo(25)
  }

  @Test
  fun `Transfers push the receiving account into a later column`() {
    val data =
      calculate(
        income(total = 1000, accountId = "checking"),
        expense(total = -300, categoryId = "food", accountId = "savings"),
        transfers =
          aggregateTransferPairs(
            listOf(
              SankeyTransfer("t1", "t2", -400, "checking", "Checking"),
              SankeyTransfer("t2", "t1", 400, "savings", "Savings"),
            ),
          ),
      )

    assertThat(data.flows()).contains(Flow("checking", "savings", 400))
    assertThat(data.node("checking").column).isEqualTo(1)
    assertThat(data.node("savings").column).isEqualTo(2)
    assertThat(data.node("bills").column).isEqualTo(3)
  }

  @Test
  fun `Transfer pairs net out between two accounts`() {
    val pairs =
      aggregateTransferPairs(
        listOf(
          SankeyTransfer("t1", "t2", -400, "a", "A"),
          SankeyTransfer("t2", "t1", 400, "b", "B"),
          SankeyTransfer("t3", "t4", 100, "a", "A"),
          SankeyTransfer("t4", "t3", -100, "b", "B"),
        ),
      )

    assertThat(pairs).single().isEqualTo(SankeyTransferPair("a", "A", "b", "B", 300))
  }

  @Test
  fun `No transactions gives no nodes`() {
    assertThat(calculate().nodes).isEmpty()
  }

  private data class Flow(val from: String, val to: String, val value: Long)

  private fun SankeyData.flows() = links.map { link ->
    Flow(nodes[link.source].key, nodes[link.target].key, link.value.toLong())
  }

  private fun SankeyData.node(key: String) = nodes.first { it.key == key }

  private fun calculate(
    vararg entries: SankeyEntry,
    transfers: List<SankeyTransferPair> = emptyList(),
    categoryOrder: List<String> = emptyList(),
    params: SankeyParams = SankeyParams(),
  ) =
    calculateSankey(
      title = null,
      start = MONTH,
      end = MONTH,
      entries = entries.toList(),
      transfers = transfers,
      categoryOrder = categoryOrder,
      params = params,
    )

  private fun income(
    total: Long,
    accountId: String = "checking",
    payeeId: String? = null,
    payeeName: String? = null,
  ) =
    SankeyEntry(
      categoryGroupId = "income",
      categoryGroup = "Income",
      categoryId = "salary",
      category = "Salary",
      isIncome = true,
      total = total,
      accountId = accountId,
      accountName = accountId,
      payeeId = payeeId,
      payeeName = payeeName,
    )

  private fun expense(
    total: Long,
    categoryId: String,
    category: String = categoryId,
    groupId: String = "bills",
    accountId: String = "checking",
  ) =
    SankeyEntry(
      categoryGroupId = groupId,
      categoryGroup = groupId,
      categoryId = categoryId,
      category = category,
      isIncome = false,
      total = total,
      accountId = accountId,
      accountName = accountId,
    )

  private companion object {
    val MONTH = YearMonth(2024, JANUARY)
  }
}
