@file:Suppress("JUnitMalformedDeclaration")

package aktual.budget.rules.ui.list

import aktual.budget.model.DateFormat.YyyyMmDd
import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurEndMode.AfterNOccurrences
import aktual.budget.model.RecurEndMode.Never
import aktual.budget.model.RecurEndMode.OnDate
import aktual.budget.model.RecurFrequency.Monthly
import aktual.budget.model.RecurFrequency.Weekly
import aktual.budget.model.RecurFrequency.Yearly
import aktual.budget.model.RecurPattern
import aktual.budget.model.RecurType.Day
import aktual.budget.model.RecurType.Friday
import aktual.budget.model.RecurType.Saturday
import aktual.budget.model.WeekendSolveMode.Before
import aktual.core.ui.formatter
import app.cash.burst.Burst
import app.cash.burst.burstValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.Month.SEPTEMBER

// From packages/loot-core/src/shared/schedules.test.ts
@Burst
class RecurConfigStringTest {
  data class TestCase(val expected: String, val config: RecurConfig)

  @Test
  fun `Encode recur config to string`(
    test: TestCase =
      burstValues(
        WEEKLY,
        WEEKLY_INTERVAL,
        MONTHLY,
        MONTHLY_INTERVAL,
        MONTHLY_PATTERN,
        MONTHLY_PATTERN_INTERVAL,
        MONTHLY_LAST_DAY_OF_MONTH,
        MONTHLY_LAST_DAY,
        MONTHLY_DAY_NAME,
        MONTHLY_LAST_DAY_NAME,
        MONTHLY_MULTIPLE_DAYS,
        MONTHLY_MULTIPLE_DAYS_WITH_LAST,
        MONTHLY_MIXED_DAYS_AND_DAY_NAMES,
        MONTHLY_MIXED_DAY_NAMES_FIRST,
        MONTHLY_EXPLICIT_PATTERN,
        YEARLY,
        YEARLY_INTERVAL,
        WEEKLY_OCCURRENCES,
        WEEKLY_ONCE,
        MONTHLY_OCCURRENCES,
        YEARLY_OCCURRENCES,
        WEEKLY_END_DATE,
        MONTHLY_END_DATE,
      )
  ) {
    assertEquals(expected = test.expected, actual = test.config.string(YyyyMmDd.formatter()))
  }

  companion object {
    // Weekly interval
    val WEEKLY =
      TestCase(
        expected = "Every week on Monday",
        config = RecurConfig(frequency = Weekly, start = LocalDate(2021, MAY, 17)),
      )

    val WEEKLY_INTERVAL =
      TestCase(
        expected = "Every 2 weeks on Monday",
        config = RecurConfig(frequency = Weekly, start = LocalDate(2021, MAY, 17), interval = 2),
      )

    // Monthly interval
    val MONTHLY =
      TestCase(
        expected = "Every month on the 25th",
        config = RecurConfig(frequency = Monthly, start = LocalDate(2021, APRIL, 25)),
      )

    val MONTHLY_INTERVAL =
      TestCase(
        expected = "Every 2 months on the 25th",
        config = RecurConfig(frequency = Monthly, start = LocalDate(2021, APRIL, 25), interval = 2),
      )

    val MONTHLY_PATTERN =
      TestCase(
        expected = "Every month on the 25th",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns = listOf(RecurPattern(value = 25, type = Day)),
          ),
      )

    val MONTHLY_PATTERN_INTERVAL =
      TestCase(
        expected = "Every 2 months on the 25th",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            interval = 2,
            patterns = listOf(RecurPattern(value = 25, type = Day)),
          ),
      )

    val MONTHLY_LAST_DAY_OF_MONTH =
      TestCase(
        expected = "Every month on the 31st",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns = listOf(RecurPattern(value = 31, type = Day)),
          ),
      )

    val MONTHLY_LAST_DAY =
      TestCase(
        expected = "Every month on the last day",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns = listOf(RecurPattern(value = -1, type = Day)),
          ),
      )

    val MONTHLY_DAY_NAME =
      TestCase(
        expected = "Every month on the 2nd Friday",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns = listOf(RecurPattern(value = 2, type = Friday)),
          ),
      )

    val MONTHLY_LAST_DAY_NAME =
      TestCase(
        expected = "Every month on the last Friday",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns = listOf(RecurPattern(value = -1, type = Friday)),
          ),
      )

    // Monthly with multiple days - note order doesn't matter, days are sorted
    val MONTHLY_MULTIPLE_DAYS =
      TestCase(
        expected = "Every month on the 3rd, 15th, and 20th",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns =
              listOf(
                RecurPattern(value = 15, type = Day),
                RecurPattern(value = 3, type = Day),
                RecurPattern(value = 20, type = Day),
              ),
          ),
      )

    val MONTHLY_MULTIPLE_DAYS_WITH_LAST =
      TestCase(
        expected = "Every month on the 3rd, 20th, and last day",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns =
              listOf(
                RecurPattern(value = 3, type = Day),
                RecurPattern(value = -1, type = Day),
                RecurPattern(value = 20, type = Day),
              ),
          ),
      )

    // Mix days and day names
    val MONTHLY_MIXED_DAYS_AND_DAY_NAMES =
      TestCase(
        expected = "Every month on the 2nd Friday, 3rd, and last day",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns =
              listOf(
                RecurPattern(value = 3, type = Day),
                RecurPattern(value = -1, type = Day),
                RecurPattern(value = 2, type = Friday),
              ),
          ),
      )

    // When there is a mixture of types, day names should always come first
    val MONTHLY_MIXED_DAY_NAMES_FIRST =
      TestCase(
        expected = "Every month on the 1st Saturday, 3rd Friday, 2nd, and 10th",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, APRIL, 25),
            patterns =
              listOf(
                RecurPattern(value = 1, type = Saturday),
                RecurPattern(value = 2, type = Day),
                RecurPattern(value = 3, type = Friday),
                RecurPattern(value = 10, type = Day),
              ),
          ),
      )

    // Original TEST_1 - monthly with full explicit config
    val MONTHLY_EXPLICIT_PATTERN =
      TestCase(
        expected = "Every month on the 13th",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2023, SEPTEMBER, 13),
            interval = 1,
            patterns = listOf(RecurPattern(value = 13, type = Day)),
            skipWeekend = false,
            endMode = Never,
            endOccurrences = 1,
            endDate = LocalDate(2025, MAY, 26),
            weekendSolveMode = Before,
          ),
      )

    // Yearly interval
    val YEARLY =
      TestCase(
        expected = "Every year on May 17th",
        config = RecurConfig(frequency = Yearly, start = LocalDate(2021, MAY, 17)),
      )

    val YEARLY_INTERVAL =
      TestCase(
        expected = "Every 2 years on May 17th",
        config = RecurConfig(frequency = Yearly, start = LocalDate(2021, MAY, 17), interval = 2),
      )

    // Limited occurrences
    val WEEKLY_OCCURRENCES =
      TestCase(
        expected = "Every 2 weeks on Monday, 2 times",
        config =
          RecurConfig(
            frequency = Weekly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = AfterNOccurrences,
            endOccurrences = 2,
          ),
      )

    val WEEKLY_ONCE =
      TestCase(
        expected = "Every 2 weeks on Monday, once",
        config =
          RecurConfig(
            frequency = Weekly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = AfterNOccurrences,
            endOccurrences = 1,
          ),
      )

    val MONTHLY_OCCURRENCES =
      TestCase(
        expected = "Every 2 months on the 17th, 2 times",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = AfterNOccurrences,
            endOccurrences = 2,
          ),
      )

    val YEARLY_OCCURRENCES =
      TestCase(
        expected = "Every 2 years on May 17th, 2 times",
        config =
          RecurConfig(
            frequency = Yearly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = AfterNOccurrences,
            endOccurrences = 2,
          ),
      )

    // End date
    val WEEKLY_END_DATE =
      TestCase(
        expected = "Every 2 weeks on Monday, until 2021-06-01",
        config =
          RecurConfig(
            frequency = Weekly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = OnDate,
            endDate = LocalDate(2021, JUNE, 1),
          ),
      )

    val MONTHLY_END_DATE =
      TestCase(
        expected = "Every 2 months on the 17th, until 2021-06-01",
        config =
          RecurConfig(
            frequency = Monthly,
            start = LocalDate(2021, MAY, 17),
            interval = 2,
            endMode = OnDate,
            endDate = LocalDate(2021, JUNE, 1),
          ),
      )
  }
}
