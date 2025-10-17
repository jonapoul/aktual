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
package actual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.onDay

@Immutable
sealed interface ReportDate {
  val date: LocalDate

  /**
   * Like "2011-10"
   */
  data class Month(val yearMonth: YearMonth) : ReportDate {
    override val date = yearMonth.onDay(1)
    override fun toString() = yearMonth.toString()
  }

  /**
   * Like "2023-11-01"
   */
  data class Date(override val date: LocalDate) : ReportDate {
    override fun toString() = date.toString()
  }

  companion object {
    private const val SEPARATOR = "-"

    @Suppress("MagicNumber")
    fun parse(string: String): ReportDate = when (string.split(SEPARATOR).size) {
      2 -> Month(YearMonth.parse(string))
      3 -> Date(LocalDate.parse(string))
      else -> error("Unexpected ReportDate '$string'")
    }
  }
}
