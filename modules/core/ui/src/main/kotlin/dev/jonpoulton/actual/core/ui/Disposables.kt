package dev.jonpoulton.actual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun OnDispose(onDisposeEffect: () -> Unit) {
  DisposableEffect(Unit) {
    onDispose(onDisposeEffect)
  }
}
