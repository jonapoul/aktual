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
package actual.settings.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
internal sealed interface Clickability

@Immutable
internal data object NotClickable : Clickability

@Immutable
internal data class Clickable(
  val enabled: Boolean = true,
  val onClick: () -> Unit,
) : Clickability

@Stable
internal fun Clickable(onClick: () -> Unit) = Clickable(enabled = true, onClick)
