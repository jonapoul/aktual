package dev.jonpoulton.actual.api.client

import alakazam.kotlin.core.StateHolder
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualApisStateHolder @Inject constructor() : StateHolder<ActualApis?>(initialState = null) {
  fun getIfMatches(serverUrl: String): ActualApis? {
    val current = peek() ?: return null
    val matches = current.serverUrl == serverUrl
    return if (matches) current else null
  }
}
