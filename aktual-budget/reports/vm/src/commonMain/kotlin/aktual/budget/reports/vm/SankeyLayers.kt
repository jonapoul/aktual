package aktual.budget.reports.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// packages/desktop-client/src/components/reports/reports/Sankey.tsx
data class SankeyLayerRange(val from: SankeyLayer, val to: SankeyLayer)

enum class SankeyLayerEnd {
  From,
  To,
}

// Budgeted graphs have no payees, and spent ones no budget column
fun sankeyLayers(mode: SankeyMode?): ImmutableList<SankeyLayer> =
  SankeyLayer.entries
    .filter { layer -> if (mode == Budgeted) layer != IncomePayee else layer != Budget }
    .toImmutableList()

fun defaultSankeyLayers(mode: SankeyMode?): SankeyLayerRange =
  SankeyLayerRange(from = if (mode == Budgeted) IncomeCategory else IncomePayee, to = Category)

// normalizeLayerRange(): keeps from before to, moving the other end when one is changed past it
fun normalizeSankeyLayers(
  mode: SankeyMode?,
  from: SankeyLayer?,
  to: SankeyLayer?,
  changed: SankeyLayerEnd? = null,
): SankeyLayerRange {
  val layers = sankeyLayers(mode)
  val fallback = defaultSankeyLayers(mode)
  var start = from?.takeIf { it in layers } ?: fallback.from
  var end = to?.takeIf { it in layers } ?: fallback.to

  if (layers.indexOf(start) >= layers.indexOf(end)) {
    when (changed) {
      From -> {
        val index = maxOf(layers.indexOf(start) + 1, layers.indexOf(fallback.to))
        end = layers[minOf(layers.lastIndex, index)]
      }
      To -> {
        val index = minOf(layers.indexOf(end) - 1, layers.indexOf(fallback.from))
        start = layers[maxOf(0, index)]
      }
      null -> {
        start = fallback.from
        end = fallback.to
      }
    }
  }

  return if (layers.indexOf(start) >= layers.indexOf(end)) {
    fallback
  } else {
    SankeyLayerRange(start, end)
  }
}

// The layers a menu can pick for one end, given where the other end is
fun sankeyLayerChoices(
  mode: SankeyMode?,
  end: SankeyLayerEnd,
  other: SankeyLayer,
): ImmutableList<SankeyLayer> {
  val layers = sankeyLayers(mode)
  val index = layers.indexOf(other)
  if (index == -1) return persistentListOf()
  return when (end) {
    From -> layers.subList(0, index)
    To -> layers.subList(index + 1, layers.size)
  }.toImmutableList()
}
