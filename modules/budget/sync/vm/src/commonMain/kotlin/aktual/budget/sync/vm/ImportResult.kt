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

import aktual.budget.model.DbMetadata

sealed interface ImportResult {
  data class Success(val meta: DbMetadata) : ImportResult

  sealed interface Failure : ImportResult
  data object NotZipFile : Failure
  data object InvalidZipFile : Failure
  data object InvalidMetaFile : Failure
  data class OtherFailure(val message: String) : Failure
}
