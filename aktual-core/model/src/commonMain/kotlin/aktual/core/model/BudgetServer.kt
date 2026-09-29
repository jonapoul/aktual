package aktual.core.model

import androidx.compose.runtime.Immutable

/** The server an open budget syncs with. [None] for budgets opened without one, like the demo. */
@Immutable
sealed interface BudgetServer {
  data class Remote(val url: ServerUrl, val token: Token) : BudgetServer

  data object None : BudgetServer
}
