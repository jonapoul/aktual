@file:Suppress("MagicNumber")

package aktual.budget.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.format.DateTimeFormat

// From getRecurringDescription in packages/loot-core/src/shared/schedules.ts
fun RecurConfig.description(dateFormat: DateTimeFormat<LocalDate>): String {
  val endModeSuffix =
    when (endMode) {
      AfterNOccurrences -> if (endOccurrences == 1) "once" else "$endOccurrences times"
      OnDate -> "until ${endDate?.let(dateFormat::format)}"
      Never,
      Unknown,
      null -> null
    }

  val weekendSolveSuffix =
    when (weekendSolveMode) {
        After -> "(after weekend)"
        Before -> "(before weekend)"
        Unknown,
        null -> ""
      }
      .takeIf { skipWeekend == true }
      .orEmpty()

  val suffix = endModeSuffix?.let { ", $it $weekendSolveSuffix" } ?: weekendSolveSuffix

  return "${frequencyDescription()}$suffix".trim()
}

/** Just the repeating part, e.g. "Every month on the 1st" */
fun RecurConfig.frequencyDescription(): String {
  val dt = interval ?: 1
  return when (frequency) {
    Daily -> {
      if (dt != 1) {
        "Every $dt days"
      } else {
        "Every day"
      }
    }
    Weekly -> {
      if (dt != 1) {
        "Every $dt weeks on ${start.dayOfWeek.nice}"
      } else {
        "Every week on ${start.dayOfWeek.nice}"
      }
    }
    Monthly -> {
      monthlyRecurConfigDesc()
    }
    Yearly -> {
      val dateStr = "${start.month.nice} ${numberSuffix(start.day)}"
      if (dt != 1) "Every $dt years on $dateStr" else "Every year on $dateStr"
    }
    Unknown -> {
      "Unknown frequency"
    }
  }
}

private fun RecurConfig.monthlyRecurConfigDesc(): String {
  val patterns = patterns
  val interval = interval ?: 1
  return if (!patterns.isNullOrEmpty()) {
    // Sort the days ascending. We filter out -1 because that represents "last days" and should
    // always be last, but this sort would put them first
    val sortedPatterns =
      patterns
        .asSequence()
        .sortedWith(RecurPatternComparator)
        .filter { it.value != -1 }
        .plus(patterns.filter { it.value == -1 }) // Add on all -1 values to the end
        .toList()

    val strings = mutableListOf<String>()
    val uniqueDays = sortedPatterns.map { it.type }.distinct()
    val isSameDay = uniqueDays.size == 1 && Day !in uniqueDays
    sortedPatterns.forEach { p ->
      strings +=
        if (p.type == Day) {
          if (p.value == -1) "last day" else numberSuffix(p.value)
        } else if (isSameDay) {
          if (p.value == -1) "last" else numberSuffix(p.value)
        } else {
          if (p.value == -1) {
            "last " + dayName(p.type)
          } else {
            numberSuffix(p.value) + " " + dayName(p.type)
          }
        }
    }

    var range = ""
    if (strings.size > 2) {
      range += strings.slice(0..<strings.size - 1).joinToString(separator = ", ")
      range += ", and "
      range += strings.last()
    } else {
      range += strings.joinToString(separator = " and ")
    }

    if (isSameDay) {
      range += " " + dayName(sortedPatterns[0].type)
    }

    if (interval != 1) {
      "Every $interval months on the $range"
    } else {
      "Every month on the $range"
    }
  } else {
    val day = numberSuffix(start.day)
    if (interval != 1) {
      "Every $interval months on the $day"
    } else {
      "Every month on the $day"
    }
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

private fun numberSuffix(number: Int): String {
  if (number in 10..19) return "${number}th"
  return when (number % 10) {
    1 -> "${number}st"
    2 -> "${number}nd"
    3 -> "${number}rd"
    else -> "${number}th"
  }
}

private fun dayName(type: RecurType): String =
  when (type) {
    Sunday -> "Sunday"
    Monday -> "Monday"
    Tuesday -> "Tuesday"
    Wednesday -> "Wednesday"
    Thursday -> "Thursday"
    Friday -> "Friday"
    Saturday -> "Saturday"
    Day -> error("Should never happen")
    Unknown -> "unknown day"
  }

private val DayOfWeek.nice: String
  get() =
    when (this) {
      SUNDAY -> "Sunday"
      MONDAY -> "Monday"
      TUESDAY -> "Tuesday"
      WEDNESDAY -> "Wednesday"
      THURSDAY -> "Thursday"
      FRIDAY -> "Friday"
      SATURDAY -> "Saturday"
    }

private val Month.nice: String
  get() =
    when (this) {
      JANUARY -> "January"
      FEBRUARY -> "February"
      MARCH -> "March"
      APRIL -> "April"
      MAY -> "May"
      JUNE -> "June"
      JULY -> "July"
      AUGUST -> "August"
      SEPTEMBER -> "September"
      OCTOBER -> "October"
      NOVEMBER -> "November"
      DECEMBER -> "December"
    }
