package aktual.about.data

import aktual.core.model.Assets
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer

@Inject
class LicensesRepository
internal constructor(private val assets: Assets, private val contexts: CoroutineContexts) {
  suspend fun loadLicenses(): LicensesLoadState =
    try {
      val jsonString =
        withContext(contexts.io) {
          assets.getStream(LICENSES_FILENAME).reader().use { it.readText() }
        }

      val dirtyLibraries =
        withContext(contexts.default) {
          LicensesJson.decodeFromString(
            deserializer = ListSerializer(ArtifactDetail.serializer()),
            string = jsonString,
          )
        }

      val sorted = dirtyLibraries.sortedBy { it.id }
      LicensesLoadState.Success(sorted)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      LicensesLoadState.Failure(e.requireMessage())
    }

  private companion object {
    const val LICENSES_FILENAME = "licensee.json"
  }
}
