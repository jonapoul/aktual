package dev.jonpoulton.actual.core.model

import androidx.compose.runtime.Immutable
import net.afanasev.sekret.Secret

@Immutable
@JvmInline
value class Password(
  @Secret private val value: String,
) {
  override fun toString(): String = value

  companion object {
    val Empty = Password(value = "")
  }
}
