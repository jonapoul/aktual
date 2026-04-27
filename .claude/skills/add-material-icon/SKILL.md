---
name: add-material-icon
description: Fetch a Material Icon SVG and convert it to a Compose ImageVector in the aktual-core:icons module. Use when the user wants to add a new Material icon.
argument-hint: "<icon_name> [--classic]"
---

Add a Material Design icon to the `aktual-core/icons` module by fetching its SVG source and converting it to a Kotlin `ImageVector`.

Do NOT ask clarifying questions — just execute.

## Arguments

- `$ARGUMENTS` should contain the icon name in either snake_case or PascalCase (e.g., `calendar_today` or `CalendarToday`)
- If `--classic` is included, fetch from classic Material Icons (24x24) instead of Material Symbols
- If `--material` is included, use `MaterialIcons` as the receiver instead of `AktualIcons`

## Step-by-Step Process

### 1. Determine source and fetch SVG

**Material Symbols (default, 960x960):**

Fetch the filled variant SVG directly from the Google Fonts CDN:
```
https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsoutlined/{snake_case_name}/fill1/24px.svg
```

The `fill1` path segment selects the filled variant. The response is a plain SVG file (not base64).

**Classic Material Icons (with `--classic`, 24x24):**

The source repo is: `https://android.googlesource.com/platform/frameworks/support/+/1de65587b7e999a38df120bd8827c3594974864d/compose/material/material/icons/generator/raw-icons/filled/`

Convert the PascalCase icon name to snake_case for the filename (e.g., `AccountCircle` → `account_circle`).

Fetch the SVG using WebFetch:
```
https://android.googlesource.com/platform/frameworks/support/+/1de65587b7e999a38df120bd8827c3594974864d/compose/material/material/icons/generator/raw-icons/filled/{snake_case_name}.xml?format=TEXT
```

The response is base64 encoded. Decode it to get the Android Vector Drawable XML.

### 2. Parse the SVG/Vector Drawable

From the Android Vector Drawable XML (classic), extract:
- `android:viewportWidth` and `android:viewportHeight` (typically 24)
- `android:pathData` from each `<path>` element

From an SVG (symbols), extract:
- The `viewBox` attribute (typically `0 -960 960 960`)
- The `d` attribute from each `<path>` element

### 3. Convert path data to Kotlin

Convert SVG/Android path commands to Kotlin `PathBuilder` calls:

| SVG Command | Kotlin Call |
|-------------|-------------|
| `M x,y` | `moveTo(xf, yf)` |
| `m dx,dy` | `moveToRelative(dxf, dyf)` |
| `L x,y` | `lineTo(xf, yf)` |
| `l dx,dy` | `lineToRelative(dxf, dyf)` |
| `H x` | `horizontalLineTo(xf)` |
| `h dx` | `horizontalLineToRelative(dxf)` |
| `V y` | `verticalLineTo(yf)` |
| `v dy` | `verticalLineToRelative(dyf)` |
| `C x1,y1 x2,y2 x,y` | `curveTo(x1f, y1f, x2f, y2f, xf, yf)` |
| `c dx1,dy1 dx2,dy2 dx,dy` | `curveToRelative(dx1f, dy1f, dx2f, dy2f, dxf, dyf)` |
| `S x2,y2 x,y` | `reflectiveCurveTo(x2f, y2f, xf, yf)` |
| `s dx2,dy2 dx,dy` | `reflectiveCurveToRelative(dx2f, dy2f, dxf, dyf)` |
| `Q x1,y1 x,y` | `quadTo(x1f, y1f, xf, yf)` |
| `q dx1,dy1 dx,dy` | `quadToRelative(dx1f, dy1f, dxf, dyf)` |
| `T x,y` | `reflectiveQuadTo(xf, yf)` |
| `t dx,dy` | `reflectiveQuadToRelative(dxf, dyf)` |
| `A rx,ry,rot,largeArc,sweep,x,y` | `arcTo(rxf, ryf, rotf, largeArc(bool), sweep(bool), xf, yf)` |
| `a rx,ry,rot,largeArc,sweep,dx,dy` | `arcToRelative(rxf, ryf, rotf, largeArc(bool), sweep(bool), dxf, dyf)` |
| `Z` or `z` | `close()` |

**Important for Material Symbols (960x960 viewport):**
- The viewBox is `0 -960 960 960`, meaning y ranges from -960 (top) to 0 (bottom)
- For absolute y coordinates: `y_kotlin = y_svg + 960`
- Relative offsets stay unchanged
- Use `materialIcon(name = "...", viewportSize = 960f)`

**Number formatting:**
- All coordinate values must end with `f` (Kotlin float literal)
- Use the exact numeric values from the SVG (don't round)
- Comma-separated values in SVG should become separate float arguments

### 4. Create the Kotlin file

Create the file at:
```
aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/{IconName}.kt
```

Use this template for classic 24x24 icons:

```kotlin
@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.{IconName}: ImageVector by lazy {
  materialIcon(name = "{IconName}") {
    materialPath {
      // converted path commands here
    }
  }
}
```

For Material Symbols (960x960) icons:

```kotlin
@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.{IconName}: ImageVector by lazy {
  materialIcon(name = "{IconName}", viewportSize = 960f) {
    materialPath {
      // converted path commands here (with y + 960 adjustment for absolute coords)
    }
  }
}
```

If the vector drawable has multiple `<path>` elements, use multiple `materialPath { }` blocks.

If any path has `android:fillType="evenOdd"`, pass `pathFillType = PathFillType.EvenOdd` to `materialPath` and add the import `import androidx.compose.ui.graphics.PathFillType`.

### 5. Update IconPreviews.kt

Read `aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/IconPreviews.kt`.

Add the new icon name to the `materialIcons` list in **alphabetical order**.

### 6. Verify

Run `./gradlew :aktual-core:icons:compileAll` to verify the icon compiles correctly.

## Important Notes

- Check that a file with this icon name doesn't already exist before creating it
- The icon name in the file must match the PascalCase name used as the extension property name
- Always use `by lazy` for the property delegate
- Always include `@file:Suppress("UnusedReceiverParameter")` at the top
- If the fetched XML contains `android:autoMirrored="true"`, pass `autoMirror = true` to `materialIcon()`