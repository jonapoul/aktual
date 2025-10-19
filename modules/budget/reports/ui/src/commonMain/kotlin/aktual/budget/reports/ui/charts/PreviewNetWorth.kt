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
import aktual.budget.reports.vm.NetWorthData
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Month

internal object PreviewNetWorth {
  val DATA = NetWorthData(
    title = "My Net Worth",
    items = persistentMapOf(
      date(2023, Month.JANUARY) to Amount(-44_000.00),
      date(2024, Month.JANUARY) to Amount(-18_000.00),
      date(2024, Month.FEBRUARY) to Amount(21_000.34),
      date(2024, Month.MARCH) to Amount(25_000.67),
      date(2024, Month.JULY) to Amount(-10123.98),
      date(2024, Month.AUGUST) to Amount(-5123.69),
      date(2024, Month.DECEMBER) to Amount(-6789.12),
      date(2025, Month.JANUARY) to Amount(198.0),
      date(2025, Month.FEBRUARY) to Amount(1.98),
      date(2025, Month.MARCH) to Amount(256.0),
      date(2025, Month.JUNE) to Amount(30000.0),
    ),
  )
}
