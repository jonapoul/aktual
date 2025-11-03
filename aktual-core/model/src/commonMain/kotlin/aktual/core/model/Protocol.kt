/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

enum class Protocol(private val value: String) {
  Http("http"),
  Https("https"),
  ;

  override fun toString(): String = value
}
