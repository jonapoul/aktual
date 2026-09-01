package aktual.budget.model

import androidx.compose.runtime.StableMarker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

@OptIn(ExperimentalSerializationApi::class) @MetaSerializable @StableMarker annotation class Id

@JvmInline @Id value class AccountId(val value: String)

@JvmInline @Id value class BankId(val value: String)

@JvmInline @Id value class BudgetId(val value: String)

@JvmInline @Id value class CategoryId(val value: String)

@JvmInline @Id value class CategoryGroupId(val value: String)

@JvmInline @Id value class CleanupGroupId(val value: String)

@JvmInline @Id value class CustomReportId(val value: String)

@JvmInline @Id value class DashboardPageId(val value: String)

@JvmInline @Id value class PayeeId(val value: String)

@JvmInline @Id value class PayeeLocationId(val value: String)

@JvmInline @Id value class RuleId(val value: String)

@JvmInline @Id value class ScheduleId(val value: String)

@JvmInline @Id value class ScheduleNextDateId(val value: String)

@JvmInline @Id value class TagId(val value: String)

@JvmInline @Id value class TransactionId(val value: String)

@JvmInline @Id value class TransactionFilterId(val value: String)

@JvmInline @Id value class WidgetId(val value: String)

@JvmInline @Id value class ZeroBudgetMonthId(val value: String)
