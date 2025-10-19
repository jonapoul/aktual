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
