package aktual.budget.db.converters

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.ReportDate
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.Timestamp
import androidx.room3.TypeConverter
import kotlin.uuid.Uuid

internal class AmountConverter {
  @TypeConverter fun from(value: Long): Amount = Amount(value)

  @TypeConverter fun to(value: Amount): Long = value.toLong()
}

internal class UuidConverter {
  @TypeConverter fun from(value: String): Uuid = Uuid.parse(value)

  @TypeConverter fun to(value: Uuid): String = value.toString()
}

internal class AccountSyncSourceConverter {
  @TypeConverter fun from(value: String): AccountSyncSource = AccountSyncSource.fromString(value)

  @TypeConverter fun to(value: AccountSyncSource): String = value.value
}

internal class SyncedPrefKeyConverter {
  @TypeConverter fun from(value: String): SyncedPrefKey = SyncedPrefKey.decode(value)

  @TypeConverter fun to(value: SyncedPrefKey): String = value.key
}

internal class TimestampConverter {
  @TypeConverter fun from(value: String): Timestamp = Timestamp.parse(value)

  @TypeConverter fun to(value: Timestamp): String = value.toString()
}

internal class ReportDateConverter {
  @TypeConverter fun from(value: String): ReportDate = ReportDate.parse(value)

  @TypeConverter fun to(value: ReportDate): String = value.toString()
}
