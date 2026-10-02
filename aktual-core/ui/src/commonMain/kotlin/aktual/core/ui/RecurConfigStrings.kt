@file:Suppress("MagicNumber")

package aktual.core.ui

import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurPattern
import aktual.budget.model.RecurType
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat

// From getRecurringDescription in packages/desktop-client/src/util/schedule.ts
@Composable
fun RecurConfig.description(dateFormat: DateTimeFormat<LocalDate>): String {
  val endModeSuffix =
    when (endMode) {
      AfterNOccurrences -> {
        val count = endOccurrences ?: 1
        if (count == 1) Strings.recurEndOnce else Plurals.recurEndTimes(count, count)
      }
      OnDate -> endDate?.let { Strings.recurEndUntil(dateFormat.format(it)) }
      Never,
      Unknown,
      null -> null
    }

  val frequency = frequencyDescription()
  val withEnd = endModeSuffix?.let { Strings.recurWithEnd(frequency, it) } ?: frequency

  if (skipWeekend != true) return withEnd
  return when (weekendSolveMode) {
    After -> Strings.recurWeekendAfter(withEnd)
    Before -> Strings.recurWeekendBefore(withEnd)
    Unknown,
    null -> withEnd
  }
}

/** Just the repeating part, e.g. "Every month on the 1st" */
@Composable
fun RecurConfig.frequencyDescription(): String {
  val dt = interval ?: 1
  return when (frequency) {
    Daily -> if (dt != 1) Plurals.recurEveryNDays(dt, dt) else Strings.recurEveryDay
    Weekly -> {
      val day = start.dayOfWeek.stringLong()
      if (dt != 1) Plurals.recurEveryNWeeks(dt, dt, day) else Strings.recurEveryWeek(day)
    }
    Monthly -> {
      val range = monthlyRange()
      if (dt != 1) Plurals.recurEveryNMonths(dt, dt, range) else Strings.recurEveryMonth(range)
    }
    Yearly -> {
      val date = Strings.recurMonthDay(start.month.stringLong(), ordinal(start.day))
      if (dt != 1) Plurals.recurEveryNYears(dt, dt, date) else Strings.recurEveryYear(date)
    }
    Unknown -> Strings.recurUnknownFrequency
  }
}

@Composable
private fun RecurConfig.monthlyRange(): String {
  val patterns = patterns
  if (patterns.isNullOrEmpty()) return ordinal(start.day)

  // Sort the days ascending. We filter out -1 because that represents "last days" and should
  // always be last, but this sort would put them first
  val sortedPatterns =
    patterns
      .asSequence()
      .sortedWith(RecurPatternComparator)
      .filter { it.value != -1 }
      .plus(patterns.filter { it.value == -1 }) // Add on all -1 values to the end
      .toList()

  val uniqueDays = sortedPatterns.map { it.type }.distinct()
  val isSameDay = uniqueDays.size == 1 && Day !in uniqueDays
  val strings = sortedPatterns.map { p ->
    when {
      p.type == Day -> if (p.value == -1) Strings.recurLastDay else ordinal(p.value)
      isSameDay -> if (p.value == -1) Strings.recurLast else ordinal(p.value)
      else -> {
        val nth = if (p.value == -1) Strings.recurLast else ordinal(p.value)
        Strings.recurWeekday(nth, dayName(p.type))
      }
    }
  }

  val range = joinList(strings)
  return if (isSameDay) Strings.recurWeekday(range, dayName(sortedPatterns[0].type)) else range
}

@Composable
private fun joinList(strings: List<String>): String =
  when (strings.size) {
    0 -> ""
    1 -> strings[0]
    2 -> Strings.recurListTwo(strings[0], strings[1])
    else -> {
      var joined = strings[0]
      for (i in 1 until strings.lastIndex) joined = Strings.recurListMiddle(joined, strings[i])
      Strings.recurListEnd(joined, strings.last())
    }
  }

private object RecurPatternComparator : Comparator<RecurPattern> {
  private val RecurType.sortValue
    get() = if (this == Day) 1 else 0

  override fun compare(p1: RecurPattern, p2: RecurPattern): Int {
    val typeOrder = p1.type.sortValue - p2.type.sortValue
    val valueOrder = p1.value - p2.value
    return if (typeOrder == 0) valueOrder else typeOrder
  }
}

@Composable
private fun ordinal(number: Int): String {
  if (number in 10..19) return Strings.recurOrdinalTh(number)
  return when (number % 10) {
    1 -> Strings.recurOrdinalSt(number)
    2 -> Strings.recurOrdinalNd(number)
    3 -> Strings.recurOrdinalRd(number)
    else -> Strings.recurOrdinalTh(number)
  }
}

@Composable
private fun dayName(type: RecurType): String =
  when (type) {
    Sunday -> Strings.weekSunday
    Monday -> Strings.weekMonday
    Tuesday -> Strings.weekTuesday
    Wednesday -> Strings.weekWednesday
    Thursday -> Strings.weekThursday
    Friday -> Strings.weekFriday
    Saturday -> Strings.weekSaturday
    Day -> error("Should never happen")
    Unknown -> Strings.recurUnknownDay
  }
