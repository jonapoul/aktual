/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.Serializable
import java.util.stream.IntStream

@Redacted
@JvmInline
@Serializable
value class Password(val value: String) : CharSequence by value {
  override fun chars(): IntStream = value.chars()
  override fun codePoints(): IntStream = value.codePoints()

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
