package actual.about.licenses.data

sealed interface LicensesLoadState {
  data class Success(val libraries: List<LibraryModel>) : LicensesLoadState
  data class Failure(val cause: String) : LicensesLoadState
}
