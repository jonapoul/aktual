# aktual-core:icons

Icon definitions as `ImageVector` properties, split into two families:

- **`AktualIcons`** (package `aktual.core.icons`) - custom SVG icons. Built with `aktualIcon()` + `aktualPath {}`. For square icons `size` is the SVG viewBox size, and the icon always renders at 24dp to match Material. Non-square icons pass `width`/`height`, which are used as both viewport and dp size. Square icons whose paths fill the whole viewBox pass `padded = true`, which adds Material's 2dp keyline so they don't look bigger than Material icons.
- **`MaterialIcons`** (package `aktual.core.icons.material`) - Material Design icons. Built with `materialIcon()` + `materialPath {}`; 24dp, or 960-unit viewport for Material Symbols.

## Check upstream first

Before adding or picking an icon, check whether upstream Actual has one for the same purpose, and prefer it over a Material icon. Upstream SVGs live in `/home/jon/dev/actual/packages/component-library/src/icons/{v0,v1,v2}/`. To see which one Actual uses for a given action, grep `packages/desktop-client/src` for `Svg<Name>` (e.g. delete uses `SvgTrash`, settings uses `SvgCog`). Add it with `/add-svg-icon` as an `AktualIcons` entry. Only fall back to a Material icon when upstream has nothing suitable.

## Adding icons

Prefer the `/add-svg-icon` and `/add-material-icon` skills - they handle SVG → `ImageVector` conversion. The notes below are for when you need to do it by hand.

## File shape

Each icon is its own file. Example - for `AktualIcons`:

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

Create the file in the family's package, then append to the alphabetically-sorted list in the matching preview file (`AktualIconPreviews.kt` → `aktualIcons`, or `material/MaterialIconPreviews.kt` → `materialIcons`). The `@PreviewParameter` renders the whole list in Android Studio.

## Flags

Country flags are vector drawables in `src/commonMain/composeResources/drawable/flag_<region>.xml`. Show one with `Flag(regionCode)` (or get the painter with `flagPainter(regionCode)`) in `aktual.core.icons.flags`, and add a branch to `flagResource` when adding a flag. Only the flags the app uses are bundled.

They come from the large (32x24) set of [Flagpack](https://github.com/Yummygum/flagpack-core) (`svg/l/`), with some exceptions:
- `sa` and `ar` use the small set (`svg/s/`), since the large ones are over 100KB each.
- `gb`, the Union Jack in `au`, and the taegeuk in `kr` are generated from the official geometry, since Flagpack's are inaccurate. They keep Flagpack's colours, and `au` and `kr` keep its stars and trigrams.
- `hu` uses the official red `#CE2939`, since Flagpack's looks orange.
- `eu` is drawn by hand, since Flagpack has none.

Convert with `npx svg2vectordrawable`, but prepare the SVG first, since the converter has gaps:
- It turns named colours (`white`, `black`, `red`) black and ignores `style="fill:..."`. Use hex attributes.
- It ignores `clip-path`, and only turns a `<mask>` into a clip when the mask is top-level (not in `<defs>`), defined before its use, and made of `<path>`s rather than `<rect>`s. Rewrite each `<clipPath>` as a `<mask>` that meets those rules.
- It ignores `transform` on rects. Bake translations into `x`/`y`.
- Flagpack's "outside" stroke masks only show the outer half of a stroke. Drop the mask and draw the stroke before the fill it outlines (e.g. South Africa, Liechtenstein).

Render the result and compare it with the source before committing.
