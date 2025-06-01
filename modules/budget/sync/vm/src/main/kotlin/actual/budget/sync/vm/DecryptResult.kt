package actual.budget.sync.vm

import okio.Path

sealed interface DecryptResult {
  sealed interface Success : DecryptResult {
    val path: Path
  }

  data class Decrypted(override val path: Path) : Success
  data class NotNeeded(override val path: Path) : Success

  sealed interface Failure : DecryptResult
  data object MissingKey : Failure
  data class UnknownAlgorithm(val algorithm: String) : Failure
  data class OtherFailure(val message: String) : Failure
}
