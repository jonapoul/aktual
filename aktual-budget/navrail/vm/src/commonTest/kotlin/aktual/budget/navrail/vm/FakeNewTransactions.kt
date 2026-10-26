package aktual.budget.navrail.vm

import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeNewTransactions : NewTransactions {
  override val counts = MutableStateFlow<Map<AccountId, Int>>(emptyMap())
  val seen = mutableListOf<AccountId>()

  override suspend fun imported(account: AccountId, ids: List<TransactionId>) = error("Not used")

  override suspend fun markSeen(account: AccountId) {
    seen += account
    counts.update { it - account }
  }
}
