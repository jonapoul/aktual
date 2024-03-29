package dev.jonpoulton.actual.core.prefs

import com.fredporciuncula.flow.preferences.Preference
import com.fredporciuncula.flow.preferences.map

fun <R : Any> Preference<String>.simpleMap(constructor: (String) -> R): Preference<R> =
  map(
    mapper = constructor,
    reverse = { it.toString() },
  )

fun <R : Any> Preference<String?>.simpleNullableMap(constructor: (String) -> R): Preference<R?> =
  map(
    mapper = { if (it != null) constructor(it) else null },
    reverse = { it?.toString() },
  )
