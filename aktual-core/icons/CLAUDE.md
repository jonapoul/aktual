# aktual-core:icons

Custom and Material icon definitions for Aktual, stored as `ImageVector` properties.

## Two icon families

- **`AktualIcons`** — custom SVG icons (non-Material). Use `aktualIcon()` + `aktualPath {}`. Size is typically 20f or 24f.
- **`MaterialIcons`** — Material Design icons. Use `materialIcon()` + `materialPath {}`. Fixed 24dp, or 960-unit viewport for Material Symbols.

## File pattern

```kotlin
@file:Suppress("UnusedReceiverParameter") // don't omit this

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
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

## Adding a new icon

1. Create `IconName.kt` in this directory following the pattern above.
2. Add the icon to `IconPreviews.kt` in alphabetical order within the correct list (see below).

## IconPreviews.kt

Contains two `@Preview` composables, each backed by a list.

| List variable  | Icon family     | Add your icon here if…          |
|----------------|-----------------|----------------------------------|
| `aktualIcons`  | `AktualIcons`   | you added `val AktualIcons.Foo` |
| `materialIcons`| `MaterialIcons` | you added `val MaterialIcons.Foo`|

The `@PreviewParameter` provider uses `getDisplayName` to label each icon by name in the Android Studio preview panel, so all icons in a family appear in one parameterized preview.
