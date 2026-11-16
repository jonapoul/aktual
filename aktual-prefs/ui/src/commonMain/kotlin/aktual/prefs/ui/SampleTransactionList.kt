package aktual.prefs.ui

import aktual.budget.model.Amount
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.datetime.LocalDate

// Scrollable list of made-up transactions, for previews that need some content to draw over
@Composable
internal fun SampleTransactionList(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(contentPadding)) {
    val swatches = swatches()
    SampleTransactions.fastForEachIndexed { i, transaction ->
      SampleRow(transaction = transaction, color = swatches[i % swatches.size])
    }
  }
}

@Composable
private fun SampleRow(transaction: SampleTransaction, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = transaction.payee.label(),
        style = typography.bodyMedium,
        color = colors.pageText,
        maxLines = 1,
      )
      Text(
        text = transaction.date.formatted(),
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    }
    Text(
      text = transaction.amount.formattedString(isPrivacyEnabled = false),
      style = typography.bodyMedium,
      color = if (transaction.amount.isPositive()) colors.numberPositive else colors.numberNegative,
    )
  }
}

@Composable
@ReadOnlyComposable
private fun swatches(): List<Color> =
  listOf(colors.chartQual1, colors.chartQual2, colors.chartQual3, colors.chartQual4)

private enum class SamplePayee {
  Groceries,
  Salary,
  Coffee,
  Electricity,
  Rent,
  Refund,
  Restaurant,
  Insurance,
}

@Composable
private fun SamplePayee.label(): String =
  when (this) {
    Groceries -> Strings.settingsSamplePayeeGroceries
    Salary -> Strings.settingsSamplePayeeSalary
    Coffee -> Strings.settingsSamplePayeeCoffee
    Electricity -> Strings.settingsSamplePayeeElectricity
    Rent -> Strings.settingsSamplePayeeRent
    Refund -> Strings.settingsSamplePayeeRefund
    Restaurant -> Strings.settingsSamplePayeeRestaurant
    Insurance -> Strings.settingsSamplePayeeInsurance
  }

private data class SampleTransaction(
  val payee: SamplePayee,
  val date: LocalDate,
  val amount: Amount,
)

private fun sample(payee: SamplePayee, day: Int, amount: Double) =
  SampleTransaction(payee, LocalDate(year = 2026, month = 3, day = day), Amount(amount))

private val SampleTransactions =
  listOf(
    sample(Groceries, day = 28, amount = -64.99),
    sample(Salary, day = 27, amount = 2_450.0),
    sample(Coffee, day = 26, amount = -4.75),
    sample(Electricity, day = 24, amount = -120.0),
    sample(Rent, day = 21, amount = -950.0),
    sample(Refund, day = 19, amount = 35.0),
    sample(Restaurant, day = 17, amount = -42.0),
    sample(Insurance, day = 14, amount = -58.2),
  )

@Preview
@Composable
private fun PreviewSampleTransactionList(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SampleTransactionList() }
