package actual.about.data

import github.api.model.GithubRelease

sealed interface LatestReleaseState {
  // Releases found, but nothing newer than the one installed
  data object NoNewUpdate : LatestReleaseState

  // Releases returned, but it's an empty list. No releases have been created on GitHub
  data object NoReleases : LatestReleaseState

  // Couldn't access the repo - it's probably set to private
  data object PrivateRepo : LatestReleaseState

  // A newer release was found on GitHub than the one you have installed
  data class UpdateAvailable(val release: GithubRelease) : LatestReleaseState

  // Some other failure when checking
  data class Failure(val errorMessage: String) : LatestReleaseState
}
