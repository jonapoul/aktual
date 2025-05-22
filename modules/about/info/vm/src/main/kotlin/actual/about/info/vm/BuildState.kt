package actual.about.info.vm

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
@Poko
class BuildState(
  val buildVersion: String,
  val buildDate: String,
  val sourceCodeRepo: String,
  val year: Int,
)
