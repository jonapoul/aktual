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
package aktual.preview

import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetState

internal val PreviewBudgetSynced = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = BudgetId("abc123"),
  hasKey = true,
)

internal val PreviewBudgetSyncing = PreviewBudgetSynced.copy(
  name = "Syncing Budget",
  state = BudgetState.Syncing,
  encryptKeyId = null,
)

internal val PreviewBudgetBroken = PreviewBudgetSynced.copy(
  name = "Broken Budget",
  state = BudgetState.Broken,
  encryptKeyId = null,
)
