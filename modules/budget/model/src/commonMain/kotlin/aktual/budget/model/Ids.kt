/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid
import java.io.Serializable as JavaIoSerializable

@JvmInline
value class AccountId(private val value: String) : Comparable<AccountId> {
  override fun toString(): String = value
  override fun compareTo(other: AccountId) = value.compareTo(other.value)
}

@JvmInline
value class BankId(private val value: String) : Comparable<BankId> {
  override fun toString(): String = value
  override fun compareTo(other: BankId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class BudgetId(val value: String) : Comparable<BudgetId>, JavaIoSerializable {
  override fun toString(): String = value
  override fun compareTo(other: BudgetId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CategoryId(private val value: String) : Comparable<CategoryId> {
  override fun toString(): String = value
  override fun compareTo(other: CategoryId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CategoryGroupId(private val value: String) : Comparable<CategoryGroupId> {
  override fun toString(): String = value
  override fun compareTo(other: CategoryGroupId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class CustomReportId(val value: String) : Comparable<CustomReportId> {
  override fun toString(): String = value
  override fun compareTo(other: CustomReportId) = value.compareTo(other.value)
}

@JvmInline
value class PayeeId(private val value: String) : Comparable<PayeeId> {
  override fun toString(): String = value
  override fun compareTo(other: PayeeId) = value.compareTo(other.value)
}

@JvmInline
value class RuleId(private val value: String) : Comparable<RuleId> {
  override fun toString(): String = value
  override fun compareTo(other: RuleId) = value.compareTo(other.value)
}

@JvmInline
value class ScheduleId(private val value: String) : Comparable<ScheduleId> {
  override fun toString(): String = value
  override fun compareTo(other: ScheduleId) = value.compareTo(other.value)
}

@JvmInline
value class ScheduleNextDateId(private val value: String) : Comparable<ScheduleNextDateId> {
  override fun toString(): String = value
  override fun compareTo(other: ScheduleNextDateId) = value.compareTo(other.value)
}

@JvmInline
value class TransactionId(private val value: String) : Comparable<TransactionId> {
  override fun toString(): String = value
  override fun compareTo(other: TransactionId) = value.compareTo(other.value)
}

@JvmInline
value class TransactionFilterId(private val value: String) : Comparable<TransactionFilterId> {
  override fun toString(): String = value
  override fun compareTo(other: TransactionFilterId) = value.compareTo(other.value)
}

@JvmInline
@Serializable
value class WidgetId(private val value: String) : Comparable<WidgetId>, JavaIoSerializable {
  constructor(uuid: Uuid) : this(uuid.toString())
  override fun toString(): String = value
  override fun compareTo(other: WidgetId) = value.compareTo(other.value)
}

@JvmInline
value class ZeroBudgetMonthId(private val value: String) : Comparable<ZeroBudgetMonthId> {
  override fun toString(): String = value
  override fun compareTo(other: ZeroBudgetMonthId) = value.compareTo(other.value)
}
