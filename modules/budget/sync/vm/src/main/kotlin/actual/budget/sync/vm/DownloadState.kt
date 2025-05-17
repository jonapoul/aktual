package actual.budget.sync.vm

import actual.core.model.Bytes
import okio.Path

sealed interface DownloadState {
  val total: Bytes

  data class InProgress(
    val read: Bytes,
    override val total: Bytes,
  ) : DownloadState

  data class Done(
    override val total: Bytes,
    val path: Path,
  ) : DownloadState

  sealed interface Failure : DownloadState {
    val message: String

    data object NotLoggedIn : Failure {
      override val total = Bytes.Zero
      override val message = "Not logged in"
    }

    data class IO(
      override val message: String,
      override val total: Bytes,
    ) : Failure

    data class Http(
      val code: Int,
      override val message: String,
      override val total: Bytes,
    ) : Failure

    data class Other(
      override val message: String,
      override val total: Bytes,
    ) : Failure
  }
}
