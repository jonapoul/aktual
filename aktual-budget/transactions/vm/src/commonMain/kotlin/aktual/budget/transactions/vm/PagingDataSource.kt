package aktual.budget.transactions.vm

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

@Immutable
interface PagingDataSource {
  val pagingData: Flow<PagingData<TransactionId>>
}
