---
name: add-svg-icon
description: Convert an SVG file (local path or URL) to a Compose ImageVector in the aktual-core:icons module. Defaults to AktualIcons; use --material for MaterialIcons.
argument-hint: "<filepath-or-url> <IconName> [--material] [--size <N>]"
---

Convert an SVG file to a Kotlin `ImageVector` and add it to the `aktual-core/icons` module.

## Arguments

- `$ARGUMENTS` should contain:
  - A **file path or URL** to an SVG file
  - The **icon name** in PascalCase (e.g., `MyIcon`, `BankTransfer`)
  - `--material` (optional) — generate as a `MaterialIcons` extension instead of `AktualIcons`
  - `--size <N>` (optional) — override the icon size (default: derived from SVG viewBox)

## Step-by-Step Process

### 1. Fetch the SVG

- If the argument is a **URL**: use `WebFetch` to download the SVG content
- If the argument is a **file path**: use `Read` to read the SVG file

### 2. Parse the SVG

Extract from the SVG:
- The `viewBox` attribute (e.g., `"0 0 24 24"` or `"0 0 20 20"`)
  - Parse as `minX minY width height`
- The `d` attribute from each `<path>` element
- Any `fill-rule="evenodd"` on path elements

Determine the icon size:
- If `--size` was provided, use that value
- Otherwise, use the `width` from the viewBox (e.g., `24` from `"0 0 24 24"`)

### 3. Convert path data to Kotlin

Convert SVG path commands to Kotlin `PathBuilder` calls:

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

**Number formatting:**
- All coordinate values must end with `f` (Kotlin float literal)
- Use the exact numeric values from the SVG (don't round)
- Comma-separated values in SVG should become separate float arguments

### 4. Create the Kotlin file

**Default: AktualIcons**

Create the file at:
```
aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/{IconName}.kt
```

Template:

```kotlin
@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.{IconName}: ImageVector by lazy {
  aktualIcon(name = "{IconName}", size = {size}f) {
    aktualPath {
      // converted path commands here
    }
  }
}
```

**With `--material` flag: MaterialIcons**

Create the file at:
```
aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/material/{IconName}.kt
```

Template:

```kotlin
@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.{IconName}: ImageVector by lazy {
  materialIcon(name = "{IconName}") {
    materialPath {
      // converted path commands here
    }
  }
}
```

For MaterialIcons, the default viewport is 24f. If the SVG viewBox is not 24x24, pass `viewportSize = {size}f` to `materialIcon()`.

**Multiple paths:** If the SVG has multiple `<path>` elements, use multiple `aktualPath { }` or `materialPath { }` blocks.

**Even-odd fill rule:** If any path has `fill-rule="evenodd"`, pass `pathFillType = PathFillType.EvenOdd` to `aktualPath` or `materialPath` and add the import `import androidx.compose.ui.graphics.PathFillType`.

### 5. Update preview file

**AktualIcons (default):**
Read `aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/IconPreviews.kt`.
Add the new icon name to the `aktualIcons` list in **alphabetical order**.

**MaterialIcons (`--material`):**
Read `aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/material/MaterialIconPreviews.kt`.
Add the new icon name to the `materialIcons` list in **alphabetical order**.

### 6. Verify

Run `./gradlew :aktual-core:icons:compileAll` to verify the icon compiles correctly.

## Important Notes

- Check that a file with this icon name doesn't already exist before creating it
- The icon name in the file must match the PascalCase name used as the extension property name
- Always use `by lazy` for the property delegate
- Always include the `@file:Suppress` annotation at the top
- For AktualIcons, include both `"BooleanLiteralArgument"` and `"UnusedReceiverParameter"` suppressions
- For MaterialIcons, only `"UnusedReceiverParameter"` is needed
- AktualIcons use `aktualIcon` + `aktualPath` from `aktual.core.icons.internal`
- MaterialIcons use `materialIcon` + `materialPath` from `aktual.core.icons.material.internal`