package actual.budget.sync.vm

import okio.Buffer
import okio.Path

sealed interface DecryptResult {
  sealed interface Success : DecryptResult
  data class DecryptedBuffer(val buffer: Buffer) : Success
  data class DecryptedFile(val path: Path) : Success
  data class NotNeeded(val path: Path) : Success

  sealed interface Failure : DecryptResult
  data object MissingKey : Failure
  data class UnknownAlgorithm(val algorithm: String) : Failure
  data class OtherFailure(val message: String) : Failure
}
