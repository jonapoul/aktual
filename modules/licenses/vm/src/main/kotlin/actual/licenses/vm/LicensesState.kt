package actual.licenses.vm

import actual.licenses.data.LibraryModel
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface LicensesState {
  data object Loading : LicensesState
  data class Loaded(val libraries: ImmutableList<LibraryModel>) : LicensesState
  data object NoneFound : LicensesState
  data class Error(val errorMessage: String) : LicensesState
}

internal fun LicensesState.Loaded.filteredBy(text: String): LicensesState.Loaded {
  val filtered = libraries.filter { lib -> lib.matches(text) }
  return LicensesState.Loaded(libraries = filtered.toImmutableList())
}

private fun LibraryModel.matches(text: String): Boolean =
  project.contains(text, ignoreCase = true) ||
    (description?.contains(text, ignoreCase = true) ?: false) ||
    version.contains(text, ignoreCase = true) ||
    dependency.contains(text, ignoreCase = true) ||
    (url?.contains(text, ignoreCase = true) ?: false) ||
    (year?.toString()?.contains(text, ignoreCase = true) ?: false) ||
    developers.any { developer -> developer.contains(text, ignoreCase = true) } ||
    licenses.any { model -> model.license.contains(text, ignoreCase = true) }
