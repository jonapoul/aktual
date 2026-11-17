package aktual.prefs.ui

import aktual.budget.model.Amount
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.Split
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.theme.hasAlternateRowColour
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.datetime.LocalDate

// Scrollable list of made-up transactions, for previews that need some content to draw over. Odd
// rows are shaded like the real list when the theme has an alternate row colour
@Composable
internal fun SampleTransactionList(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
  alternateRowColours: Boolean = false,
  expandSplits: Boolean = false,
) {
  val shadeRows = alternateRowColours && colors.hasAlternateRowColour
  Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(contentPadding)) {
    val swatches = swatches()
    SampleTransactions.fastForEachIndexed { i, transaction ->
      val background =
        if (shadeRows && i % 2 == 1) colors.tableRowBackgroundAlternate else Color.Transparent
      Column(modifier = Modifier.fillMaxWidth().background(background)) {
        SampleRow(
          transaction = transaction,
          color = swatches[i % swatches.size],
          expanded = expandSplits,
        )
        if (transaction.parts.isNotEmpty()) {
          AnimatedVisibility(visible = expandSplits) {
            Column { transaction.parts.fastForEach { SamplePartRow(it) } }
          }
        }
      }
    }
  }
}

@Composable
private fun SampleRow(transaction: SampleTransaction, color: Color, expanded: Boolean) {
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
      if (transaction.parts.isEmpty()) {
        Text(
          text = transaction.date.formatted(),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 1,
        )
      } else {
        SplitLabel(expanded)
      }
    }
    AmountText(transaction.amount, typography.bodyMedium)
  }
}

@Composable
private fun SplitLabel(expanded: Boolean) {
  val rotation by animateFloatAsState(if (expanded) HALF_TURN else 0f)
  Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Icon(
      modifier = Modifier.size(SplitIconSize),
      imageVector = AktualIcons.Split,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
    Text(
      text = Strings.transactionsSplit,
      style = typography.bodySmall,
      fontStyle = Italic,
      color = colors.pageTextSubdued,
      maxLines = 1,
    )
    Icon(
      modifier = Modifier.size(SplitIconSize).rotate(rotation),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
  }
}

@Composable
private fun SamplePartRow(part: SamplePart) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(start = PartIndent, end = 12.dp, bottom = 6.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = part.category.label(),
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
      maxLines = 1,
    )
    AmountText(part.amount, typography.bodySmall)
  }
}

@Composable
private fun AmountText(amount: Amount, style: TextStyle) =
  Text(
    text = amount.formattedString(isPrivacyEnabled = false),
    style = style,
    color = if (amount.isPositive()) colors.numberPositive else colors.numberNegative,
  )

private const val HALF_TURN = 180f
private val SplitIconSize = 12.dp

// Lines the parts up with the payee, past the swatch
private val PartIndent = 52.dp

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
  Supermarket,
}

private enum class SampleCategory {
  Food,
  Household,
}

@Composable
private fun SampleCategory.label(): String =
  when (this) {
    Food -> Strings.settingsSampleCategoryFood
    Household -> Strings.settingsSampleCategoryHousehold
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
    Supermarket -> Strings.settingsSamplePayeeSupermarket
  }

private data class SampleTransaction(
  val payee: SamplePayee,
  val date: LocalDate,
  val amount: Amount,
  val parts: List<SamplePart> = emptyList(),
)

private data class SamplePart(val category: SampleCategory, val amount: Amount)

private fun sample(payee: SamplePayee, day: Int, amount: Double, vararg parts: SamplePart) =
  SampleTransaction(
    payee = payee,
    date = LocalDate(year = 2026, month = 3, day = day),
    amount = Amount(amount),
    parts = parts.toList(),
  )

private fun part(category: SampleCategory, amount: Double) = SamplePart(category, Amount(amount))

private val SampleTransactions =
  listOf(
    sample(Groceries, day = 28, amount = -64.99),
    sample(Salary, day = 27, amount = 2_450.0),
    sample(
      Supermarket,
      day = 26,
      amount = -86.4,
      part(Food, amount = -62.15),
      part(Household, amount = -24.25),
    ),
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
  PreviewWithColors(colors) {
    SampleTransactionList(alternateRowColours = true, expandSplits = true)
  }
