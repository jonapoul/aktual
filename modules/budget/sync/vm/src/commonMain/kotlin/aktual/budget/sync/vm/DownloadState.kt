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
package aktual.budget.sync.vm

import aktual.core.model.Bytes
import okio.Path

sealed interface DownloadState {
  val total: Bytes

  data class InProgress(
    val read: Bytes,
    override val total: Bytes,
  ) : DownloadState

  data class Done(
    override val total: Bytes,
    val path: Path,
  ) : DownloadState

  sealed interface Failure : DownloadState {
    val message: String

    data object NotLoggedIn : Failure {
      override val total = Bytes.Zero
      override val message = "Not logged in"
    }

    data class IO(
      override val message: String,
      override val total: Bytes,
    ) : Failure

    data class Http(
      val code: Int,
      override val message: String,
      override val total: Bytes,
    ) : Failure

    data class Other(
      override val message: String,
      override val total: Bytes,
    ) : Failure
  }
}
