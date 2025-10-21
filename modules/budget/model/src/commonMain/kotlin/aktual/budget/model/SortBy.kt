/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class SortBy(private val value: String) {
  Asc(value = "asc"),
  Budget(value = "budget"),
  Desc(value = "desc"),
  Name(value = "name"),
  ;

  override fun toString(): String = value
}
