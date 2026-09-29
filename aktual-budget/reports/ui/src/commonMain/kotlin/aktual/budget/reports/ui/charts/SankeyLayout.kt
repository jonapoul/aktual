package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.SankeyData
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private const val RELAX_ITERATIONS = 32
private const val RELAX_DECAY = 0.99f
private const val MAX_PADDING_FRACTION = 0.4f
private const val BEZIER_SEARCH_STEPS = 24
private const val CUBIC_COEFFICIENT = 3

internal data class SankeyLayout(val nodes: List<Rect>, val links: List<LinkBand>)

// A band between two nodes, where the y values are the band's centre at each end
internal data class LinkBand(
  val x0: Float,
  val y0: Float,
  val x1: Float,
  val y1: Float,
  val thickness: Float,
)

internal sealed interface SankeyHit {
  @JvmInline value class Node(val index: Int) : SankeyHit

  @JvmInline value class Link(val index: Int) : SankeyHit
}

// Nodes and their labels take priority over links. When several links are in reach, the one whose
// centre is closest wins
internal fun SankeyLayout.hitTest(
  point: Offset,
  slop: Float,
  labels: List<Rect?> = emptyList(),
): SankeyHit? {
  val node =
    nodes.indices.indexOfFirst { i ->
      nodes[i].inflate(slop).contains(point) || labels.getOrNull(i)?.contains(point) == true
    }
  if (node >= 0) return SankeyHit.Node(node)

  val link =
    links.indices
      .mapNotNull { i ->
        val distance = links[i].distanceFromCentre(point) ?: return@mapNotNull null
        if (distance <= links[i].thickness / 2 + slop) i to distance else null
      }
      .minByOrNull { it.second }
  return link?.let { SankeyHit.Link(it.first) }
}

// Vertical distance from the point to the band's centre line, or null if it's outside the band's x
// range
private fun LinkBand.distanceFromCentre(point: Offset): Float? {
  if (point.x < x0 || point.x > x1) return null
  val mid = (x0 + x1) / 2

  // x only ever increases along the curve, so binary search for where it reaches the point
  var low = 0f
  var high = 1f
  repeat(BEZIER_SEARCH_STEPS) {
    val t = (low + high) / 2
    if (cubic(t, x0, mid, mid, x1) < point.x) low = t else high = t
  }
  val t = (low + high) / 2
  return abs(point.y - cubic(t, y0, y0, y1, y1))
}

private fun cubic(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
  val u = 1 - t
  return u * u * u * p0 +
    CUBIC_COEFFICIENT * u * u * t * p1 +
    CUBIC_COEFFICIENT * u * t * t * p2 +
    t * t * t * p3
}

// Node order within each column is kept as given, and nodes are only nudged up or down to line up
// with the nodes they're linked to
internal fun layoutSankey(
  data: SankeyData,
  width: Float,
  height: Float,
  nodeWidth: Float,
  nodePadding: Float,
): SankeyLayout {
  val nodes = data.nodes
  if (nodes.isEmpty() || width <= 0f || height <= 0f) return SankeyLayout(emptyList(), emptyList())

  val values = nodes.map { it.value.toLong().toFloat() }
  val columns = nodes.indices.groupBy { nodes[it].column }.toSortedMap().values.toList()
  val lastColumn = nodes.maxOf { it.column }
  val maxCount = columns.maxOf { it.size }
  val padding =
    if (maxCount > 1) min(nodePadding, height * MAX_PADDING_FRACTION / (maxCount - 1)) else 0f
  val scale = columns.minOf { column ->
    val total = column.sumOf { values[it].toDouble() }.toFloat()
    if (total > 0f) (height - (column.size - 1) * padding) / total else Float.MAX_VALUE
  }

  val x =
    FloatArray(nodes.size) { i ->
      if (lastColumn == 0) 0f else nodes[i].column * (width - nodeWidth) / lastColumn
    }
  val h = FloatArray(nodes.size) { values[it] * scale }
  val y = FloatArray(nodes.size)

  columns.forEach { column -> stackCentred(column, y, h, padding, height) }

  // Link indices into and out of each node
  val incoming = nodes.indices.map { i -> data.links.indices.filter { data.links[it].target == i } }
  val outgoing = nodes.indices.map { i -> data.links.indices.filter { data.links[it].source == i } }

  fun centre(i: Int) = y[i] + h[i] / 2

  fun relax(column: List<Int>, alpha: Float, fromSources: Boolean) {
    for (i in column) {
      val links = (if (fromSources) incoming[i] else outgoing[i]).map { data.links[it] }
      val total = links.sumOf { it.value.toLong() }
      if (total == 0L) continue
      val target =
        links.sumOf { link ->
          val other = if (fromSources) link.source else link.target
          centre(other).toDouble() * link.value.toLong()
        } / total
      y[i] += (target.toFloat() - centre(i)) * alpha
    }
    resolveCollisions(column, y, h, padding, height)
  }

  repeat(RELAX_ITERATIONS) { iteration ->
    val alpha = RELAX_DECAY.pow(iteration)
    columns.drop(1).forEach { relax(it, alpha, fromSources = true) }
    columns.dropLast(1).asReversed().forEach { relax(it, alpha, fromSources = false) }
  }

  val thickness = data.links.map { it.value.toLong() * scale }
  val bandStarts = FloatArray(data.links.size)
  val bandEnds = FloatArray(data.links.size)
  for (i in nodes.indices) {
    stackBands(outgoing[i].sortedBy { centre(data.links[it].target) }, y[i], thickness, bandStarts)
    stackBands(incoming[i].sortedBy { centre(data.links[it].source) }, y[i], thickness, bandEnds)
  }

  val bands =
    data.links.mapIndexed { l, link ->
      LinkBand(
        x0 = x[link.source] + nodeWidth,
        y0 = bandStarts[l],
        x1 = x[link.target],
        y1 = bandEnds[l],
        thickness = thickness[l],
      )
    }
  val rects = nodes.indices.map { i -> Rect(x[i], y[i], x[i] + nodeWidth, y[i] + h[i]) }
  return SankeyLayout(rects, bands)
}

private fun stackCentred(
  column: List<Int>,
  y: FloatArray,
  h: FloatArray,
  padding: Float,
  height: Float,
) {
  var top = (height - column.sumOf { h[it].toDouble() }.toFloat() - (column.size - 1) * padding) / 2
  for (i in column) {
    y[i] = top
    top += h[i] + padding
  }
}

// Stacks the given links' bands down from the top of a node, storing each band's centre
private fun stackBands(links: List<Int>, top: Float, thickness: List<Float>, centres: FloatArray) {
  var offset = top
  for (l in links) {
    centres[l] = offset + thickness[l] / 2
    offset += thickness[l]
  }
}

// Pushes overlapping nodes apart without changing their order, keeping them inside the height
private fun resolveCollisions(
  column: List<Int>,
  y: FloatArray,
  h: FloatArray,
  padding: Float,
  height: Float,
) {
  var bottom = 0f
  for (i in column) {
    y[i] = max(y[i], bottom)
    bottom = y[i] + h[i] + padding
  }
  var top = height
  for (i in column.asReversed()) {
    y[i] = min(y[i], top - h[i])
    top = y[i] - padding
  }
  column.forEach { y[it] = max(y[it], 0f) }
}
