package actual.android.app.nav

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
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
