package actual.about.info.vm

import actual.core.model.ActualVersions
import androidx.compose.runtime.Immutable

@Immutable
data class BuildState(
  val versions: ActualVersions,
  val buildDate: String,
  val year: Int,
)
