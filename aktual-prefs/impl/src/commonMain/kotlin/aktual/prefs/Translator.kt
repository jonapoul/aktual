package aktual.prefs

import alakazam.kotlin.SerializableByString

internal interface Translator<Encoded, Decoded> {
  fun encode(value: Decoded): Encoded

  fun decode(value: Encoded): Decoded
}

internal inline fun <reified E : Enum<E>> enumOrdinalTranslator() =
  object : Translator<Int, E> {
    override fun encode(value: E): Int = value.ordinal

    override fun decode(value: Int): E = enumValues<E>()[value]
  }

internal inline fun <reified E> enumStringTranslator() where E : Enum<E>, E : SerializableByString =
  object : Translator<String, E> {
    override fun encode(value: E): String = value.value

    override fun decode(value: String): E =
      enumValues<E>().firstOrNull { it.value == value } ?: error("No ${E::class} matching '$value'")
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
