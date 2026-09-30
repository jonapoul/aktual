package aktual.core.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BudgetServer {
  data class Remote(val url: ServerUrl, val token: Token) : BudgetServer

  data object None : BudgetServer
}
