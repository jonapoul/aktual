package actual.about.info.vm

import androidx.compose.runtime.Immutable

@Immutable
data class BuildState(
  val buildVersion: String,
  val buildDate: String,
  val sourceCodeRepo: String,
  val year: Int,
)
