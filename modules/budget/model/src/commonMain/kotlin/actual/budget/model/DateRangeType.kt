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

enum class DateRangeType(val value: String) {
  ThisWeek(value = "This week"),
  LastWeek(value = "Last week"),
  ThisMonth(value = "This month"),
  LastMonth(value = "Last month"),
  Last3Months(value = "Last 3 months"),
  Last6Months(value = "Last 6 months"),
  Last12Months(value = "Last 12 months"),
  YearToDate(value = "Year to date"),
  LastYear(value = "Last year"),
  AllTime(value = "All time"),
  ;

  override fun toString(): String = value
}
