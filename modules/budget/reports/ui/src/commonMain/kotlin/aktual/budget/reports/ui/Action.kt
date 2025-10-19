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
package aktual.budget.reports.ui

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.TextData
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data class OpenItem(val id: WidgetId) : Action
  data class Rename(val id: WidgetId) : Action
  data class Delete(val id: WidgetId) : Action
  data class SetSummaryType(val type: SummaryChartType) : Action
  data class SetAllTimeDivisor(val allTime: Boolean) : Action
  data class ClickCalendarDay(val day: CalendarDay) : Action
  data class SaveTextContent(val data: TextData, val newContent: String) : Action
  data object CreateNewReport : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
