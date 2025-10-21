/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
