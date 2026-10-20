package aktual.budget.reports.vm

import aktual.budget.db.CustomReportAccounts
import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportCategories
import aktual.budget.db.CustomReportCategoryGroups
import aktual.budget.db.CustomReportPayees
import aktual.budget.db.CustomReportTags
import aktual.budget.model.Amount
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.SortBy
import aktual.budget.model.tagNamesInNotes
import kotlin.math.abs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/** The dates [CustomReportAmounts] is queried for, and how its dates are cut down to intervals */
internal data class CustomReportQuery(val start: LocalDate, val end: LocalDate, val divisor: Long)

// packages/desktop-client/src/components/reports/spreadsheets/makeQuery.ts compares dates by the
// interval they fall in, so monthly and yearly reports cover the whole of their first and last
// month or year
internal fun customReportQuery(interval: Interval, range: LocalDateRange): CustomReportQuery =
  when (interval) {
    Monthly ->
      CustomReportQuery(
        range.start.yearMonth.firstDay,
        range.endInclusive.yearMonth.lastDay,
        MONTH_DIVISOR,
      )
    Yearly ->
      CustomReportQuery(
        LocalDate(range.start.year, JANUARY, 1),
        LocalDate(range.endInclusive.year, DECEMBER, LAST_DAY_OF_DECEMBER),
        YEAR_DIVISOR,
      )
    Daily,
    Weekly,
    Unknown -> CustomReportQuery(range.start, range.endInclusive, DAY_DIVISOR)
  }

internal data class CustomReportInputs(
  val amounts: List<CustomReportAmounts>,
  val categories: List<CustomReportCategories>,
  val categoryGroups: List<CustomReportCategoryGroups>,
  val payees: List<CustomReportPayees>,
  val accounts: List<CustomReportAccounts>,
  // Only for reports split by tag, which sum these in place of [amounts]
  val tagged: List<TaggedAmounts> = emptyList(),
  val tags: List<CustomReportTags> = emptyList(),
)

/** A transaction's amounts, with the notes its tags are read from */
internal data class TaggedAmounts(val amounts: CustomReportAmounts, val notes: String?)

internal data class CustomSpreadsheet(
  val intervals: ImmutableList<CustomIntervalTotals>,
  val intervalCount: Int,
  val groups: ImmutableList<CustomGroupTotals>,
  val categoryGroups: ImmutableList<CustomGroupTotals>,
  val totals: CustomTotals,
  val trends: ImmutableMap<CustomGroup, CustomTrend>,
)

// packages/desktop-client/src/components/reports/spreadsheets/custom-spreadsheet.ts
// createCustomSpreadsheet() and grouped-spreadsheet.ts createGroupedSpreadsheet(). Upstream filters
// every row for every group and interval, this sums each row into its group and interval once.
internal fun customSpreadsheet(
  config: CustomReportConfig,
  range: LocalDateRange,
  firstDayOfWeek: FirstDayOfWeek,
  inputs: CustomReportInputs,
): CustomSpreadsheet {
  val intervals = intervals(config.interval, range, firstDayOfWeek)
  val indexOf = intervalIndexer(config, intervals, firstDayOfWeek)
  val amounts = if (config.groupBy == Tag) inputs.tagged.map { it.amounts } else inputs.amounts
  val sums = sumAmounts(intervals.size, indexOf, amounts)
  val metric = config.balanceType.metric
  val calculator = GroupCalculator(intervals, metric, config.showEmpty)

  val categoryList =
    inputs.categories.map { CustomGroup.Category(it.id, it.name.orEmpty()) } +
      listOf<CustomGroup>(Uncategorized, OffBudget, Transfers)
  val groupList =
    inputs.categoryGroups.map { CustomGroup.Group(it.id, it.name.orEmpty()) } +
      CustomGroup.UncategorizedAndOffBudget

  val (items, itemSums) =
    when (config.groupBy) {
      Category,
      GroupBy.Interval -> categoryList to sums.byCategory
      Group,
      CategoryGroup -> groupList to sums.byGroup
      Payee -> inputs.payees.map { CustomGroup.Payee(it.id, it.name.orEmpty()) } to sums.byPayee
      Account ->
        inputs.accounts.map { CustomGroup.Account(it.id, it.name.orEmpty()) } to sums.byAccount
      Tag ->
        sumTags(
          intervals.size,
          indexOf,
          inputs.tagged,
          config.tagScope.resolve(inputs.tags),
          config.showEmpty,
        )
      Unknown -> emptyList<CustomGroup>() to sums.byCategory
    }
  if (items.isEmpty()) return EMPTY

  val (intervalTotals, totals) = calculator.intervalTotals(items, itemSums)
  val groups = items.map { calculator.group(it, itemSums[it.key]) }.filter(calculator::isShown)
  val trim =
    calculator.trimRange(
      config.trimIntervals,
      groups.map { g -> g.intervals.map { it.totals } } + listOf(intervalTotals.map { it.totals }),
    )
  val comparator = comparator(metric, config.sortBy)
  val trimmedIntervals = intervalTotals.slice(trim)
  val sortedGroups = groups.map { it.trimmed(trim) }.sortedWith(comparator)

  return CustomSpreadsheet(
    intervals = trimmedIntervals.toImmutableList(),
    intervalCount = intervals.size,
    groups = sortedGroups.toImmutableList(),
    categoryGroups =
      categoryGroups(
          calculator,
          groupList,
          categoryList,
          inputs.categories,
          sums,
          config.trimIntervals,
        )
        .sortedWith(comparator)
        .map { it.copy(categories = it.categories.sortedWith(comparator).toImmutableList()) }
        .toImmutableList(),
    totals = totals,
    trends =
      if (config.showTrendLines) {
        trendLines(trimmedIntervals, sortedGroups.map { it.group })
      } else {
        persistentMapOf()
      },
  )
}

private val EMPTY =
  CustomSpreadsheet(
    persistentListOf(),
    0,
    persistentListOf(),
    persistentListOf(),
    Zero,
    persistentMapOf(),
  )

// packages/desktop-client/src/components/reports/graphs/util/computeTrendLines.ts: a least squares
// fit through each group's stacked amounts, with the interval index as x
internal fun trendLines(
  intervals: List<CustomIntervalTotals>,
  groups: List<CustomGroup>,
): ImmutableMap<CustomGroup, CustomTrend> {
  val n = intervals.size
  if (n < 2) return persistentMapOf()
  val sumX = n * (n - 1) / 2.0
  val sumXSquared = (n - 1) * n * (2 * n - 1) / 6.0
  val denominator = n * sumXSquared - sumX * sumX

  return groups
    .associateWith { group ->
      var sumY = 0.0
      var sumXY = 0.0
      intervals.forEachIndexed { x, interval ->
        val y = interval.stacked[group]?.toDouble() ?: 0.0
        sumY += y
        sumXY += x * y
      }
      val slope = (n * sumXY - sumX * sumY) / denominator
      val intercept = (sumY - slope * sumX) / n
      CustomTrend(start = Amount(intercept), end = Amount(intercept + slope * (n - 1)))
    }
    .toImmutableMap()
}

// grouped-spreadsheet.ts: each category group, holding its categories
private fun categoryGroups(
  calculator: GroupCalculator,
  groupList: List<CustomGroup>,
  categoryList: List<CustomGroup>,
  categories: List<CustomReportCategories>,
  sums: Sums,
  trimIntervals: Boolean,
): List<CustomGroupTotals> {
  val categoriesByGroup = categories.groupBy { it.cat_group }
  val namedCategories = categoryList.filterIsInstance<CustomGroup.Category>().associateBy { it.id }

  val groups =
    groupList
      .map { group ->
        val children =
          if (group is Group) {
            categoriesByGroup[group.id].orEmpty().mapNotNull { namedCategories[it.id] }
          } else {
            listOf<CustomGroup>(Uncategorized, Transfers, OffBudget)
          }
        calculator
          .group(group, sums.byGroup[group.key])
          .copy(
            categories =
              children
                .map { calculator.group(it, sums.byCategory[it.key]) }
                .filter(calculator::isShown)
                .toImmutableList(),
          )
      }
      .filter(calculator::isShown)

  // Upstream checks the first group's intervals a second time, in place of the report's totals
  val series = groups.flatMap { group ->
    (listOf(group) + group.categories).map { g -> g.intervals.map { it.totals } }
  }
  val trim = calculator.trimRange(trimIntervals, series)
  return groups.map { group ->
    group
      .trimmed(trim)
      .copy(categories = group.categories.map { it.trimmed(trim) }.toImmutableList())
  }
}

private class GroupCalculator(
  private val intervals: List<CustomInterval>,
  private val metric: CustomMetric,
  private val showEmpty: Boolean,
) {
  // custom-spreadsheet.ts, the intervalData reducer
  fun intervalTotals(
    items: List<CustomGroup>,
    sums: Map<Any, IntervalSums>,
  ): Pair<List<CustomIntervalTotals>, CustomTotals> {
    var totalAssets = 0L
    var totalDebts = 0L
    var netAssets = 0L
    var netDebts = 0L

    val result = intervals.mapIndexed { index, interval ->
      var intervalAssets = 0L
      var intervalDebts = 0L
      val stacked = LinkedHashMap<CustomGroup, Amount>()
      for (item in items) {
        val itemSums = sums[item.key]
        val assets = itemSums?.assets?.get(index) ?: 0L
        val debts = itemSums?.debts?.get(index) ?: 0L
        intervalAssets += assets
        intervalDebts += debts
        stacked[item] = Amount(stackAmount(assets, debts))
      }
      val intervalTotal = intervalAssets + intervalDebts
      totalAssets += intervalAssets
      totalDebts += intervalDebts
      netAssets += intervalTotal.coerceAtLeast(0)
      netDebts += intervalTotal.coerceAtMost(0)
      CustomIntervalTotals(
        interval = interval,
        totals = totals(intervalAssets, intervalDebts),
        stacked = stacked.toImmutableMap(),
      )
    }

    val totals =
      CustomTotals(
        assets = Amount(totalAssets),
        debts = Amount(totalDebts),
        netAssets = Amount(netAssets),
        netDebts = Amount(netDebts),
        total = Amount(totalAssets + totalDebts),
      )
    return result to totals
  }

  private fun stackAmount(assets: Long, debts: Long): Long {
    val net = assets + debts
    return when (metric) {
      Assets -> assets
      Debts -> abs(debts)
      NetAssets -> net.coerceAtLeast(0)
      NetDebts -> if (net < 0) abs(net) else 0
      Total -> net
    }
  }

  // packages/desktop-client/src/components/reports/spreadsheets/recalculate.ts recalculate()
  fun group(group: CustomGroup, sums: IntervalSums?): CustomGroupTotals {
    var previous: Long? = null
    val groupIntervals = intervals.mapIndexed { index, interval ->
      val assets = sums?.assets?.get(index) ?: 0L
      val debts = sums?.debts?.get(index) ?: 0L
      val total = assets + debts
      val change = previous?.let { total - it } ?: 0L
      previous = total
      CustomGroupIntervalTotals(interval, totals(assets, debts), Amount(change))
    }
    return CustomGroupTotals(
      group = group,
      totals = totals(sums?.assets?.sum() ?: 0L, sums?.debts?.sum() ?: 0L),
      intervals = groupIntervals.toImmutableList(),
      categories = persistentListOf(),
    )
  }

  // packages/desktop-client/src/components/reports/spreadsheets/filterEmptyRows.ts
  fun isShown(group: CustomGroupTotals): Boolean {
    if (showEmpty) return true
    val totals = group.totals
    return if (metric == Total) {
      totals.debts != Zero || totals.assets != Zero || totals.total != Zero
    } else {
      totals[metric] != Zero
    }
  }

  // packages/desktop-client/src/components/reports/spreadsheets/trimIntervals.ts
  // determineIntervalRange(): from the first interval with anything in it to the last, across all
  // the given series
  fun trimRange(trimIntervals: Boolean, series: List<List<CustomTotals>>): IntRange {
    if (!trimIntervals) return intervals.indices
    var start = intervals.size
    var end = -1
    for (items in series) {
      val first = items.indexOfFirst { it[metric] != Zero }
      if (first != -1) {
        start = minOf(start, first)
        end = maxOf(end, items.indexOfLast { it[metric] != Zero })
      }
    }
    return if (start > end) IntRange.EMPTY else start..end
  }
}

private fun totals(assets: Long, debts: Long): CustomTotals {
  val total = assets + debts
  return CustomTotals(
    assets = Amount(assets),
    debts = Amount(debts),
    netAssets = Amount(total.coerceAtLeast(0)),
    netDebts = Amount(total.coerceAtMost(0)),
    total = Amount(total),
  )
}

private fun CustomGroupTotals.trimmed(range: IntRange): CustomGroupTotals =
  if (range == intervals.indices) {
    this
  } else {
    copy(intervals = intervals.slice(range).toImmutableList())
  }

// packages/desktop-client/src/components/reports/spreadsheets/sortData.ts sortData(). Upstream
// sorts by translated names, which for the made-up groups we only have in English here
private fun comparator(metric: CustomMetric, sortBy: SortBy): Comparator<CustomGroupTotals> {
  val reverse = metric == Debts || metric == NetDebts
  val effective =
    when {
      reverse && sortBy == Asc -> Desc
      reverse && sortBy == Desc -> Asc
      else -> sortBy
    }
  return when (effective) {
    Asc -> compareBy { it.totals[metric] }
    Desc -> compareByDescending { it.totals[metric] }
    Name -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.group.sortName }
    Budget,
    Unknown -> Comparator { _, _ -> 0 }
  }
}

private val CustomGroup.sortName: String
  get() =
    when (this) {
      is Account -> name
      is Category -> name
      is Group -> name
      is Payee -> name
      OffBudget -> "Off budget"
      Transfers -> "Transfers"
      Uncategorized -> "Uncategorized"
      UncategorizedAndOffBudget -> "Uncategorized & Off budget"
      is TagSet -> names.joinToString(" + ") { "#$it" }
      Untagged -> "Untagged"
    }

// What a group's amounts are summed under: its ID, or the made-up group itself
private val CustomGroup.key: Any
  get() =
    when (this) {
      is Account -> id
      is Category -> id
      is Group -> id
      is Payee -> id
      OffBudget,
      Transfers,
      Uncategorized,
      UncategorizedAndOffBudget,
      is TagSet,
      Untagged -> this
    }

private class IntervalSums(size: Int) {
  val assets = LongArray(size)
  val debts = LongArray(size)
}

private class Sums(private val size: Int) {
  val byCategory = HashMap<Any, IntervalSums>()
  val byGroup = HashMap<Any, IntervalSums>()
  val byPayee = HashMap<Any, IntervalSums>()
  val byAccount = HashMap<Any, IntervalSums>()

  fun add(map: HashMap<Any, IntervalSums>, key: Any, index: Int, row: CustomReportAmounts) {
    val sums = map.getOrPut(key) { IntervalSums(size) }
    sums.assets[index] += row.assets
    sums.debts[index] += row.debts
  }
}

// The interval a row's amounts fall in, or null for rows the report doesn't show
private fun intervalIndexer(
  config: CustomReportConfig,
  intervals: List<CustomInterval>,
  firstDayOfWeek: FirstDayOfWeek,
): (CustomReportAmounts) -> Int? {
  val indices = intervals.withIndex().associate { (index, interval) -> interval.start to index }
  return { row ->
    val key = row.interval_key?.takeIf { config.isShown(row) }
    key?.let { indices[intervalStart(config.interval, it, firstDayOfWeek)] }
  }
}

private fun sumAmounts(
  size: Int,
  indexOf: (CustomReportAmounts) -> Int?,
  rows: List<CustomReportAmounts>,
): Sums {
  val sums = Sums(size)
  for (row in rows) {
    val index = indexOf(row) ?: continue

    // filterHiddenItems.ts: categorised on-budget amounts go to their category, the rest to one of
    // the made-up categories
    val offBudget = row.off_budget == true
    val category = row.category
    if (category != null && !offBudget) {
      sums.add(sums.byCategory, category, index, row)
      row.category_group?.let { sums.add(sums.byGroup, it, index, row) }
    } else {
      val made =
        when {
          offBudget -> CustomGroup.OffBudget
          row.transfer_account != null -> CustomGroup.Transfers
          else -> CustomGroup.Uncategorized
        }
      sums.add(sums.byCategory, made, index, row)
      sums.add(sums.byGroup, CustomGroup.UncategorizedAndOffBudget, index, row)
    }
    row.payee?.let { sums.add(sums.byPayee, it, index, row) }
    sums.add(sums.byAccount, row.account, index, row)
  }
  return sums
}

// packages/desktop-client/src/components/reports/spreadsheets/tagGroups.ts groupQueryDataByTags():
// each row goes to the bucket of exactly the scope's tags in its notes. Showing empty rows lists
// every single-tag bucket and the untagged one even without amounts
private fun sumTags(
  size: Int,
  indexOf: (CustomReportAmounts) -> Int?,
  rows: List<TaggedAmounts>,
  scope: List<CustomReportTags>,
  showEmpty: Boolean,
): Pair<List<CustomGroup>, Map<Any, IntervalSums>> {
  val tagsByName = scope.associateBy { it.tag }
  val buckets = LinkedHashSet<CustomGroup>()
  if (showEmpty) {
    scope.forEach { buckets += tagBucket(listOf(it)) }
    buckets += CustomGroup.Untagged
  }

  val sums = Sums(size)
  val byBucket = HashMap<Any, IntervalSums>()
  for (row in rows) {
    val index = indexOf(row.amounts) ?: continue
    val bucket = tagBucket(tagNamesInNotes(row.notes.orEmpty()).mapNotNull(tagsByName::get))
    buckets += bucket
    sums.add(byBucket, bucket, index, row.amounts)
  }
  return buckets.toList() to byBucket
}

private fun tagBucket(tags: List<CustomReportTags>): CustomGroup {
  if (tags.isEmpty()) return Untagged
  val sorted =
    tags.sortedWith(
      compareBy<CustomReportTags, String>(TagNameOrder) { it.tag }.thenBy { it.id.value },
    )
  return CustomGroup.TagSet(
    ids = sorted.map { it.id }.toImmutableList(),
    names = sorted.map { it.tag }.toImmutableList(),
  )
}

// tagScope.ts resolveTagScope(): the visible tags in scope, a selection keeping its own order
internal fun CustomTagScope.resolve(tags: List<CustomReportTags>): List<CustomReportTags> {
  val visible = tags.filter { it.hidden != true }
  return when (this) {
    All -> visible
    is Selected -> visible.associateBy { it.id }.let { byId -> tagIds.mapNotNull(byId::get) }
  }
}

// Intl.Collator('en', { numeric: true }): case-insensitive, with runs of digits compared as
// numbers.
// Names equal that way fall back to an exact comparison
private object TagNameOrder : Comparator<String> {
  private val chunks = Regex("""\d+|\D+""")

  override fun compare(a: String, b: String): Int {
    val left = chunks.findAll(a).map { it.value }.toList()
    val right = chunks.findAll(b).map { it.value }.toList()
    for (i in 0 until minOf(left.size, right.size)) {
      val x = left[i]
      val y = right[i]
      val result =
        if (x.first().isDigit() && y.first().isDigit()) {
          x.trimStart('0').length.compareTo(y.trimStart('0').length).takeIf { it != 0 }
            ?: x.trimStart('0').compareTo(y.trimStart('0'))
        } else {
          x.compareTo(y, ignoreCase = true)
        }
      if (result != 0) return result
    }
    return left.size.compareTo(right.size).takeIf { it != 0 } ?: a.compareTo(b)
  }
}

// packages/desktop-client/src/components/reports/spreadsheets/filterHiddenItems.ts
// filterReportTransactions()
private fun CustomReportConfig.isShown(row: CustomReportAmounts): Boolean {
  val hidden = row.category_hidden == true || row.category_group_hidden == true
  val offBudget = row.off_budget == true
  return (showHidden || !hidden) &&
    (showOffBudget || !offBudget) &&
    (showUncategorized || row.category != null || offBudget)
}

private fun intervalStart(
  interval: Interval,
  key: Long,
  firstDayOfWeek: FirstDayOfWeek,
): LocalDate =
  when (interval) {
    Monthly -> LocalDate((key / MONTH_DIVISOR).toInt(), (key % MONTH_DIVISOR).toInt(), 1)
    Yearly -> LocalDate(key.toInt(), 1, 1)
    Daily,
    Unknown -> dateOf(key)
    Weekly -> weekStart(dateOf(key), firstDayOfWeek)
  }

private fun dateOf(key: Long) =
  LocalDate(
    (key / YEAR_DIVISOR).toInt(),
    (key / MONTH_DIVISOR % MONTH_DIVISOR).toInt(),
    (key % MONTH_DIVISOR).toInt(),
  )

// packages/loot-core/src/shared/months.ts rangeInclusive() and its day, week and year versions:
// every interval from the one holding the start date to the one holding the end
internal fun intervals(
  interval: Interval,
  range: LocalDateRange,
  firstDayOfWeek: FirstDayOfWeek,
): List<CustomInterval> {
  fun startOf(date: LocalDate): LocalDate =
    when (interval) {
      Daily,
      Unknown -> date
      Weekly -> weekStart(date, firstDayOfWeek)
      Monthly -> date.yearMonth.firstDay
      Yearly -> LocalDate(date.year, JANUARY, 1)
    }

  fun next(start: LocalDate): LocalDate =
    when (interval) {
      Daily,
      Unknown -> start.plus(1, DAY)
      Weekly -> start.plus(1, WEEK)
      Monthly -> start.plus(1, MONTH)
      Yearly -> start.plus(1, YEAR)
    }

  val starts = mutableListOf<LocalDate>()
  var start = startOf(range.start)
  val last = startOf(range.endInclusive)
  while (start < last) {
    starts += start
    start = next(start)
  }
  starts += start

  return starts.mapIndexed { index, intervalStart ->
    val from = if (index == 0) range.start else intervalStart
    val to = if (index == starts.lastIndex) range.endInclusive else starts[index + 1].minus(1, DAY)
    CustomInterval(intervalStart, from..to)
  }
}

internal fun weekStart(date: LocalDate, firstDayOfWeek: FirstDayOfWeek): LocalDate {
  val daysIntoWeek =
    (date.dayOfWeek.isoDayNumber % DAYS_PER_WEEK - firstDayOfWeek.value + DAYS_PER_WEEK) %
      DAYS_PER_WEEK
  return date.minus(daysIntoWeek, DAY)
}

private const val DAY_DIVISOR = 1L
private const val MONTH_DIVISOR = 100L
private const val YEAR_DIVISOR = 10_000L
internal const val LAST_DAY_OF_DECEMBER = 31
internal const val DAYS_PER_WEEK = 7
