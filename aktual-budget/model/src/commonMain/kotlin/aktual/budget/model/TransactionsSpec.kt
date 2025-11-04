/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSpec(
  val accountSpec: AccountSpec,
)

@Immutable
sealed interface AccountSpec {
  data object AllAccounts : AccountSpec
  data class SpecificAccount(val id: AccountId) : AccountSpec
}
