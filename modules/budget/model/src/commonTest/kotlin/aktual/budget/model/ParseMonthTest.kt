/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test

@Burst
class ParseMonthTest(
  private val case: TestCase = burstValues(
    TestCase(year = 2025, month = Month.JULY, expected = "2025-07"),
    TestCase(year = 1, month = Month.JANUARY, expected = "0001-01"),
    TestCase(year = 9999, month = Month.DECEMBER, expected = "9999-12"),
  ),
) {
  data class TestCase(val year: Int, val month: Month, val expected: String)

  @Test
  fun `Parse and stringify`() {
    val date = ReportDate.Month(YearMonth(case.year, case.month))
    assertThat(date.toString()).isEqualTo(case.expected)
    assertThat(ReportDate.parse(case.expected)).isEqualTo(date)
  }
}
