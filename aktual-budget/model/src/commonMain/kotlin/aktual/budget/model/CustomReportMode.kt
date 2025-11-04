/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class CustomReportMode(private val value: String) {
  Total(value = "total"),
  Time(value = "time"),
  ;

  override fun toString(): String = value
}
