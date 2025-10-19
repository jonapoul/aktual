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
package github.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A subset of the fields returned by https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 */
@Serializable
data class GithubRelease(
  // The title (version string) of the release. E.g. "1.2.3"
  @SerialName("name") val versionName: String,

  // The ISO-8601 string timestamp of the publish. E.g. "2021-11-06T12:15:10Z"
  @SerialName("published_at") val publishedAt: Instant,

  // The URL of the release tag. E.g. "https://github.com/jonapoul/aktual/releases/tag/1.0.0"
  @SerialName("html_url") val htmlUrl: String,

  // The name of the tag, e.g. "v1.2.3"
  @SerialName("tag_name") val tagName: String,
)
