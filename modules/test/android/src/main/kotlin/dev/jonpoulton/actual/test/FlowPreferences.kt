package dev.jonpoulton.actual.test

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import kotlinx.coroutines.CoroutineDispatcher

fun buildSharedPreferences(): SharedPreferences {
  val context = ApplicationProvider.getApplicationContext<Context>()
  return context.getSharedPreferences("test", Context.MODE_PRIVATE)
}

fun buildFlowPreferences(
  dispatcher: CoroutineDispatcher,
  prefs: SharedPreferences = buildSharedPreferences(),
): FlowSharedPreferences {
  return FlowSharedPreferences(prefs, dispatcher)
}
