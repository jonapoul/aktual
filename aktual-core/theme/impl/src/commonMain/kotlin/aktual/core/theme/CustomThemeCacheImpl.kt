package aktual.core.theme

import aktual.core.model.AppDirectory
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import logcat.logcat
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class CustomThemeCacheImpl(
  appDirectory: AppDirectory,
  private val fileSystem: FileSystem,
  private val contexts: CoroutineContexts,
) : CustomThemeCache {
  private val themesDir = appDirectory.get() / "themes"
  private val summariesFile = themesDir / "summaries.json"

  init {
    fileSystem.createDirectory(themesDir)
  }

  private val Path.isDirectory
    get() = fileSystem.metadataOrNull(this)?.isDirectory == true

  private val Path.isRegularFile
    get() = fileSystem.metadataOrNull(this)?.isRegularFile == true

  override suspend fun repos(): List<CustomThemeRepo> =
    withContext(contexts.io) {
      fileSystem
        .list(themesDir)
        .filter { it.isDirectory }
        .flatMap { userDir ->
          fileSystem
            .list(userDir)
            .filter { it.isRegularFile }
            .map { repoFile ->
              CustomThemeRepo(
                userName = userDir.name,
                repoName = repoFile.name.removeSuffix(".json"),
              )
            }
        }
    }

  override suspend fun summaries(): List<CustomThemeSummary> {
    val exists = withContext(contexts.io) { fileSystem.exists(summariesFile) }
    if (!exists) {
      return emptyList()
    }

    return try {
      val json = read(summariesFile)
      decode(json, SUMMARIES_SERIALIZER)
    } catch (e: IOException) {
      logcat.e(e) { "Failed reading summaries from $summariesFile" }
      emptyList()
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed decoding summaries from $summariesFile" }
      emptyList()
    }
  }

  override suspend fun theme(repo: CustomThemeRepo): CustomTheme? {
    val themeFile = repo.themeFile()
    val exists = withContext(contexts.io) { fileSystem.exists(themeFile) }
    if (!exists) {
      return null
    }

    return try {
      val json = read(themeFile)
      decode(json, THEME_SERIALIZER)
    } catch (e: IOException) {
      logcat.e(e) { "Failed reading theme from $themeFile" }
      null
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed decoding theme from $themeFile" }
      null
    }
  }

  override suspend fun save(summaries: List<CustomThemeSummary>) {
    try {
      val encoded = encode(summaries, SUMMARIES_SERIALIZER)
      write(encoded, summariesFile)
    } catch (e: IOException) {
      logcat.e(e) { "Failed writing summaries to $summariesFile" }
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed encoding summaries to $summariesFile" }
    }
  }

  override suspend fun save(theme: CustomTheme) {
    require(theme is JsonCustomTheme) { "Invalid custom theme: $theme" }
    val themeFile = theme.repo.themeFile()

    try {
      val encoded = encode(theme, THEME_SERIALIZER)
      withContext(contexts.io) { themeFile.parent?.let(fileSystem::createDirectory) }
      write(encoded, themeFile)
    } catch (e: IOException) {
      logcat.e(e) { "Failed writing theme to $themeFile" }
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed encoding theme to $themeFile" }
    }
  }

  override suspend fun clear() {
    withContext(contexts.io) {
      fileSystem.delete(summariesFile)
      fileSystem.deleteRecursively(themesDir)
      fileSystem.createDirectory(themesDir)
    }
  }

  private suspend fun <T> encode(value: T, serializer: SerializationStrategy<T>): String =
    withContext(contexts.default) { ThemeJson.encodeToString(serializer, value) }

  private suspend fun <T> decode(encoded: String, serializer: DeserializationStrategy<T>): T =
    withContext(contexts.default) { ThemeJson.decodeFromString(serializer, encoded) }

  private suspend fun write(encoded: String, path: Path) {
    withContext(contexts.io) { fileSystem.sink(path).buffer().use { it.writeUtf8(encoded) } }
  }

  private suspend fun read(path: Path): String =
    withContext(contexts.io) { fileSystem.source(path).buffer().use { it.readUtf8() } }

  private fun CustomThemeRepo.themeFile() = themesDir / userName / "$repoName.json"

  private companion object {
    val SUMMARIES_SERIALIZER = ListSerializer(CustomThemeSummary.serializer())
    val THEME_SERIALIZER = JsonCustomTheme.serializer()
  }
}
