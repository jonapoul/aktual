package actual.test

import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun <R : ComposeContentTestRule> R.runTest(block: R.() -> Unit) {
  block()
}
