package aktual.prefs

interface SystemUiPreferences {
  val showBottomBar: Preference<Boolean>
  val hazeAppBars: Preference<Boolean>
  val hazeDialogs: Preference<Boolean>
  val hazeRadius: Preference<Float>
  val hazeAlpha: Preference<Float>
  val hidePreviewInAppSwitcher: Preference<Boolean>
}
