package actual.licenses.data

import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject

class LicensesRepository @Inject internal constructor(
  private val assetsProvider: AssetsProvider,
  private val contexts: CoroutineContexts,
) {
  suspend fun loadLicenses(): LicensesLoadState = try {
    val jsonString = withContext(contexts.io) {
      assetsProvider
        .licensesJsonStream()
        .reader()
        .use { it.readText() }
    }

    val dirtyLibraries = withContext(contexts.default) {
      LicensesJson.decodeFromString(
        deserializer = ListSerializer(LibraryModel.serializer()),
        string = jsonString,
      )
    }

    val cleanedLibraries = dirtyLibraries
      .sortedBy { it.project }
      .map { it.cleanedUp() }

    LicensesLoadState.Success(cleanedLibraries)
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    LicensesLoadState.Failure(e.requireMessage())
  }
}
