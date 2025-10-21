/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

/**
 * packages/loot-core/src/shared/rules.ts#ALLOCATION_METHODS
 */
enum class AllocationMethod(private val value: String) {
  FixedAmount("fixed-amount"), // a fixed amount
  FixedPercent("fixed-percent"), // a fixed percent of the remainder
  Remainder("remainder"), // an equal portion of the remainder
  ;

  override fun toString(): String = value
}
