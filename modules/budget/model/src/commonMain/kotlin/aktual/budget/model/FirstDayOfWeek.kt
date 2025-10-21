/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class FirstDayOfWeek(val value: Int) {
  Sunday(value = 0),
  Monday(value = 1),
  Tuesday(value = 2),
  Wednesday(value = 3),
  Thursday(value = 4),
  Friday(value = 5),
  Saturday(value = 6),
  ;

  companion object {
    fun from(value: String?): FirstDayOfWeek? {
      val int = value?.toInt() ?: return null
      return entries.firstOrNull { it.value == int }
    }
  }
}
