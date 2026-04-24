# aktual-core:icons

Icon definitions as `ImageVector` properties, split into two families:

- **`AktualIcons`** (package `aktual.core.icons`) — custom SVG icons. Built with `aktualIcon()` + `aktualPath {}`; typical size 20f or 24f.
- **`MaterialIcons`** (package `aktual.core.icons.material`) — Material Design icons. Built with `materialIcon()` + `materialPath {}`; 24dp, or 960-unit viewport for Material Symbols.

Prefer the `/add-svg-icon` and `/add-material-icon` skills — they handle SVG → `ImageVector` conversion. The notes below are for when you need to do it by hand.

## File shape

Each icon is its own file. Example — for `AktualIcons`:

```kotlin
@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.FooBar: ImageVector by lazy {
  aktualIcon(name = "FooBar", size = 20f) { aktualPath { /* path */ } }
}
```

`MaterialIcons` is identical modulo package (`aktual.core.icons.material`) and helpers (`materialIcon`/`materialPath`). `materialIcon(name = "...")` automatically prefixes `"Material."`.

## Coordinate systems

- **Standard Material (24×24)**: `viewBox="0 0 24 24"`, use `materialIcon(name = "...")`.
- **Material Symbols (960×960)**: `viewBox="0 -960 960 960"` (y runs from -960 top to 0 bottom). Use `materialIcon(name = "...", viewportSize = 960f)`. Convert absolute y: `y_kotlin = y_svg + 960` (e.g. SVG `M480-120` → `moveTo(480f, 840f)`). Relative offsets are unchanged.

## SVG path → Compose path

`M`→`moveTo`, `L`→`lineTo`, `l`→`lineToRelative`, `C`→`curveTo`, `c`→`curveToRelative`, `H`→`horizontalLineTo`, `h`→`horizontalLineToRelative`, `V`→`verticalLineTo`, `v`→`verticalLineToRelative`, `Z`→`close`.

## Wiring up a new icon

Create the file in the family's package, then append to the alphabetically-sorted list in the matching preview file (`IconPreviews.kt` → `aktualIcons`, or `material/MaterialIconPreviews.kt` → `materialIcons`). The `@PreviewParameter` renders the whole list in Android Studio.
