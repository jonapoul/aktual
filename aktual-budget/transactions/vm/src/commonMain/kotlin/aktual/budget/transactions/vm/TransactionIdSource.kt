package aktual.budget.transactions.vm

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

@Stable
interface TransactionIdSource {
  val pagingData: Flow<PagingData<TransactionId>>
}
