/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class Interval(private val value: String) {
  Daily("Daily"),
  Weekly("Weekly"),
  Monthly("Monthly"),
  Yearly("Yearly"),
  ;

  override fun toString(): String = value
}
