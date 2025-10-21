/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress("SpellCheckingInspection", "FunctionName")

package aktual.budget.model

@JvmInline
value class AccountSyncSource private constructor(val value: String) {
  override fun toString(): String = value

  companion object {
    val SimpleFin = AccountSyncSource(value = "simpleFin")
    val GoCardless = AccountSyncSource(value = "goCardless")
    val PluggyAi = AccountSyncSource(value = "pluggyai")
    fun Other(value: String) = AccountSyncSource(value)

    fun fromString(string: String): AccountSyncSource = when (string) {
      SimpleFin.value -> SimpleFin
      GoCardless.value -> GoCardless
      else -> Other(string)
    }
  }
}
