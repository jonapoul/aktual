package aktual.budget.reports.vm

import aktual.budget.model.Amount
import aktual.budget.reports.vm.SankeyColor.Palette
import aktual.core.model.Percent
import kotlin.math.abs
import kotlin.math.max
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

private const val OTHER_SUFFIX = "__OTHER_BUCKET"
private const val NEGATIVE_SUFFIX = "__NEGATIVE"
private const val ALL_ACCOUNTS = "all_income"
private const val PALETTE_SIZE = 9
private const val HEX_RADIX = 16
private const val COLOR_KEY_CHARS = 3
internal const val DEFAULT_TOP_N_CATEGORIES = 15

// Declared in the order they're drawn from left to right
internal enum class SankeyLayer(val key: String) {
  IncomePayee("payee"),
  IncomeCategory("income_category"),
  Account("account"),
  Budget("budget"),
  CategoryGroup("category_group"),
  Category("category");

  companion object {
    fun parse(key: String?): SankeyLayer? = entries.firstOrNull { it.key == key }
  }
}

internal data class SankeyEntry(
  val categoryGroupId: String,
  val categoryGroup: String,
  val categoryId: String,
  val category: String,
  val isIncome: Boolean,
  // Signed sum of the transactions
  val total: Long,
  val accountId: String,
  val accountName: String,
  val payeeId: String? = null,
  val payeeName: String? = null,
)

internal data class SankeyTransfer(
  val id: String,
  val transferId: String,
  val amount: Long,
  val accountId: String,
  val accountName: String,
)

internal data class SankeyTransferPair(
  val fromAccountId: String,
  val fromAccountName: String,
  val toAccountId: String,
  val toAccountName: String,
  val amount: Long,
)

internal data class SankeyParams(
  val topN: Int = DEFAULT_TOP_N_CATEGORIES,
  val sort: CategorySort = PerGroup,
  val layerFrom: SankeyLayer = IncomePayee,
  val layerTo: SankeyLayer = Category,
  val groupAccounts: Boolean = false,
  val showPercentages: Boolean = false,
)

private data class GraphNode(
  val layer: SankeyLayer,
  val label: SankeyLabel,
  val isNegative: Boolean = false,
  val to: LinkedHashMap<String, Long> = LinkedHashMap(),
)

private typealias Graph = LinkedHashMap<String, GraphNode>

// Names and values of the nodes merged into each link, keyed by the link's source and target
private typealias GroupedItems = HashMap<Pair<String, String>, LinkedHashMap<String, Long>>

// packages/desktop-client/src/components/reports/spreadsheets/sankey-spreadsheet.ts
// createTransactionsGraph() and buildSankeyData()
// categoryOrder lists category group and category IDs in budget order, for CategorySort.BudgetOrder
internal fun calculateSankey(
  title: String?,
  start: YearMonth,
  end: YearMonth,
  entries: List<SankeyEntry>,
  transfers: List<SankeyTransferPair>,
  categoryOrder: List<String>,
  params: SankeyParams,
): SankeyData {
  val graph = transactionsGraph(entries, transfers, params.groupAccounts)
  val grouped = groupOtherNodes(graph, params.topN, global = params.sort == Global)
  val sorted = sortGraph(graph, params.sort, categoryOrder)
  filterLayers(sorted, params.layerFrom, params.layerTo)
  cleanUpNodes(sorted)
  return toSankeyData(title, start, end, sorted, grouped, params.showPercentages)
}

// Merges each pair of transfer transactions into one net flow between two accounts
internal fun aggregateTransferPairs(transfers: List<SankeyTransfer>): List<SankeyTransferPair> {
  val byId = transfers.associateBy { it.id }
  val names = transfers.associate { it.accountId to it.accountName }
  val pairs = LinkedHashMap<Pair<String, String>, Long>()

  for (from in transfers) {
    val to = byId[from.transferId]
    if (to == null || from.accountId > to.accountId) continue
    val key = from.accountId to to.accountId
    val source = if (from.amount < 0) from.accountId else to.accountId
    val sign = if (source == key.first) 1 else -1
    pairs[key] = (pairs[key] ?: 0L) + sign * abs(from.amount)
  }

  return pairs
    .filterValues { it != 0L }
    .map { (key, value) ->
      val (fromId, toId) = if (value > 0) key else key.second to key.first
      SankeyTransferPair(
        fromAccountId = fromId,
        fromAccountName = names[fromId].orEmpty(),
        toAccountId = toId,
        toAccountName = names[toId].orEmpty(),
        amount = abs(value),
      )
    }
}

private fun transactionsGraph(
  entries: List<SankeyEntry>,
  transfers: List<SankeyTransferPair>,
  groupAccounts: Boolean,
): Graph {
  val graph = Graph()

  fun addAccount(id: String, name: String) {
    val accountId = if (groupAccounts) ALL_ACCOUNTS else id
    val label = if (groupAccounts) SankeyLabel.Income else SankeyLabel.Text(name)
    graph.addNode(accountId, GraphNode(Account, label))
  }

  fun accountKey(id: String) = if (groupAccounts) ALL_ACCOUNTS else id

  for (entry in entries) {
    val value = abs(entry.total)
    val isNegative = entry.total < 0
    val account = accountKey(entry.accountId)
    val negativeKey = entry.categoryId + NEGATIVE_SUFFIX
    val categoryLabel = SankeyLabel.Text(entry.category)

    when {
      entry.isIncome && isNegative -> {
        // Account > Income category
        addAccount(entry.accountId, entry.accountName)
        graph.addNode(negativeKey, GraphNode(CategoryGroup, categoryLabel, true))
        graph.addLink(account, negativeKey, value)
      }

      entry.isIncome -> {
        // Payee > Income category > Account
        graph.addNode(entry.categoryId, GraphNode(IncomeCategory, categoryLabel))
        addAccount(entry.accountId, entry.accountName)
        graph.addLink(entry.categoryId, account, value)
        if (entry.payeeId != null) {
          val payeeLabel = SankeyLabel.Text(entry.payeeName.orEmpty())
          graph.addNode(entry.payeeId, GraphNode(IncomePayee, payeeLabel))
          graph.addLink(entry.payeeId, entry.categoryId, value)
        }
      }

      isNegative -> {
        // Account > Category group > Category
        addAccount(entry.accountId, entry.accountName)
        val groupLabel = SankeyLabel.Text(entry.categoryGroup)
        graph.addNode(entry.categoryGroupId, GraphNode(CategoryGroup, groupLabel))
        graph.addNode(entry.categoryId, GraphNode(Category, categoryLabel))
        graph.addLink(account, entry.categoryGroupId, value)
        graph.addLink(entry.categoryGroupId, entry.categoryId, value)
      }

      else -> {
        // Refunded expense category > Account
        graph.addNode(negativeKey, GraphNode(IncomeCategory, categoryLabel))
        addAccount(entry.accountId, entry.accountName)
        graph.addLink(negativeKey, account, value)
      }
    }
  }

  if (!groupAccounts) {
    for (pair in transfers) {
      addAccount(pair.fromAccountId, pair.fromAccountName)
      addAccount(pair.toAccountId, pair.toAccountName)
      graph.addLink(pair.fromAccountId, pair.toAccountId, pair.amount)
    }
  }

  return graph
}

private fun Graph.addNode(key: String, node: GraphNode) {
  if (key !in this) put(key, node)
}

private fun Graph.addLink(from: String, to: String, value: Long) {
  val node = get(from) ?: return
  node.to[to] = (node.to[to] ?: 0L) + value
}

private fun Graph.nodeValue(key: String): Long {
  val incoming = values.sumOf { it.to[key] ?: 0L }
  val outgoing = get(key)?.to?.values?.sum() ?: 0L
  return max(incoming, outgoing)
}

private fun String.isOther() = endsWith(OTHER_SUFFIX)

// Keeps at most topN nodes in each layer, moving the smallest into an "Other" node
private fun groupOtherNodes(graph: Graph, topN: Int, global: Boolean): GroupedItems {
  val grouped = GroupedItems()
  for (layer in SankeyLayer.entries) {
    val smallestFirst =
      graph
        .filter { (key, node) ->
          node.layer == layer && !key.isOther() && key != ALL_ACCOUNTS && !node.isNegative
        }
        .keys
        .sortedBy { graph.nodeValue(it) }
        .toMutableList()
    fun others() = graph.count { (key, node) -> node.layer == layer && key.isOther() }
    while (smallestFirst.isNotEmpty() && smallestFirst.size + others() > topN) {
      moveToOther(graph, smallestFirst.removeAt(0), global, grouped)
    }
  }
  return grouped
}

private fun moveToOther(graph: Graph, key: String, global: Boolean, grouped: GroupedItems) {
  val node = graph[key] ?: return
  val name = (node.label as? SankeyLabel.Text)?.value ?: key
  fun addGrouped(from: String, to: String, value: Long) {
    val items = grouped.getOrPut(from to to) { LinkedHashMap() }
    items[name] = (items[name] ?: 0L) + value
  }

  val parentKey =
    when {
      global -> null
      node.layer == Category ->
        graph.entries.firstOrNull { (_, n) -> n.layer == CategoryGroup && key in n.to }?.key
      node.layer == IncomePayee -> node.to.keys.firstOrNull { graph[it]?.layer == IncomeCategory }
      else -> null
    }
  val otherKey = (parentKey ?: node.layer.key) + OTHER_SUFFIX
  graph.addNode(otherKey, GraphNode(node.layer, Other))

  for ((fromKey, from) in graph.entries.toList()) {
    val value = from.to.remove(key) ?: continue
    if (fromKey != otherKey) {
      graph.addLink(fromKey, otherKey, value)
      addGrouped(fromKey, otherKey, value)
    }
  }
  for ((toKey, value) in node.to) {
    if (toKey != otherKey) {
      graph.addLink(otherKey, toKey, value)
      addGrouped(otherKey, toKey, value)
    }
  }
  graph.remove(key)
}

private fun sortGraph(graph: Graph, sort: CategorySort, categoryOrder: List<String>): Graph {
  val values = graph.keys.associateWith { graph.nodeValue(it) }
  val byValue = graph.keys.sortedByDescending { values.getValue(it) }

  val order: MutableList<String> =
    when (sort) {
      Global -> {
        (byValue.filterNot { it.isOther() } + byValue.filter { it.isOther() }).toMutableList()
      }

      BudgetOrder -> {
        val used = LinkedHashSet<String>()
        for (id in categoryOrder) {
          if (id in graph) used.add(id)
          val other = id + OTHER_SUFFIX
          if (other in graph) used.add(other)
        }
        (used + graph.keys.filterNot { it in used }).toMutableList()
      }

      PerGroup,
      Unknown -> {
        byValue.toMutableList().apply {
          placeAround(graph, values, CategoryGroup, Category, after = true)
          placeAround(graph, values, IncomeCategory, IncomePayee, after = false)
        }
      }
    }

  // Negative nodes always go at the start of their layer
  graph.filterValues { it.isNegative }.keys.forEach { key -> order.moveToStart(key) }
  return order.associateWithTo(Graph()) { graph.getValue(it) }
}

// Moves each anchor's related nodes next to it, biggest first, with any "Other" node last
private fun MutableList<String>.placeAround(
  graph: Graph,
  values: Map<String, Long>,
  anchorLayer: SankeyLayer,
  relatedLayer: SankeyLayer,
  after: Boolean,
) {
  val anchors = filter { graph[it]?.layer == anchorLayer }
  for (anchor in anchors) {
    val otherKey = anchor + OTHER_SUFFIX
    val related =
      if (after) {
        graph.getValue(anchor).to.keys.filter { graph[it]?.layer == relatedLayer }
      } else {
        graph.filter { (_, n) -> n.layer == relatedLayer && anchor in n.to }.keys
      }
    val ordered =
      related.filterNot { it == otherKey }.sortedByDescending { values.getValue(it) } +
        listOfNotNull(otherKey.takeIf { graph[it]?.layer == relatedLayer })
    removeAll(ordered.toSet())
    val index = indexOf(anchor)
    addAll(if (after) index + 1 else index, ordered)
  }
}

private fun MutableList<String>.moveToStart(key: String) {
  if (remove(key)) add(0, key)
}

private fun filterLayers(graph: Graph, from: SankeyLayer, to: SankeyLayer) {
  graph.values.removeAll { it.layer !in from..to }
  graph.values.forEach { node -> node.to.keys.retainAll(graph.keys) }
}

private fun cleanUpNodes(graph: Graph) {
  graph.values.forEach { node -> node.to.values.removeAll { it == 0L } }
  val hasIncoming = graph.values.flatMapTo(HashSet()) { it.to.keys }
  graph.entries.removeAll { (key, node) -> node.to.isEmpty() && key !in hasIncoming }
}

// Each layer takes one column, except accounts which spread over more when there are transfers
// between them
private fun columns(graph: Graph): Map<String, Int> {
  val accountDepths = accountDepths(graph)
  val columns = HashMap<String, Int>()
  var offset = 0
  for (layer in SankeyLayer.entries) {
    val keys = graph.filterValues { it.layer == layer }.keys
    if (keys.isEmpty()) continue
    keys.forEach { key -> columns[key] = offset + (accountDepths[key] ?: 0) }
    offset += keys.maxOf { accountDepths[it] ?: 0 } + 1
  }
  return columns
}

// Longest chain of transfers leading into each account
private fun accountDepths(graph: Graph): Map<String, Int> {
  val depths = HashMap<String, Int>()
  val visiting = HashSet<String>()

  fun depth(key: String): Int {
    depths[key]?.let {
      return it
    }
    if (!visiting.add(key)) return 0
    val parents = graph.filter { (_, n) -> n.layer == Account && key in n.to }.keys
    val result = parents.maxOfOrNull { depth(it) + 1 } ?: 0
    visiting.remove(key)
    depths[key] = result
    return result
  }

  graph.filterValues { it.layer == Account }.keys.forEach(::depth)
  return depths
}

private fun toSankeyData(
  title: String?,
  start: YearMonth,
  end: YearMonth,
  graph: Graph,
  grouped: GroupedItems,
  showPercentages: Boolean,
): SankeyData {
  val columns = columns(graph)
  val values = graph.keys.associateWith { graph.nodeValue(it) }
  val columnTotals =
    graph.keys
      .groupingBy { columns.getValue(it) }
      .fold(0L) { total, key ->
        total + values.getValue(key)
      }
  val indices = graph.keys.withIndex().associate { (i, key) -> key to i }
  val colors = graph.mapValues { (key, node) -> nodeColor(key, node) }

  val nodes = graph.map { (key, node) ->
    SankeyNode(
      key = key,
      label = node.label,
      column = columns.getValue(key),
      value = Amount(values.getValue(key)),
      percent = Percent(values.getValue(key), columnTotals.getValue(columns.getValue(key))),
      color = colors.getValue(key),
    )
  }

  val links = graph.flatMap { (sourceKey, source) ->
    source.to.map { (targetKey, value) ->
      val colorKey =
        when {
          source.isNegative -> sourceKey
          targetKey.endsWith(NEGATIVE_SUFFIX) -> targetKey
          source.layer in SOURCE_COLORED -> sourceKey
          else -> targetKey
        }
      SankeyLink(
        source = indices.getValue(sourceKey),
        target = indices.getValue(targetKey),
        value = Amount(value),
        color = colors.getValue(colorKey),
        grouped = grouped.itemsFor(sourceKey, targetKey),
      )
    }
  }

  return SankeyData(
    title = title,
    start = start,
    end = end,
    showPercentages = showPercentages,
    nodes = nodes.toImmutableList(),
    links = links.toImmutableList(),
  )
}

private fun GroupedItems.itemsFor(
  source: String,
  target: String,
): ImmutableList<SankeyGroupedItem> {
  val items = get(source to target) ?: return persistentListOf()
  return items.entries
    .sortedByDescending { it.value }
    .map { (name, value) -> SankeyGroupedItem(name, Amount(value)) }
    .toImmutableList()
}

private val SOURCE_COLORED =
  setOf(
    SankeyLayer.IncomePayee,
    SankeyLayer.IncomeCategory,
    SankeyLayer.Account,
    SankeyLayer.CategoryGroup,
  )

private fun nodeColor(key: String, node: GraphNode): SankeyColor =
  when {
    node.isNegative -> Negative
    key == ALL_ACCOUNTS -> Primary
    else -> Palette(paletteIndex(key))
  }

// Uses the first few hex chars of the key (usually a UUID) as a stable random value
private fun paletteIndex(key: String): Int {
  val hex = key.filter { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }.take(COLOR_KEY_CHARS)
  return hex.ifEmpty { "0" }.toInt(HEX_RADIX) % PALETTE_SIZE
}
