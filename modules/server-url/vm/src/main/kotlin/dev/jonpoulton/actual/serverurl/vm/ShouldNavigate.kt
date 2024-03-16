package dev.jonpoulton.actual.serverurl.vm

sealed interface ShouldNavigate {
  data object ToBootstrap : ShouldNavigate
  data object ToLogin : ShouldNavigate
}
