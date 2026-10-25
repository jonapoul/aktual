package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@Immutable
value class AccountId(val value: String) : Comparable<AccountId> {
  override fun compareTo(other: AccountId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class AccountGroupId(val value: String) : Comparable<AccountGroupId> {
  override fun compareTo(other: AccountGroupId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class BankId(val value: String) : Comparable<BankId> {
  override fun compareTo(other: BankId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class BudgetId(val value: String) : Comparable<BudgetId> {
  override fun compareTo(other: BudgetId): Int = value.compareTo(other.value)

  override fun toString(): String = value

  companion object {
    val Demo = BudgetId("_demo-budget")
  }
}

@JvmInline
@Serializable
@Immutable
value class CategoryId(val value: String) : Comparable<CategoryId> {
  override fun compareTo(other: CategoryId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class CategoryGroupId(val value: String) : Comparable<CategoryGroupId> {
  override fun compareTo(other: CategoryGroupId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class CleanupGroupId(val value: String) : Comparable<CleanupGroupId> {
  override fun compareTo(other: CleanupGroupId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class CustomReportId(val value: String) : Comparable<CustomReportId> {
  override fun compareTo(other: CustomReportId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class DashboardPageId(val value: String) : Comparable<DashboardPageId> {
  override fun compareTo(other: DashboardPageId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class PayeeId(val value: String) : Comparable<PayeeId> {
  override fun compareTo(other: PayeeId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class PayeeLocationId(val value: String) : Comparable<PayeeLocationId> {
  override fun compareTo(other: PayeeLocationId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class RuleId(val value: String) : Comparable<RuleId> {
  override fun compareTo(other: RuleId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class ScheduleId(val value: String) : Comparable<ScheduleId> {
  override fun compareTo(other: ScheduleId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class ScheduleNextDateId(val value: String) : Comparable<ScheduleNextDateId> {
  override fun compareTo(other: ScheduleNextDateId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class TagId(val value: String) : Comparable<TagId> {
  override fun compareTo(other: TagId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class TransactionId(val value: String) : Comparable<TransactionId> {
  override fun compareTo(other: TransactionId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class TransactionFilterId(val value: String) : Comparable<TransactionFilterId> {
  override fun compareTo(other: TransactionFilterId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class WidgetId(val value: String) : Comparable<WidgetId> {
  override fun compareTo(other: WidgetId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}

@JvmInline
@Serializable
@Immutable
value class ZeroBudgetMonthId(val value: String) : Comparable<ZeroBudgetMonthId> {
  override fun compareTo(other: ZeroBudgetMonthId): Int = value.compareTo(other.value)

  override fun toString(): String = value
}
