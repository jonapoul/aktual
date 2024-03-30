package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable

@Immutable
data class Budget(
  val name: String,
  val state: BudgetState,
  val hasKey: Boolean,
  val encryptKeyId: String?,
  val groupId: String,
  val cloudFileId: String,
)
