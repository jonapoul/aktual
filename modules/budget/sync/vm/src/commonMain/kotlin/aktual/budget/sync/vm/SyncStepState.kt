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

import aktual.core.model.Percent
import androidx.compose.runtime.Immutable

@Immutable
sealed interface SyncStepState {
  data object NotStarted : SyncStepState

  sealed interface InProgress : SyncStepState {
    data object Indefinite : InProgress
    data class Definite(val progress: Percent) : InProgress
  }

  sealed interface Stopped : SyncStepState
  data class Failed(val moreInfo: String) : Stopped
  data object Succeeded : Stopped
}
