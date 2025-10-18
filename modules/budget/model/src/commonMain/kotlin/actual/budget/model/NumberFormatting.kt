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
package actual.budget.model

enum class NumberFormat(
  val value: String,
  val thousandsSeparator: String,
  val decimalSeparator: String,
) {
  // e.g. 1,000.33
  CommaDot(value = "comma-dot", thousandsSeparator = ",", decimalSeparator = "."),

  // e.g. 1.000,33
  DotComma(value = "dot-comma", thousandsSeparator = ".", decimalSeparator = ","),

  // e.g. 1 000,33
  SpaceComma(value = "space-comma", thousandsSeparator = "\u00A0", decimalSeparator = ","),

  // e.g. 1'000.33
  ApostropheDot(value = "apostrophe-dot", thousandsSeparator = "’", decimalSeparator = "."),

  // e.g. 1,00,000.33
  CommaDotIn(value = "comma-dot-in", thousandsSeparator = ",", decimalSeparator = "."),
  ;

  companion object {
    val Default = CommaDot
    fun from(value: String?): NumberFormat = entries.firstOrNull { it.value == value } ?: Default
  }
}

data class NumberFormatConfig(
  val format: NumberFormat,
  val hideFraction: Boolean,
)
