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
package aktual.app.nav

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

@Immutable
@Serializable
data object ChangePasswordNavRoute

@Immutable
@Serializable
data object InfoNavRoute

@Immutable
@Serializable
data object LicensesNavRoute

@Immutable
@Serializable
data class ListBudgetsNavRoute(
  val token: LoginToken,
) : JSerializable

@Immutable
@Serializable
data object LoginNavRoute

@Immutable
@Serializable
data object ServerUrlNavRoute

@Immutable
@Serializable
data object SettingsNavRoute

@Immutable
@Serializable
data class SyncBudgetsNavRoute(
  val token: LoginToken,
  val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class TransactionsNavRoute(
  val token: LoginToken,
  val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class ReportsListNavRoute(
  val token: LoginToken,
  val budgetId: BudgetId,
) : JSerializable

@Immutable
@Serializable
data class ReportNavRoute(
  val token: LoginToken,
  val budgetId: BudgetId,
  val widgetId: WidgetId,
) : JSerializable

@Immutable
@Serializable
data class CreateReportNavRoute(
  val token: LoginToken,
  val budgetId: BudgetId,
) : JSerializable
