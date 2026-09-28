package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.SankeyColor
import aktual.budget.reports.vm.SankeyData
import aktual.budget.reports.vm.SankeyLabel
import aktual.budget.reports.vm.SankeyLink
import aktual.budget.reports.vm.SankeyNode
import aktual.core.l10n.Strings
import aktual.core.model.Percent
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.stringShort
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH

private const val LINK_ALPHA = 0.6f
private const val PERCENT_DECIMALS = 1

@Composable
internal fun SankeyChart(
  data: SankeyData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      Header(modifier = Modifier.fillMaxWidth(), data = data)
    }

    val chartModifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f)
    if (data.nodes.isEmpty()) {
      Box(modifier = chartModifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(
          text = Strings.reportsSankeyEmpty,
          color = colors.pageTextSubdued,
          textAlign = Center,
        )
      }
    } else {
      Chart(modifier = chartModifier.padding(4.dp), data = data, compact = compact)
    }
  }

@Composable
private fun Header(data: SankeyData, modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)) {
    Text(
      text = data.title ?: Strings.reportsChooseTypeSankey,
      overflow = Ellipsis,
      color = colors.pageText,
      style = typography.bodyLarge,
    )

    val range =
      if (data.start == data.end) data.start.stringShort() else dateRange(data.start, data.end)
    DateRangeText(Strings.reportsSankeySpent(range))
  }

@Composable
private fun Chart(data: SankeyData, compact: Boolean, modifier: Modifier = Modifier) {
  val textMeasurer = rememberTextMeasurer()
  val theme = colors
  val nameStyle = TextStyle(color = theme.pageText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
  val valueStyle = TextStyle(color = theme.pageText, fontSize = 11.sp)
  val labels = data.nodes.map { it.label.string() }
  val values =
    data.nodes.map { node ->
      if (data.showPercentages) node.percent.toString(PERCENT_DECIMALS) else node.value.formatted()
    }

  Box(
    modifier =
      modifier.fillMaxSize().drawWithCache {
        val nodeWidth = NODE_WIDTH.toPx()
        val labelGap = LABEL_GAP.toPx()
        val layout =
          layoutSankey(
            data = data,
            width = size.width,
            height = size.height,
            nodeWidth = nodeWidth,
            nodePadding = NODE_PADDING.toPx(),
          )
        val lastColumn = data.nodes.maxOf { it.column }
        val columnSpacing =
          if (lastColumn == 0) size.width else (size.width - nodeWidth) / lastColumn
        val labelConstraints =
          Constraints(
            maxWidth = (columnSpacing - nodeWidth - labelGap * 2).toInt().coerceAtLeast(0)
          )

        val paths = layout.links.map { band -> band.path() }
        val texts =
          if (compact) {
            emptyList()
          } else {
            data.nodes.indices.map { i ->
              val name =
                textMeasurer.measure(
                  text = labels[i],
                  style = nameStyle,
                  overflow = Ellipsis,
                  maxLines = 1,
                  constraints = labelConstraints,
                )
              val value =
                textMeasurer.measure(
                  text = values[i],
                  style = valueStyle,
                  maxLines = 1,
                  constraints = labelConstraints,
                )
              name to value
            }
          }

        onDrawBehind {
          data.links.forEachIndexed { i, link ->
            drawPath(paths[i], color = link.color.resolve(theme), alpha = LINK_ALPHA)
          }

          data.nodes.forEachIndexed { i, node ->
            val rect = layout.nodes[i]
            drawRect(color = node.color.resolve(theme), topLeft = rect.topLeft, size = rect.size)

            val (name, value) = texts.getOrNull(i) ?: return@forEachIndexed
            val textHeight = name.size.height + value.size.height
            // Skip labels that would overlap their neighbours
            if (rect.height + NODE_PADDING.toPx() < textHeight) return@forEachIndexed

            val onLeft = node.column == lastColumn && lastColumn > 0
            val top = rect.center.y - textHeight / 2
            fun x(width: Int) = if (onLeft) rect.left - labelGap - width else rect.right + labelGap
            drawText(name, topLeft = Offset(x(name.size.width), top))
            drawText(value, topLeft = Offset(x(value.size.width), top + name.size.height))
          }
        }
      }
  )
}

private fun LinkBand.path(): Path {
  val mid = (x0 + x1) / 2
  val half = thickness / 2
  return Path().apply {
    moveTo(x0, y0 - half)
    cubicTo(mid, y0 - half, mid, y1 - half, x1, y1 - half)
    lineTo(x1, y1 + half)
    cubicTo(mid, y1 + half, mid, y0 + half, x0, y0 + half)
    close()
  }
}

private fun SankeyColor.resolve(theme: Colors): Color =
  when (this) {
    Primary -> theme.reportsBlue
    Negative -> theme.toBudgetNegative
    is Palette ->
      with(theme) {
        listOf(
            chartQual1,
            chartQual2,
            chartQual3,
            chartQual4,
            chartQual5,
            chartQual6,
            chartQual7,
            chartQual8,
            chartQual9,
          )
          .getOrElse(index) { chartQual1 }
      }
  }

@Composable
private fun SankeyLabel.string(): String =
  when (this) {
    is Text -> value
    SankeyLabel.Income -> Strings.reportsSankeyIncome
    Other -> Strings.reportsSankeyOther
  }

@Composable
private fun Amount.formatted(): String =
  toString(
    numberFormatConfig = LocalNumberFormatConfig.current,
    currencyConfig = LocalCurrencyConfig.current,
    includeSign = false,
    isPrivacyEnabled = LocalPrivacyEnabled.current,
  )

private val NODE_WIDTH = 8.dp
private val NODE_PADDING = 16.dp
private val LABEL_GAP = 4.dp

@Preview
@Composable
private fun PreviewSankeyChart(
  @PreviewParameter(SankeyChartProvider::class) params: ColoredParams<SankeyChartParams>
) =
  PreviewWithColors(params.colors) {
    SankeyChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .height(if (params.data.compact) 300.dp else 500.dp)
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
    )
  }

private data class SankeyChartParams(val data: SankeyData, val compact: Boolean)

private class SankeyChartProvider :
  ColoredParameterProvider<SankeyChartParams>(
    SankeyChartParams(PREVIEW_SANKEY_DATA, compact = true),
    SankeyChartParams(PREVIEW_SANKEY_DATA.copy(showPercentages = true), compact = false),
    SankeyChartParams(
      PREVIEW_SANKEY_DATA.copy(nodes = persistentListOf(), links = persistentListOf()),
      compact = true,
    ),
  )

private const val PREVIEW_TOTAL = 5000.0

private fun node(key: String, column: Int, value: Double, color: Int, label: SankeyLabel? = null) =
  SankeyNode(
    key = key,
    label = label ?: SankeyLabel.Text(key),
    column = column,
    value = Amount(value),
    percent = Percent(value, PREVIEW_TOTAL),
    color = SankeyColor.Palette(color),
  )

private fun link(source: Int, target: Int, value: Double, color: Int) =
  SankeyLink(source, target, Amount(value), SankeyColor.Palette(color))

internal val PREVIEW_SANKEY_DATA =
  SankeyData(
    title = "Money flow",
    start = date(2026, JANUARY),
    end = date(2026, MARCH),
    showPercentages = false,
    nodes =
      persistentListOf(
        node("Employer", column = 0, value = 4000.0, color = 0),
        node("Clients", column = 0, value = 1000.0, color = 1),
        node("Salary", column = 1, value = 4000.0, color = 2),
        node("Freelance", column = 1, value = 1000.0, color = 3),
        node("Checking", column = 2, value = 5000.0, color = 4),
        node("Bills", column = 3, value = 2500.0, color = 5),
        node("Food", column = 3, value = 1500.0, color = 6),
        node("Fun", column = 3, value = 1000.0, color = 7),
        node("Rent", column = 4, value = 2000.0, color = 8),
        node("Utilities", column = 4, value = 500.0, color = 0),
        node("Groceries", column = 4, value = 1000.0, color = 1),
        node("Restaurants", column = 4, value = 500.0, color = 2),
        node("Hobbies", column = 4, value = 1000.0, color = 3, label = Other),
      ),
    links =
      persistentListOf(
        link(0, 2, 4000.0, color = 0),
        link(1, 3, 1000.0, color = 1),
        link(2, 4, 4000.0, color = 2),
        link(3, 4, 1000.0, color = 3),
        link(4, 5, 2500.0, color = 4),
        link(4, 6, 1500.0, color = 4),
        link(4, 7, 1000.0, color = 4),
        link(5, 8, 2000.0, color = 5),
        link(5, 9, 500.0, color = 5),
        link(6, 10, 1000.0, color = 6),
        link(6, 11, 500.0, color = 6),
        link(7, 12, 1000.0, color = 7),
      ),
  )
