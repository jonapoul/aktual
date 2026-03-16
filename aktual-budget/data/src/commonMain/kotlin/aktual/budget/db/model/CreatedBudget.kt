package aktual.budget.db.model

import aktual.budget.db.converters.YearMonthFromStringConverter
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.TypeConverters
import kotlinx.datetime.YearMonth

@Entity(tableName = "created_budgets")
@TypeConverters(YearMonthFromStringConverter::class)
data class CreatedBudget(@PrimaryKey @ColumnInfo(name = "month") val month: YearMonth)
