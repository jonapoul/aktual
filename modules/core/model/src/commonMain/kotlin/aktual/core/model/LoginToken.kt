/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Serializable
@JvmInline
value class LoginToken(val value: String) : JavaIoSerializable {
  override fun toString(): String = value
}
