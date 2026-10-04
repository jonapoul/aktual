package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.SankeyColor
import aktual.budget.reports.vm.SankeyData
import aktual.budget.reports.vm.SankeyGroupedItem
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
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH

private const val LINK_ALPHA = 0.6f
private const val DIMMED_LINK_ALPHA = 0.2f
private const val PERCENT_DECIMALS = 1
private const val GROUPED_ALPHA = 0.7f

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
private fun Chart(data: SankeyData, compact: Boolean, modifier: Modifier = Modifier) =
  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val textMeasurer = rememberTextMeasurer()
    val theme = colors
    val nameStyle = TextStyle(color = theme.pageText, fontSize = 12.sp, fontWeight = Bold)
    val valueStyle = TextStyle(color = theme.pageText, fontSize = 11.sp)
    val labels = data.nodes.map { it.label.string() }
    val values =
      data.nodes.map { node ->
        if (data.showPercentages) {
          node.percent.toString(PERCENT_DECIMALS)
        } else {
          node.value.formatted()
        }
      }

    val density = LocalDensity.current
    val width = constraints.maxWidth.toFloat()
    val height = constraints.maxHeight.toFloat()
    val layout =
      remember(data, width, height, density) {
        with(density) {
          layoutSankey(
            data = data,
            width = width,
            height = height,
            nodeWidth = NODE_WIDTH.toPx(),
            nodePadding = NODE_PADDING.toPx(),
          )
        }
      }
    val nodeLabels =
      remember(layout, labels, values, nameStyle, valueStyle, textMeasurer, compact) {
        if (compact) {
          []
        } else {
          with(density) {
            nodeLabels(data, layout, width, textMeasurer, labels, values, nameStyle, valueStyle)
          }
        }
      }
    var selection by remember(data) { mutableStateOf<Selection?>(null) }
    val haptics = LocalHapticFeedback.current

    val onSelect by
      rememberUpdatedState<(Selection?) -> Unit> { tapped ->
        selection = if (tapped?.hit == selection?.hit) null else tapped
        if (selection != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
      }
    val tapModifier =
      if (compact) {
        Modifier
      } else {
        Modifier.onTap(layout, nodeLabels.map { it?.bounds }) { onSelect(it) }
      }

    Box(
      modifier =
        Modifier.fillMaxSize().then(tapModifier).drawWithCache {
          val paths = layout.links.map { band -> band.path() }

          onDrawBehind {
            val hit = selection?.hit
            data.links.forEachIndexed { i, link ->
              val alpha =
                when {
                  hit == null -> LINK_ALPHA
                  hit.highlights(i, link) -> 1f
                  else -> DIMMED_LINK_ALPHA
                }
              drawPath(paths[i], color = link.color.resolve(theme), alpha = alpha)
            }

            data.nodes.forEachIndexed { i, node ->
              val rect = layout.nodes.getOrNull(i) ?: return@forEachIndexed
              drawRect(color = node.color.resolve(theme), topLeft = rect.topLeft, size = rect.size)

              nodeLabels.getOrNull(i)?.let { label -> drawNodeLabel(label) }
            }
          }
        }
    )

    selection?.let { (hit, position) ->
      val tooltipModifier = Modifier.tooltipPosition(position)
      when (hit) {
        is Node -> {
          Tooltip(
            title = labels[hit.index],
            value = data.nodes[hit.index].value,
            percent = data.nodes[hit.index].percent,
            modifier = tooltipModifier,
          )
        }

        is Link -> {
          val link = data.links[hit.index]
          Tooltip(
            title = Strings.reportsSankeyLink(labels[link.source], labels[link.target]),
            value = link.value,
            grouped = link.grouped,
            modifier = tooltipModifier,
          )
        }
      }
    }
  }

private fun Modifier.onTap(
  layout: SankeyLayout,
  labelBounds: List<Rect?>,
  onSelect: (Selection?) -> Unit,
): Modifier =
  pointerInput(layout, labelBounds) {
    detectTapGestures { offset ->
      val hit = layout.hitTest(offset, TAP_SLOP.toPx(), labelBounds)
      onSelect(hit?.let { Selection(it, offset) })
    }
  }

private data class NodeLabel(
  val name: TextLayoutResult,
  val value: TextLayoutResult,
  val bounds: Rect,
  val onLeft: Boolean,
)

// Null for nodes whose label would overlap its neighbours
private fun Density.nodeLabels(
  data: SankeyData,
  layout: SankeyLayout,
  width: Float,
  textMeasurer: TextMeasurer,
  labels: List<String>,
  values: List<String>,
  nameStyle: TextStyle,
  valueStyle: TextStyle,
): List<NodeLabel?> {
  val nodeWidth = NODE_WIDTH.toPx()
  val labelGap = LABEL_GAP.toPx()
  val lastColumn = data.nodes.maxOf { it.column }
  val columnSpacing = if (lastColumn == 0) width else (width - nodeWidth) / lastColumn
  val constraints =
    Constraints(maxWidth = (columnSpacing - nodeWidth - labelGap * 2).toInt().coerceAtLeast(0))

  return data.nodes.mapIndexed { i, node ->
    val rect = layout.nodes.getOrNull(i) ?: return@mapIndexed null
    val name = textMeasurer.measureLabel(labels[i], nameStyle, constraints)
    val value = textMeasurer.measureLabel(values[i], valueStyle, constraints)
    val textHeight = name.size.height + value.size.height
    if (rect.height + NODE_PADDING.toPx() < textHeight) return@mapIndexed null

    val textWidth = max(name.size.width, value.size.width)
    val onLeft = node.column == lastColumn && lastColumn > 0
    val left = if (onLeft) rect.left - labelGap - textWidth else rect.right + labelGap
    val top = rect.center.y - textHeight / 2
    NodeLabel(name, value, Rect(left, top, left + textWidth, top + textHeight), onLeft)
  }
}

private fun TextMeasurer.measureLabel(
  text: String,
  style: TextStyle,
  constraints: Constraints,
): TextLayoutResult =
  measure(
    text = text,
    style = style,
    overflow = Ellipsis,
    maxLines = 1,
    constraints = constraints,
  )

private fun DrawScope.drawNodeLabel(label: NodeLabel) {
  val bounds = label.bounds
  fun x(width: Int) = if (label.onLeft) bounds.right - width else bounds.left
  drawText(label.name, topLeft = Offset(x(label.name.size.width), bounds.top))
  drawText(
    label.value,
    topLeft = Offset(x(label.value.size.width), bounds.top + label.name.size.height),
  )
}

private data class Selection(val hit: SankeyHit, val position: Offset)

private fun SankeyHit.highlights(index: Int, link: SankeyLink): Boolean =
  when (this) {
    is Link -> this.index == index
    is Node -> this.index == link.source || this.index == link.target
  }

@Composable
private fun Tooltip(
  title: String,
  value: Amount,
  modifier: Modifier = Modifier,
  percent: Percent? = null,
  grouped: ImmutableList<SankeyGroupedItem> = persistentListOf(),
) =
  Column(
    modifier =
      modifier
        .widthIn(max = TOOLTIP_MAX_WIDTH)
        .shadow(TOOLTIP_ELEVATION, TOOLTIP_SHAPE)
        .background(colors.menuBackground, TOOLTIP_SHAPE)
        .padding(10.dp)
  ) {
    Text(text = title, color = colors.menuItemText, style = typography.bodyMedium)
    Row {
      Text(
        text = value.formatted(),
        color = colors.menuItemText,
        style = typography.bodyMedium,
        fontWeight = Bold,
      )

      if (percent != null) {
        HorizontalSpacer(6.dp)
        Text(
          text = percent.toString(PERCENT_DECIMALS),
          color = colors.menuItemText.copy(alpha = GROUPED_ALPHA),
          style = typography.bodyMedium,
        )
      }
    }

    if (grouped.isNotEmpty()) {
      VerticalSpacer(6.dp)
      grouped.fastForEach { item -> GroupedItemRow(item) }
    }
  }

@Composable
private fun GroupedItemRow(item: SankeyGroupedItem, modifier: Modifier = Modifier) =
  Row(modifier = modifier) {
    val style = typography.bodySmall
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = item.name,
      color = colors.menuItemText.copy(alpha = GROUPED_ALPHA),
      style = style,
      maxLines = 1,
      overflow = Ellipsis,
    )
    HorizontalSpacer(8.dp)
    Text(
      text = item.value.formatted(),
      color = colors.menuItemText.copy(alpha = GROUPED_ALPHA),
      style = style,
    )
  }

// Puts the tooltip beside the tapped point, flipping to the left when it would run off the right
// edge,
// and keeps it inside the chart
private fun Modifier.tooltipPosition(position: Offset): Modifier =
  layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(placeable.width, placeable.height) {
      val gap = TOOLTIP_GAP.roundToPx()
      val tapX = position.x.roundToInt()
      val right = tapX + gap
      val x =
        if (right + placeable.width <= constraints.maxWidth) right else tapX - gap - placeable.width
      val y = position.y.roundToInt() - placeable.height / 2
      placeable.place(
        x = x.coerceIn(0, max(0, constraints.maxWidth - placeable.width)),
        y = y.coerceIn(0, max(0, constraints.maxHeight - placeable.height)),
      )
    }
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
        [
            chartQual1,
            chartQual2,
            chartQual3,
            chartQual4,
            chartQual5,
            chartQual6,
            chartQual7,
            chartQual8,
            chartQual9,
          ]
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
@ReadOnlyComposable
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
private val TAP_SLOP = 8.dp
private val TOOLTIP_GAP = 12.dp
private val TOOLTIP_MAX_WIDTH = 240.dp
private val TOOLTIP_ELEVATION = 4.dp
private val TOOLTIP_SHAPE = RoundedCornerShape(4.dp)

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
