/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
