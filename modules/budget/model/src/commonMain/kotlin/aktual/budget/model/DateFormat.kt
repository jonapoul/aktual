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

import androidx.compose.runtime.Immutable

@Immutable
data class DateFormat(
  val value: String,
  val label: String,
) {
  companion object : Set<DateFormat> by Entries {
    val Default = Entries.first()

    fun from(value: String?): DateFormat? = Entries.firstOrNull { it.value == value }
  }
}

private val Entries = setOf(
  DateFormat(value = "MM/dd/yyyy", label = "MM/DD/YYYY"),
  DateFormat(value = "dd/MM/yyyy", label = "MM/DD/YYYY"),
  DateFormat(value = "yyyy-MM-dd", label = "YYYY-MM-DD"),
  DateFormat(value = "MM.dd.yyyy", label = "MM.DD.YYYY"),
  DateFormat(value = "dd.MM.yyyy", label = "DD.MM.YYYY"),
)
