package actual.budget.sync.vm

import actual.core.model.Bytes
import dev.drewhamilton.poko.Poko
import okio.Path

sealed interface DownloadState {
  val total: Bytes

  @Poko class InProgress(
    val read: Bytes,
    override val total: Bytes,
  ) : DownloadState

  @Poko class Done(
    override val total: Bytes,
    val path: Path,
  ) : DownloadState

  sealed interface Failure : DownloadState {
    val message: String

    data object NotLoggedIn : Failure {
      override val total = Bytes.Zero
      override val message = "Not logged in"
    }

    @Poko class IO(
      override val message: String,
      override val total: Bytes,
    ) : Failure

    @Poko class Http(
      val code: Int,
      override val message: String,
      override val total: Bytes,
    ) : Failure

    @Poko class Other(
      override val message: String,
      override val total: Bytes,
    ) : Failure
  }
}
