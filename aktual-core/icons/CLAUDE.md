# aktual-core:icons

Custom and Material icon definitions for Aktual, stored as `ImageVector` properties.

## Two icon families

- **`AktualIcons`** — custom SVG icons (non-Material). Package: `aktual.core.icons`. Use `aktualIcon()` + `aktualPath {}`. Size is typically 20f or 24f.
- **`MaterialIcons`** — Material Design icons. Package: `aktual.core.icons.material`. Use `materialIcon()` + `materialPath {}`. Fixed 24dp, or 960-unit viewport for Material Symbols.

## File patterns

### AktualIcons (package `aktual.core.icons`)

```kotlin
@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.FooBar: ImageVector by lazy {
  aktualIcon(name = "FooBar", size = 20f) {
    aktualPath {
      // path commands
    }
  }
}
```

### MaterialIcons (package `aktual.core.icons.material`)

```kotlin
@file:Suppress("UnusedReceiverParameter") // don't omit this

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.FooBar: ImageVector by lazy {
  materialIcon(name = "FooBar") {          // adds "Material." prefix
    materialPath {
      // path commands
    }
  }
}
```

## Coordinate systems

There are two coordinate systems in use:

### Standard Material Icons (24×24)
- `viewBox="0 0 24 24"`, coordinates in [0, 24]
- Use `materialIcon(name = "...")` with no extra args
- Source: https://android.googlesource.com/platform//frameworks/support/+/1de65587b7e999a38df120bd8827c3594974864d/compose/material

### Material Symbols (960×960)
- `viewBox="0 -960 960 960"` — y-axis ranges from -960 (top) to 0 (bottom)
- Use `materialIcon(name = "...", viewportSize = 960f)`
- **Y conversion**: `y_kotlin = y_svg + 960` for absolute coordinates; relative offsets are unchanged
- Example: SVG `M480-120` → `moveTo(480f, 840f)` (840 = -120 + 960)
- Source: https://fonts.google.com/icons (download SVG, read viewBox to confirm)

## SVG Path Conversion Reference

- `M x y` → `moveTo(x.0f, y.0f)`
- `L x y` → `lineTo(x.0f, y.0f)`
- `l x y` → `lineToRelative(x.0f, y.0f)`
- `C x1 y1 x2 y2 x y` → `curveTo(x1.0f, y1.0f, x2.0f, y2.0f, x.0f, y.0f)`
- `c x1 y1 x2 y2 x y` → `curveToRelative(x1.0f, y1.0f, x2.0f, y2.0f, x.0f, y.0f)`
- `H x` → `horizontalLineTo(x.0f)`
- `h x` → `horizontalLineToRelative(x.0f)`
- `V y` → `verticalLineTo(y.0f)`
- `v y` → `verticalLineToRelative(y.0f)`
- `Z` → `close()`

## Adding a new icon

**AktualIcons:** Create `IconName.kt` in `aktual/core/icons/`, add to `aktualIcons` list in `IconPreviews.kt`.

**MaterialIcons:** Create `IconName.kt` in `aktual/core/icons/material/`, add to `materialIcons` list in `MaterialIconPreviews.kt`.

Add icons in alphabetical order within the list.

## Preview files

| File                    | Icon family     | List variable  |
|-------------------------|-----------------|----------------|
| `IconPreviews.kt`       | `AktualIcons`   | `aktualIcons`  |
| `material/MaterialIconPreviews.kt` | `MaterialIcons` | `materialIcons` |

The `@PreviewParameter` provider uses `getDisplayName` to label each icon by name in the Android Studio preview panel, so all icons in a family appear in one parameterized preview.
