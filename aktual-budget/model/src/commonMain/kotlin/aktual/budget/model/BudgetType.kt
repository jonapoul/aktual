/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

/**
 * packages/loot-core/src/server/prefs.ts#BUDGET_TYPES
 */
enum class BudgetType(val value: String) {
  Envelope("envelope"),
  Tracking("tracking"),
  ;

  override fun toString(): String = value

  companion object {
    fun from(value: String?): BudgetType? = entries.firstOrNull { it.value == value }
  }
}
