package aktual.core.theme

import aktual.core.model.AktualJson
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val ThemeJson =
  Json(AktualJson) {
    serializersModule = SerializersModule { contextual(Color::class, ColorSerializer) }
  }
