package aktual.prefs

import alakazam.kotlin.SerializableByString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

interface Translator<Encoded, Decoded> {
  fun encode(value: Decoded): Encoded

  // null falls back to the preference's default
  fun decode(value: Encoded): Decoded?
}

inline fun <reified E : Enum<E>> enumOrdinalTranslator() =
  object : Translator<Int, E> {
    override fun encode(value: E): Int = value.ordinal

    override fun decode(value: Int): E? = enumValues<E>().getOrNull(value)
  }

internal inline fun <reified E> enumStringTranslator() where E : Enum<E>, E : SerializableByString =
  object : Translator<String, E> {
    override fun encode(value: E): String = value.value

    override fun decode(value: String): E? = enumValues<E>().firstOrNull { it.value == value }
  }

internal inline fun <reified T : Any> toStringTranslator(crossinline constructor: (String) -> T) =
  object : Translator<String, T> {
    override fun encode(value: T): String = value.toString()

    override fun decode(value: String): T = constructor(value)
  }

internal class NoOpTranslator<T> : Translator<T, T> {
  override fun encode(value: T): T = value

  override fun decode(value: T): T = value
}

// Stores a serializable enum as its bare serial name. Decoding goes through the serializer, so an
// unrecognised value lands on the enum's @Fallback entry.
internal fun <E : Enum<E>> serialNameTranslator(serializer: KSerializer<E>) =
  object : Translator<String, E> {
    override fun encode(value: E): String =
      Json.encodeToJsonElement(serializer, value).jsonPrimitive.content

    override fun decode(value: String): E =
      Json.decodeFromJsonElement(serializer, JsonPrimitive(value))
  }
