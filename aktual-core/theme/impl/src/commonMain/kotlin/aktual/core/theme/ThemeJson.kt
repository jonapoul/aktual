package aktual.core.theme

import aktual.core.model.AktualJson
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

private val module = SerializersModule { contextual(Color::class, ColorSerializer) }

val ThemeJson = Json(AktualJson) { serializersModule = module }
