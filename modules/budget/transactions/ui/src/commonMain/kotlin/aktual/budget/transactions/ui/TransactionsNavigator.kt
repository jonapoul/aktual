/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.ui

import androidx.compose.runtime.Immutable

@Immutable
interface TransactionsNavigator {
  fun back(): Boolean
}
