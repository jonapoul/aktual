package aktual.budget.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class AccountId(val value: String) : Comparable<AccountId> {
  override fun toString(): String = value

  override fun compareTo(other: AccountId) = value.compareTo(other.value)
}

@JvmInline
value class BankId(val value: String) : Comparable<BankId> {
  override fun toString(): String = value

  override fun compareTo(other: BankId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class BudgetId(val value: String) : Comparable<BudgetId> {
  override fun toString(): String = value

  override fun compareTo(other: BudgetId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CategoryId(val value: String) : Comparable<CategoryId> {
  override fun toString(): String = value

  override fun compareTo(other: CategoryId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CategoryGroupId(val value: String) : Comparable<CategoryGroupId> {
  override fun toString(): String = value

  override fun compareTo(other: CategoryGroupId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CleanupGroupId(val value: String) : Comparable<CleanupGroupId> {
  override fun toString(): String = value

  override fun compareTo(other: CleanupGroupId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CustomReportId(val value: String) : Comparable<CustomReportId> {
  override fun toString(): String = value

  override fun compareTo(other: CustomReportId) = value.compareTo(other.value)
}

@JvmInline
value class DashboardPageId(val value: String) : Comparable<DashboardPageId> {
  override fun toString(): String = value

  override fun compareTo(other: DashboardPageId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class PayeeId(val value: String) : Comparable<PayeeId> {
  override fun toString(): String = value

  override fun compareTo(other: PayeeId) = value.compareTo(other.value)
}

@JvmInline
value class PayeeLocationId(val value: String) : Comparable<PayeeLocationId> {
  override fun toString(): String = value

  override fun compareTo(other: PayeeLocationId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class RuleId(val value: String) : Comparable<RuleId> {
  override fun toString(): String = value

  override fun compareTo(other: RuleId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class ScheduleId(val value: String) : Comparable<ScheduleId> {
  override fun toString(): String = value

  override fun compareTo(other: ScheduleId) = value.compareTo(other.value)
}

@JvmInline
value class ScheduleNextDateId(val value: String) : Comparable<ScheduleNextDateId> {
  override fun toString(): String = value

  override fun compareTo(other: ScheduleNextDateId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class TagId(val value: String) : Comparable<TagId> {
  override fun toString(): String = value

  override fun compareTo(other: TagId) = value.compareTo(other.value)
}

@JvmInline
value class TransactionId(val value: String) : Comparable<TransactionId> {
  override fun toString(): String = value

  override fun compareTo(other: TransactionId) = value.compareTo(other.value)
}

@JvmInline
value class TransactionFilterId(val value: String) : Comparable<TransactionFilterId> {
  override fun toString(): String = value

  override fun compareTo(other: TransactionFilterId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class WidgetId(val value: String) : Comparable<WidgetId> {
  override fun toString(): String = value

  override fun compareTo(other: WidgetId) = value.compareTo(other.value)
}

@JvmInline
value class ZeroBudgetMonthId(val value: String) : Comparable<ZeroBudgetMonthId> {
  override fun toString(): String = value

  override fun compareTo(other: ZeroBudgetMonthId) = value.compareTo(other.value)
}
