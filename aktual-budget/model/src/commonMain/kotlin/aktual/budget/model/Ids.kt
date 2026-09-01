package aktual.budget.model

import androidx.compose.runtime.StableMarker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

@OptIn(ExperimentalSerializationApi::class) @MetaSerializable @StableMarker annotation class IdType

@JvmInline @IdType value class AccountId(val value: String)

@JvmInline @IdType value class BankId(val value: String)

@JvmInline @IdType value class BudgetId(val value: String)

@JvmInline @IdType value class CategoryId(val value: String)

@JvmInline @IdType value class CategoryGroupId(val value: String)

@JvmInline @IdType value class CleanupGroupId(val value: String)

@JvmInline @IdType value class CustomReportId(val value: String)

@JvmInline @IdType value class DashboardPageId(val value: String)

@JvmInline @IdType value class PayeeId(val value: String)

@JvmInline @IdType value class PayeeLocationId(val value: String)

@JvmInline @IdType value class RuleId(val value: String)

@JvmInline @IdType value class ScheduleId(val value: String)

@JvmInline @IdType value class ScheduleNextDateId(val value: String)

@JvmInline @IdType value class TagId(val value: String)

@JvmInline @IdType value class TransactionId(val value: String)

@JvmInline @IdType value class TransactionFilterId(val value: String)

@JvmInline @IdType value class WidgetId(val value: String)

@JvmInline @IdType value class ZeroBudgetMonthId(val value: String)
