package aktual.core.prefs

interface SystemUiPreferences {
  val showBottomBar: Preference<Boolean>
  val blurAppBars: Preference<Boolean>
  val blurDialogs: Preference<Boolean>
  val blurRadius: Preference<Float>
  val blurAlpha: Preference<Float>
}
