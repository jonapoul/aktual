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

enum class FirstDayOfWeek(val value: Int) {
  Sunday(value = 0),
  Monday(value = 1),
  Tuesday(value = 2),
  Wednesday(value = 3),
  Thursday(value = 4),
  Friday(value = 5),
  Saturday(value = 6),
  ;

  companion object {
    fun from(value: String?): FirstDayOfWeek? {
      val int = value?.toInt() ?: return null
      return entries.firstOrNull { it.value == int }
    }
  }
}
