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
package aktual.about.data

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * From https://github.com/cashapp/licensee/blob/trunk/src/main/kotlin/app/cash/licensee/outputModel.kt
 */
@Serializable
data class ArtifactDetail(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val name: String? = null,
  val spdxLicenses: Set<SpdxLicense> = emptySet(),
  val unknownLicenses: Set<UnknownLicense> = emptySet(),
  val scm: ArtifactScm? = null,
) {
  @get:Stable val id: String get() = "$groupId:$artifactId:$version"
}

@Serializable
data class SpdxLicense(
  val identifier: String,
  val name: String,
  val url: String,
)

val Apache2 = SpdxLicense(
  identifier = "Apache-2.0",
  name = "Apache License 2.0",
  url = "https://www.apache.org/licenses/LICENSE-2.0",
)

@Serializable
data class UnknownLicense(
  val name: String?,
  val url: String?,
)

@Serializable
data class ArtifactScm(
  val url: String,
)
