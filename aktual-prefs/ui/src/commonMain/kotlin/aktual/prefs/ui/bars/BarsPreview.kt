package aktual.prefs.ui.bars

import aktual.budget.model.Amount
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.CloudCheck
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.HazeConfig
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.hazedBottomBar
import aktual.core.ui.hazedTopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.LocalDate

// A mock screen with a top and bottom bar drawn by the app's real bar effects, over a scrollable
// list of made-up transactions so there's something behind the bars to blur
@Composable
internal fun BarsPreview(config: HazeConfig, modifier: Modifier = Modifier) =
  HazedBars(
    config = config,
    barHeight = BarHeight,
    modifier = modifier.fillMaxWidth().height(PreviewHeight),
    topBar = {
      Icon(
        modifier = Modifier.padding(horizontal = 12.dp).size(20.dp),
        imageVector = AktualIcons.CheveronLeft,
        contentDescription = null,
        tint = colors.pageText,
      )
      Text(
        text = Strings.transactionsTitleAll,
        style = typography.titleMedium,
        color = colors.pageText,
        maxLines = 1,
      )
    },
    bottomBar = {
      Icon(
        modifier = Modifier.padding(horizontal = 12.dp).size(16.dp),
        imageVector = AktualIcons.CloudCheck,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
      Text(
        text = Strings.connectionConnected,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    },
  ) { topPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(top = topPadding, bottom = BarHeight),
    ) {
      val swatches = swatches()
      SampleTransactions.fastForEachIndexed { i, transaction ->
        SampleRow(transaction = transaction, color = swatches[i % swatches.size])
      }
    }
  }

@Composable
private fun HazedBars(
  config: HazeConfig,
  barHeight: Dp,
  topBar: @Composable RowScope.() -> Unit,
  bottomBar: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (topPadding: Dp) -> Unit,
) {
  val hazeState = rememberHazeState()
  // With no effect the app lays content out below the top bar instead of behind it
  val topPadding = if (config.appBarEffect == None) barHeight else 0.dp
  Box(modifier = modifier.clip(CardShape)) {
    Box(
      modifier = Modifier.matchParentSize().hazeSource(hazeState).background(colors.pageBackground),
    ) {
      content(topPadding)
    }

    Row(
      modifier =
        Modifier.align(TopCenter)
          .fillMaxWidth()
          .height(barHeight)
          .hazedTopBar(
            state = HazedTopBarState(hazeState, config.appBarEffect),
            scrollOffset = { Float.MAX_VALUE },
            config = config,
          ),
      verticalAlignment = CenterVertically,
      content = topBar,
    )

    Row(
      modifier =
        Modifier.align(BottomCenter)
          .fillMaxWidth()
          .height(barHeight)
          .hazedBottomBar(state = hazeState, config = config),
      verticalAlignment = CenterVertically,
      content = bottomBar,
    )
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
    Groceries -> Strings.settingsBarsSampleGroceries
    Salary -> Strings.settingsBarsSampleSalary
    Coffee -> Strings.settingsBarsSampleCoffee
    Electricity -> Strings.settingsBarsSampleElectricity
    Rent -> Strings.settingsBarsSampleRent
    Refund -> Strings.settingsBarsSampleRefund
    Restaurant -> Strings.settingsBarsSampleRestaurant
    Insurance -> Strings.settingsBarsSampleInsurance
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

private val BarHeight = 44.dp
private val PreviewHeight = 220.dp

@Preview
@Composable
private fun PreviewBarsPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { BarsPreview(HazeConfig(appBarEffect = Blur)) }
