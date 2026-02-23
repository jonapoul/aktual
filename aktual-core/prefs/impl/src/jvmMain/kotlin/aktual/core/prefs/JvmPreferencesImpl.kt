package aktual.core.prefs

import dev.jonpoulton.preferences.core.BooleanSerializer
import dev.jonpoulton.preferences.core.FloatSerializer
import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.LongSerializer
import dev.jonpoulton.preferences.core.NullableStringSerializer
import dev.jonpoulton.preferences.core.NullableStringSetSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.Preferences.NullableSerializer
import dev.jonpoulton.preferences.core.Preferences.Serializer
import dev.jonpoulton.preferences.core.StringSerializer
import dev.jonpoulton.preferences.core.StringSetSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.util.prefs.Preferences as JPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Inject
@ContributesBinding(AppScope::class, binding<JvmPreferences>())
@ContributesBinding(AppScope::class, binding<Preferences>())
class JvmPreferencesImpl(private val prefs: JPreferences) : JvmPreferences {
  private val changeFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)

  init {
    prefs.addPreferenceChangeListener { evt -> changeFlow.tryEmit(evt.key) }
  }

  override fun flush() = prefs.flush()

  override fun clear() {
    prefs.clear()
    prefs.flush()
  }

  override fun contains(key: String): Boolean = prefs.get(key, null) != null

  override fun getBoolean(key: String, default: Boolean): Preference<Boolean> =
    BooleanPreference(key, default, prefs, changeFlow)

  override fun getFloat(key: String, default: Float): Preference<Float> =
    FloatPreference(key, default, prefs, changeFlow)

  override fun getInt(key: String, default: Int): Preference<Int> =
    IntPreference(key, default, prefs, changeFlow)

  override fun getLong(key: String, default: Long): Preference<Long> =
    LongPreference(key, default, prefs, changeFlow)

  override fun getNullableString(key: String, default: String?): Preference<String?> =
    NullableStringPreference(key, default, prefs, changeFlow)

  override fun getString(key: String, default: String): Preference<String> =
    StringPreference(key, default, prefs, changeFlow)

  override fun getNullableStringSet(key: String, default: Set<String>?): Preference<Set<String>?> =
    NullableStringSetPreference(key, default, prefs, changeFlow)

  override fun getNullableStringSetOfNullables(
    key: String,
    default: Set<String?>?,
  ): Preference<Set<String?>?> =
    NullableStringSetOfNullablesPreference(key, default, prefs, changeFlow)

  override fun getStringSet(key: String, default: Set<String>): Preference<Set<String>> =
    StringSetPreference(key, default, prefs, changeFlow)

  override fun getStringSetOfNullables(
    key: String,
    default: Set<String?>,
  ): Preference<Set<String?>> = StringSetOfNullablesPreference(key, default, prefs, changeFlow)

  override fun <R : Any, T : Any> getObject(
    key: String,
    serializer: Serializer<R, T>,
    default: T,
  ): Preference<T> = ObjectPreference(key, default, prefs, changeFlow, serializer)

  override fun <R : Any, T : Any> getNullableObject(
    key: String,
    serializer: NullableSerializer<R, T>,
    default: T?,
  ): Preference<T?> = NullableObjectPreference(key, default, prefs, changeFlow, serializer)
}

private abstract class BasePreference<T>(
  override val key: String,
  override val default: T,
  private val changeFlow: MutableSharedFlow<String>,
) : Preference<T> {
  protected abstract val prefs: JPreferences

  override fun isSet(): Boolean = key in prefs.keys()

  override fun asFlow(): Flow<T> =
    changeFlow.asSharedFlow().onStart { emit(key) }.filter { it == key }.map { get() }

  override suspend fun setAndCommit(value: T): Boolean {
    set(value)
    prefs.flush()
    return true
  }

  override suspend fun deleteAndCommit(): Boolean {
    delete()
    prefs.flush()
    return true
  }
}

private class BooleanPreference(
  key: String,
  default: Boolean,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Boolean>(key, default, changeFlow) {
  override fun get(): Boolean = prefs.getBoolean(key, default)

  override fun set(value: Boolean) = prefs.putBoolean(key, value)

  override fun delete() = prefs.remove(key)
}

private class FloatPreference(
  key: String,
  default: Float,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Float>(key, default, changeFlow) {
  override fun get(): Float = prefs.getFloat(key, default)

  override fun set(value: Float) = prefs.putFloat(key, value)

  override fun delete() = prefs.remove(key)
}

private class IntPreference(
  key: String,
  default: Int,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Int>(key, default, changeFlow) {
  override fun get(): Int = prefs.getInt(key, default)

  override fun set(value: Int) = prefs.putInt(key, value)

  override fun delete() = prefs.remove(key)
}

private class LongPreference(
  key: String,
  default: Long,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Long>(key, default, changeFlow) {
  override fun get(): Long = prefs.getLong(key, default)

  override fun set(value: Long) = prefs.putLong(key, value)

  override fun delete() = prefs.remove(key)
}

private class StringPreference(
  key: String,
  default: String,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<String>(key, default, changeFlow) {
  override fun get(): String = prefs.get(key, default)

  override fun set(value: String) = prefs.put(key, value)

  override fun delete() = prefs.remove(key)
}

private class NullableStringPreference(
  key: String,
  default: String?,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<String?>(key, default, changeFlow) {
  override fun get(): String? = prefs.get(key, default)

  override fun set(value: String?) = if (value == null) prefs.remove(key) else prefs.put(key, value)

  override fun delete() = prefs.remove(key)
}

private class StringSetPreference(
  key: String,
  default: Set<String>,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Set<String>>(key, default, changeFlow) {
  override fun get() =
    prefs.get(key, null)?.split(COMMA)?.filter { it.isNotEmpty() }?.toSet() ?: default

  override fun set(value: Set<String>) = prefs.put(key, value.joinToString(COMMA))

  override fun delete() = prefs.remove(key)
}

private class NullableStringSetPreference(
  key: String,
  default: Set<String>?,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Set<String>?>(key, default, changeFlow) {
  override fun get() =
    prefs.get(key, null)?.split(COMMA)?.filter { it.isNotEmpty() }?.toSet() ?: default

  override fun set(value: Set<String>?) =
    if (value == null) prefs.remove(key) else prefs.put(key, value.joinToString(COMMA))

  override fun delete() = prefs.remove(key)
}

private class StringSetOfNullablesPreference(
  key: String,
  default: Set<String?>,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Set<String?>>(key, default, changeFlow) {
  override fun get() =
    prefs.get(key, null)?.split(COMMA)?.map { if (it == NULL_CHAR) null else it }?.toSet()
      ?: default

  override fun set(value: Set<String?>) =
    prefs.put(key, value.joinToString(COMMA) { it ?: NULL_CHAR })

  override fun delete() = prefs.remove(key)
}

private class NullableStringSetOfNullablesPreference(
  key: String,
  default: Set<String?>?,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
) : BasePreference<Set<String?>?>(key, default, changeFlow) {
  override fun get(): Set<String?>? =
    prefs.get(key, null)?.split(COMMA)?.map { if (it == NULL_CHAR) null else it }?.toSet()
      ?: default

  override fun set(value: Set<String?>?) =
    if (value == null) {
      prefs.remove(key)
    } else {
      prefs.put(key, value.joinToString(COMMA) { it ?: NULL_CHAR })
    }

  override fun delete() = prefs.remove(key)
}

private class ObjectPreference<R : Any, T : Any>(
  key: String,
  default: T,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
  private val serializer: Serializer<R, T>,
) : BasePreference<T>(key, default, changeFlow) {
  private val serializedDefault = serializer.serialize(default)

  @Suppress("UNCHECKED_CAST", "CyclomaticComplexMethod")
  override fun get(): T =
    when (serializer) {
      is BooleanSerializer<*> ->
        serializer.deserialize(prefs.getBoolean(key, serializedDefault as Boolean))
      is FloatSerializer<*> ->
        serializer.deserialize(prefs.getFloat(key, serializedDefault as Float))
      is IntSerializer<*> -> serializer.deserialize(prefs.getInt(key, serializedDefault as Int))
      is LongSerializer<*> -> serializer.deserialize(prefs.getLong(key, serializedDefault as Long))
      is StringSerializer<*> ->
        serializer.deserialize(prefs.get(key, null) ?: serializedDefault as String)
      is StringSetSerializer<*> -> error("Not supported right now")
    }
      as? T? ?: default

  override fun set(value: T) {
    prefs.put(key, value, serializer)
  }

  override fun delete() = prefs.remove(key)
}

private class NullableObjectPreference<R : Any, T : Any>(
  key: String,
  default: T?,
  override val prefs: JPreferences,
  changeFlow: MutableSharedFlow<String>,
  private val serializer: NullableSerializer<R, T>,
) : BasePreference<T?>(key, default, changeFlow) {
  private val serializedDefault = serializer.serialize(default)

  @Suppress("UNCHECKED_CAST")
  override fun get(): T? =
    when (serializer) {
      is NullableStringSerializer<*> ->
        serializer.deserialize(prefs.get(key, null) ?: serializedDefault as? String)
      is NullableStringSetSerializer<*> -> error("Not supported right now")
    }
      as? T? ?: default

  override fun set(value: T?) = if (value == null) delete() else prefs.put(key, value, serializer)

  override fun delete() = prefs.remove(key)
}

private const val COMMA = "\u001F"
private const val NULL_CHAR = "\u0000"

private fun <R : Any, T : Any> JPreferences.put(
  key: String,
  value: T?,
  serializer: NullableSerializer<R, T>,
) =
  when (serializer) {
    is NullableStringSerializer -> put(key, serializer.serialize(value))
    is NullableStringSetSerializer -> error("Not supported right now: $key $value $serializer")
  }

private fun <R : Any, T : Any> JPreferences.put(
  key: String,
  value: T,
  serializer: Serializer<R, T>,
) =
  when (serializer) {
    is BooleanSerializer -> putBoolean(key, serializer.serialize(value))
    is FloatSerializer -> putFloat(key, serializer.serialize(value))
    is IntSerializer -> putInt(key, serializer.serialize(value))
    is LongSerializer -> putLong(key, serializer.serialize(value))
    is StringSerializer -> put(key, serializer.serialize(value))
    is StringSetSerializer<*> -> error("Not supported right now: $key $value")
  }
