package dev.jonpoulton.actual.test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import kotlinx.coroutines.CoroutineDispatcher

fun buildFlowPreferences(dispatcher: CoroutineDispatcher): FlowSharedPreferences {
  val context = ApplicationProvider.getApplicationContext<Context>()
  val sharedPrefs = context.getSharedPreferences("test", Context.MODE_PRIVATE)
  return FlowSharedPreferences(sharedPrefs, dispatcher)
}
