package aktual.budget.db.converters

import aktual.budget.model.AccountId
import aktual.budget.model.BankId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.CustomReportId
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import aktual.budget.model.ScheduleNextDateId
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.TransactionId
import aktual.budget.model.WidgetId
import aktual.budget.model.ZeroBudgetMonthId
import androidx.room3.TypeConverter

internal class AccountIdConverter {
  @TypeConverter fun from(value: String): AccountId = AccountId(value)

  @TypeConverter fun to(value: AccountId): String = value.toString()
}

internal class BankIdConverter {
  @TypeConverter fun from(value: String): BankId = BankId(value)

  @TypeConverter fun to(value: BankId): String = value.toString()
}

internal class CategoryIdConverter {
  @TypeConverter fun from(value: String): CategoryId = CategoryId(value)

  @TypeConverter fun to(value: CategoryId): String = value.toString()
}

internal class CategoryGroupIdConverter {
  @TypeConverter fun from(value: String): CategoryGroupId = CategoryGroupId(value)

  @TypeConverter fun to(value: CategoryGroupId): String = value.toString()
}

internal class CustomReportIdConverter {
  @TypeConverter fun from(value: String): CustomReportId = CustomReportId(value)

  @TypeConverter fun to(value: CustomReportId): String = value.toString()
}

internal class PayeeIdConverter {
  @TypeConverter fun from(value: String): PayeeId = PayeeId(value)

  @TypeConverter fun to(value: PayeeId): String = value.toString()
}

internal class RuleIdConverter {
  @TypeConverter fun from(value: String): RuleId = RuleId(value)

  @TypeConverter fun to(value: RuleId): String = value.toString()
}

internal class ScheduleIdConverter {
  @TypeConverter fun from(value: String): ScheduleId = ScheduleId(value)

  @TypeConverter fun to(value: ScheduleId): String = value.toString()
}

internal class ScheduleJsonPathIndexConverter {
  @TypeConverter fun from(value: String): ScheduleJsonPathIndex = ScheduleJsonPathIndex(value)

  @TypeConverter fun to(value: ScheduleJsonPathIndex): String = value.toString()
}

internal class ScheduleNextDateIdConverter {
  @TypeConverter fun from(value: String): ScheduleNextDateId = ScheduleNextDateId(value)

  @TypeConverter fun to(value: ScheduleNextDateId): String = value.toString()
}

internal class TransactionIdConverter {
  @TypeConverter fun from(value: String): TransactionId = TransactionId(value)

  @TypeConverter fun to(value: TransactionId): String = value.toString()
}

internal class TransactionFilterIdConverter {
  @TypeConverter fun from(value: String): TransactionFilterId = TransactionFilterId(value)

  @TypeConverter fun to(value: TransactionFilterId): String = value.toString()
}

internal class WidgetIdConverter {
  @TypeConverter fun from(value: String): WidgetId = WidgetId(value)

  @TypeConverter fun to(value: WidgetId): String = value.toString()
}

internal class ZeroBudgetMonthIdConverter {
  @TypeConverter fun from(value: String): ZeroBudgetMonthId = ZeroBudgetMonthId(value)

  @TypeConverter fun to(value: ZeroBudgetMonthId): String = value.toString()
}
