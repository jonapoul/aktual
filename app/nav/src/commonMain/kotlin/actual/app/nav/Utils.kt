package actual.app.nav

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.Serializable
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

internal inline fun <reified T : Serializable> typeMapEntry(): Pair<KType, SerializableType<T>> {
  val serializer = Json.serializersModule.serializer<T>()
  return typeOf<T>() to SerializableType(serializer)
}

internal class SerializableType<T : Any>(
  private val serializer: KSerializer<T>,
) : NavType<T>(isNullableAllowed = false) {
  override val name = serializer.descriptor.serialName
  override fun put(bundle: SavedState, key: String, value: T) = bundle.write { putString(key, serializeAsValue(value)) }
  override fun get(bundle: SavedState, key: String): T = bundle.read { parseValue(getString(key)) }
  override fun parseValue(value: String): T = Json.decodeFromString(serializer, value)
  override fun serializeAsValue(value: T): String = Json.encodeToString(serializer, value)
}

internal val BudgetIdType = typeMapEntry<BudgetId>()
internal val LoginTokenType = typeMapEntry<LoginToken>()
internal val WidgetIdType = typeMapEntry<WidgetId>()
