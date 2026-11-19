package aktual.updates.domain

interface NightlyInstaller {
  // Downloads the build's APK and hands it to the system installer. progress is between 0 and 1,
  // or null if the download size isn't known
  suspend fun install(build: NightlyBuild, onProgress: (Float?) -> Unit)
}
