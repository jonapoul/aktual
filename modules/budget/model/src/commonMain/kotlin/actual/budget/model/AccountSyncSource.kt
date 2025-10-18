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
@file:Suppress("SpellCheckingInspection", "FunctionName")

package actual.budget.model

@JvmInline
value class AccountSyncSource private constructor(val value: String) {
  override fun toString(): String = value

  companion object {
    val SimpleFin = AccountSyncSource(value = "simpleFin")
    val GoCardless = AccountSyncSource(value = "goCardless")
    val PluggyAi = AccountSyncSource(value = "pluggyai")
    fun Other(value: String) = AccountSyncSource(value)

    fun fromString(string: String): AccountSyncSource = when (string) {
      SimpleFin.value -> SimpleFin
      GoCardless.value -> GoCardless
      else -> Other(string)
    }
  }
}
