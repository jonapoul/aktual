package actual.android.app

import actual.about.info.nav.InfoNavRoute
import actual.about.info.ui.InfoScreen
import actual.about.licenses.nav.LicensesNavRoute
import actual.about.licenses.ui.LicensesScreen
import actual.account.login.nav.LoginNavRoute
import actual.account.login.ui.LoginScreen
import actual.account.model.LoginToken
import actual.account.password.nav.ChangePasswordNavRoute
import actual.account.password.ui.ChangePasswordScreen
import actual.budget.list.nav.ListBudgetsNavRoute
import actual.budget.list.ui.ListBudgetsScreen
import actual.budget.sync.nav.SyncBudgetsNavRoute
import actual.budget.sync.ui.SyncBudgetScreen
import actual.settings.nav.SettingsNavRoute
import actual.settings.ui.SettingsScreen
import actual.url.nav.ServerUrlNavRoute
import actual.url.ui.ServerUrlScreen
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import java.io.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Composable
internal fun ActualNavHost(
  isServerUrlSet: Boolean,
  loginToken: LoginToken?,
  navController: NavHostController = rememberNavController(),
) {
  NavHost(
    navController = navController,
    startDestination = when {
      loginToken != null && isServerUrlSet -> ListBudgetsNavRoute(loginToken)
      isServerUrlSet -> LoginNavRoute
      else -> ServerUrlNavRoute
    },
  ) {
    composable<ChangePasswordNavRoute> { ChangePasswordScreen(navController) }

    composable<InfoNavRoute> { InfoScreen(navController) }

    composable<LicensesNavRoute> { LicensesScreen(navController) }

    composableWithArg<ListBudgetsNavRoute>(mapOf(LoginTokenType)) { route, _ ->
      ListBudgetsScreen(navController, route.token)
    }

    composable<LoginNavRoute> { LoginScreen(navController) }

    composable<ServerUrlNavRoute> { ServerUrlScreen(navController) }

    composable<SettingsNavRoute> { SettingsScreen(navController) }

    composableWithArg<SyncBudgetsNavRoute>(mapOf(LoginTokenType)) { route, _ ->
      SyncBudgetScreen(navController, route.token)
    }
  }
}

private inline fun <reified T> NavGraphBuilder.composableWithArg(
  extraTypes: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
  crossinline content: @Composable AnimatedContentScope.(T, NavBackStackEntry) -> Unit,
) where T : Any, T : Serializable = composable<T>(
  typeMap = extraTypes + mapOf(typeMapEntry<T>()),
) { navBackStackEntry ->
  val route = navBackStackEntry.toRoute<T>()
  content(route, navBackStackEntry)
}

private inline fun <reified T : Serializable> typeMapEntry(noinline parseValue: ((String) -> T)? = null) =
  typeOf<T>() to WorkingSerializableType(T::class.java, parseValue)

private class WorkingSerializableType<T : Serializable>(
  type: Class<T>,
  private val valueParser: ((String) -> T)? = null,
) : NavType.SerializableType<T>(type) {
  // Not an unsupported operation!
  override fun parseValue(value: String): T = valueParser?.invoke(value) ?: super.parseValue(value)
}

private val LoginTokenType = typeMapEntry<LoginToken>(::LoginToken)
