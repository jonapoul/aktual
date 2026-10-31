package aktual.about.data

import github.api.model.GithubRelease

sealed interface LatestReleaseState {
  // Releases found, but nothing newer than the one installed
  data object NoNewUpdate : LatestReleaseState

  // No releases have been created on GitHub
  data object NoReleases : LatestReleaseState

  // Couldn't access the repo - it's probably set to private
  data object PrivateRepo : LatestReleaseState

  data class UpdateAvailable(val release: GithubRelease) : LatestReleaseState

  data class Failure(val errorMessage: String) : LatestReleaseState
}
