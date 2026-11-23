package aktual.budget.utils

/** Uploads the open budget to the server, so it syncs from then on. */
fun interface BudgetUploader {
  suspend fun upload(): UploadResult
}

sealed interface UploadResult {
  data object Success : UploadResult

  sealed interface Failure : UploadResult

  // The budget was opened without a server
  data object NoServer : Failure

  // Uploading encrypted budgets isn't supported yet
  data object Encrypted : Failure

  @JvmInline value class Rejected(val reason: String) : Failure

  @JvmInline value class Unreachable(val message: String?) : Failure
}
