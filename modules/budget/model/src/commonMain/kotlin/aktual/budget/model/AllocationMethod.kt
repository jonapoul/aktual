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
package aktual.budget.model

/**
 * packages/loot-core/src/shared/rules.ts#ALLOCATION_METHODS
 */
enum class AllocationMethod(private val value: String) {
  FixedAmount("fixed-amount"), // a fixed amount
  FixedPercent("fixed-percent"), // a fixed percent of the remainder
  Remainder("remainder"), // an equal portion of the remainder
  ;

  override fun toString(): String = value
}
