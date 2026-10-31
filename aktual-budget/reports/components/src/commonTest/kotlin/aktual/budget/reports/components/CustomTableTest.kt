package aktual.budget.reports.components

import aktual.budget.model.Amount
import aktual.budget.reports.components.CustomTableRow.Amounts
import aktual.budget.reports.vm.CustomData
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test

class CustomTableTest {
  @Test
  fun `Grouping by category shows each category group above its categories`() {
    val data = previewCustomData(TableGraph, Category, Payment)
    val rows = rows(data)

    assertThat(rows.map { it.label() })
      .containsExactly(
        "Expenses",
        "Rent",
        "Holiday",
        "Groceries",
        "Eating out",
        "Transport",
        "-",
      )
    assertThat(rows.first()).isInstanceOf<Amounts>().prop(Amounts::isGroupHeader).isTrue()
    assertThat(rows[1]).isInstanceOf<Amounts>().prop(Amounts::isGroupHeader).isFalse()
  }

  @Test
  fun `Total mode shows the total and the average`() {
    val data = previewCustomData(TableGraph, Payee, Payment)
    val rent = rows(data).first() as Amounts

    assertThat(rent).all {
      prop(Amounts::name).isEqualTo("Rent")
      prop(Amounts::amounts).isEqualTo(amounts(-725000, -120833))
    }
  }

  @Test
  fun `Net totals also show deposits and payments`() {
    val data = previewCustomData(TableGraph, Payee, Net)
    val salary = rows(data).first() as Amounts

    assertThat(salary).all {
      prop(Amounts::name).isEqualTo("Salary")
      prop(Amounts::amounts).isEqualTo(amounts(1980000, 0, 1980000, 330000))
    }
  }

  @Test
  fun `Time mode shows a column per interval`() {
    val data = previewCustomData(TableGraph, Payee, Payment, mode = Time)
    val holiday = rows(data)[1] as Amounts

    assertThat(holiday).all {
      prop(Amounts::name).isEqualTo("Holiday")
      prop(Amounts::amounts).isEqualTo(amounts(0, 0, -65000, 0, 0, -240000, -305000, -50833))
    }
  }

  @Test
  fun `Grouping by interval shows a row per interval`() {
    val data = previewCustomData(TableGraph, Interval, Net)
    val rows = rows(data)

    assertThat(rows.map { it.label() }).containsExactly("0", "1", "2", "3", "4", "5")
    assertThat((rows.first() as Amounts).amounts).isEqualTo(amounts(320000, -184000, 136000, 22667))
  }

  @Test
  fun `Totals sum every group`() {
    val data = previewCustomData(TableGraph, Payee, Payment, mode = Time)
    val totals = customTableTotals(data, "Totals")

    assertThat(totals).all {
      prop(Amounts::name).isEqualTo("Totals")
      prop(Amounts::amounts)
        .isEqualTo(
          amounts(-184000, -176000, -262500, -176000, -181500, -443000, -1423000, -237167),
        )
    }
  }

  @Test
  fun `Average is over every interval in the dates, including trimmed ones`() {
    val data = previewCustomData(TableGraph, Payee, Payment).copy(intervalCount = 12)
    val rent = rows(data).first() as Amounts
    assertThat(rent.amounts.last()).isEqualTo(Amount(-60417))
  }

  private fun rows(data: CustomData) =
    customTableRows(
      data = data,
      name = { (it as? Category)?.name ?: (it as Group).name },
      intervalNames = List(data.intervals.size) { "$it" },
    )

  private fun CustomTableRow.label() =
    when (this) {
      is Amounts -> name
      Gap -> "-"
    }

  private fun amounts(vararg values: Long) = values.map { Amount(it) }
}
