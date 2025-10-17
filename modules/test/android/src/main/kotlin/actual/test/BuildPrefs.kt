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
package actual.test

import actual.prefs.AndroidEncryptedPreferences
import actual.prefs.EncryptedPreferences
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import kotlin.coroutines.CoroutineContext

fun buildSharedPreferences(
  context: Context = ApplicationProvider.getApplicationContext(),
  name: String = "prefs",
): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

fun buildPreferences(
  coroutineContext: CoroutineContext,
  sharedPreferences: SharedPreferences = buildSharedPreferences(),
): Preferences = AndroidSharedPreferences(sharedPreferences, coroutineContext)

fun buildEncryptedPreferences(
  coroutineContext: CoroutineContext,
  sharedPreferences: SharedPreferences = buildSharedPreferences(),
): EncryptedPreferences = AndroidEncryptedPreferences(sharedPreferences, coroutineContext)
