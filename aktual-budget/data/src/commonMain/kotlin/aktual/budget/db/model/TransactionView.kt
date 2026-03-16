package aktual.budget.db.model

import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate

data class TransactionView(
  @ColumnInfo(name = "id") val id: TransactionId,
  @ColumnInfo(name = "date") val date: LocalDate?,
  @ColumnInfo(name = "accountName") val accountName: String?,
  @ColumnInfo(name = "payeeName") val payeeName: String?,
  @ColumnInfo(name = "notes") val notes: String?,
  @ColumnInfo(name = "categoryName") val categoryName: String?,
  @ColumnInfo(name = "amount") val amount: Amount?,
)
