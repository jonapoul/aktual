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
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal object PreviewShared {
  const val WIDTH = 800
  const val HEIGHT = 200

  val AMOUNT = Amount(12345.67)

  val START_DATE = LocalDate(2024, Month.JANUARY, 1)

  val END_DATE = LocalDate(2025, Month.JULY, 6)
}
