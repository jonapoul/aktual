package aktual.about.data

import aktual.core.Assets
import aktual.core.runCatchingCancellable
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer

@Inject
class LicensesRepository
internal constructor(private val assets: Assets, private val contexts: CoroutineContexts) {
  suspend fun loadLicenses(): LicensesLoadState =
    runCatchingCancellable(
      attempt = {
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

        val sorted = (dirtyLibraries + NonLibraryArtifacts).sortedBy { it.id }
        LicensesLoadState.Success(sorted)
      },
      onFailure = { e -> LicensesLoadState.Failure(e.requireMessage()) },
    )

  private companion object {
    const val LICENSES_FILENAME = "licensee.json"
  }
}
