/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.prefs

import aktual.core.model.Base64String
import aktual.core.model.KeyId
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import dev.zacsweers.metro.Inject

@Inject
class KeyPreferences(private val prefs: EncryptedPreferences) {
  operator fun contains(keyId: KeyId?): Boolean = keyId?.let { prefs.contains(key(it)) } == true
  operator fun get(keyId: KeyId?): Base64String? = keyId?.let { preference(it).get() }
  operator fun set(keyId: KeyId, value: Base64String) = preference(keyId).set(value)

  suspend fun setAndCommit(keyId: KeyId, value: Base64String) = preference(keyId).setAndCommit(value)

  fun delete(keyId: KeyId) = preference(keyId).delete()
  fun asFlow(keyId: KeyId) = preference(keyId).asFlow()

  private fun preference(keyId: KeyId) = prefs.getNullableObject(key(keyId), Base64Serializer, default = null)
  private fun key(keyId: KeyId) = "key-$keyId"

  private companion object {
    private val Base64Serializer = SimpleNullableStringSerializer { string -> string?.let(::Base64String) }
  }
}
