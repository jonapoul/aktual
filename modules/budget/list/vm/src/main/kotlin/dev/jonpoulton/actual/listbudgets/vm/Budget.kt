package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface Budget {
  val id: String
  val name: String
}

@Immutable
data class LocalBudget(
  override val id: String,
  override val name: String,
) : Budget

@Immutable
data class RemoteBudget(
  override val id: String,
  override val name: String,
  val groupId: String,
  val cloudFileId: String,
) : Budget

