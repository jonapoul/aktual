package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payees")
data class PayeeEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("category") val category: String?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
  @ColumnInfo("transfer_acct") val transferAccount: String?,
  @ColumnInfo("favorite") val isFavorite: Boolean? = false,
  @ColumnInfo("learn_categories") val learnCategories: Boolean? = true,
)
