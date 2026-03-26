package aktual.core.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal fun DataStore<Preferences>.boolean(
  key: Preferences.Key<Boolean>,
  default: Boolean?,
): NullablePreference<Boolean> = SimpleNullablePreferenceImpl(this, key, default)

internal fun DataStore<Preferences>.int(
  key: Preferences.Key<Int>,
  default: Int?,
): NullablePreference<Int> = SimpleNullablePreferenceImpl(this, key, default)

internal fun DataStore<Preferences>.double(
  key: Preferences.Key<Double>,
  default: Double?,
): NullablePreference<Double> = SimpleNullablePreferenceImpl(this, key, default)

internal fun DataStore<Preferences>.string(
  key: Preferences.Key<String>,
  default: String?,
): NullablePreference<String> = SimpleNullablePreferenceImpl(this, key, default)

internal fun DataStore<Preferences>.float(
  key: Preferences.Key<Float>,
  default: Float?,
): NullablePreference<Float> = SimpleNullablePreferenceImpl(this, key, default)

internal fun DataStore<Preferences>.long(
  key: Preferences.Key<Long>,
  default: Long?,
): NullablePreference<Long> = SimpleNullablePreferenceImpl(this, key, default)

internal fun <Encoded : Any, Decoded : Any> DataStore<Preferences>.translated(
  key: Preferences.Key<Encoded>,
  default: Decoded?,
  translator: Translator<Encoded, Decoded>,
): NullablePreference<Decoded> = NullablePreferenceImpl(this, key, default, translator)
