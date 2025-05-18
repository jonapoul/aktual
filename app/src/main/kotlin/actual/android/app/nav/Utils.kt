package actual.android.app.nav

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal inline fun <reified T> NavGraphBuilder.composableWithArg(
  extraTypes: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
  crossinline content: @Composable AnimatedContentScope.(T, NavBackStackEntry) -> Unit,
) where T : Any, T : Serializable = composable<T>(
  typeMap = extraTypes + mapOf(typeMapEntry<T>()),
) { navBackStackEntry ->
  val route = navBackStackEntry.toRoute<T>()
  content(route, navBackStackEntry)
}

internal inline fun <reified T : Serializable> typeMapEntry(noinline parseValue: ((String) -> T)? = null) =
  typeOf<T>() to WorkingSerializableType(T::class, parseValue)

internal class WorkingSerializableType<T : Serializable>(
  type: KClass<T>,
  private val valueParser: ((String) -> T)? = null,
) : NavType.SerializableType<T>(type.java) {
  // Not an unsupported operation! This throws UnsupportedOperationException in the super-call
  override fun parseValue(value: String): T = valueParser?.invoke(value) ?: super.parseValue(value)
}

internal val BudgetIdType = typeMapEntry<BudgetId>(::BudgetId)
internal val LoginTokenType = typeMapEntry<LoginToken>(::LoginToken)
