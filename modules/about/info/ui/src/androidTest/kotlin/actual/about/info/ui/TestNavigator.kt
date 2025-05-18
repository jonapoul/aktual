package actual.about.info.ui

internal class TestNavigator : InfoNavigator {
  var hasNavigated = false

  override fun back(): Boolean = true

  override fun toLicenses() {
    hasNavigated = true
  }
}
