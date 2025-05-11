package actual.budget.sync.vm

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
    data object NotLoggedIn : Failure {
      override val total = Bytes.Zero
    }

    data class InProgress(
      val cause: String,
      val read: Bytes,
      override val total: Bytes,
    ) : Failure

    data class IO(
      val cause: String,
      override val total: Bytes,
    ) : Failure

    data class Http(
      val code: Int,
      val message: String,
      override val total: Bytes,
    ) : Failure

    data class Other(
      val message: String,
      override val total: Bytes,
    ) : Failure
  }
}
