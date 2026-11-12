package aktual.core.icons.flags

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// Takes an ISO 3166 alpha-2 region code, plus "EU" for the European Union
@Composable
fun flagPainter(regionCode: String): Painter? =
  flagResource(regionCode)?.let { painterResource(it) }

// Leaves the space empty when there's no flag for the region, so rows still line up
@Composable
fun Flag(regionCode: String?, modifier: Modifier = Modifier) =
  Box(modifier = modifier.size(FLAG_WIDTH, FLAG_HEIGHT), contentAlignment = Center) {
    val painter = regionCode?.let { flagPainter(it) }
    if (painter != null) {
      Image(
        painter = painter,
        contentDescription = null,
        contentScale = Crop,
        modifier = Modifier.fillMaxSize().clip(FLAG_SHAPE),
      )
    }
  }

private val FLAG_SHAPE = RoundedCornerShape(2.dp)
private val FLAG_WIDTH = 24.dp
private val FLAG_HEIGHT = 18.dp

internal fun flagResource(regionCode: String): DrawableResource? =
  when (regionCode.uppercase()) {
    "AE" -> Res.drawable.flag_ae
    "AR" -> Res.drawable.flag_ar
    "AT" -> Res.drawable.flag_at
    "AU" -> Res.drawable.flag_au
    "BD" -> Res.drawable.flag_bd
    "BE" -> Res.drawable.flag_be
    "BG" -> Res.drawable.flag_bg
    "BR" -> Res.drawable.flag_br
    "BY" -> Res.drawable.flag_by
    "CA" -> Res.drawable.flag_ca
    "CH" -> Res.drawable.flag_ch
    "CN" -> Res.drawable.flag_cn
    "CO" -> Res.drawable.flag_co
    "CR" -> Res.drawable.flag_cr
    "CY" -> Res.drawable.flag_cy
    "CZ" -> Res.drawable.flag_cz
    "DE" -> Res.drawable.flag_de
    "DK" -> Res.drawable.flag_dk
    "EE" -> Res.drawable.flag_ee
    "EG" -> Res.drawable.flag_eg
    "ES" -> Res.drawable.flag_es
    "EU" -> Res.drawable.flag_eu
    "FI" -> Res.drawable.flag_fi
    "FR" -> Res.drawable.flag_fr
    "GB" -> Res.drawable.flag_gb
    "GR" -> Res.drawable.flag_gr
    "GT" -> Res.drawable.flag_gt
    "HK" -> Res.drawable.flag_hk
    "HR" -> Res.drawable.flag_hr
    "HU" -> Res.drawable.flag_hu
    "ID" -> Res.drawable.flag_id
    "IE" -> Res.drawable.flag_ie
    "IN" -> Res.drawable.flag_in
    "IS" -> Res.drawable.flag_is
    "IT" -> Res.drawable.flag_it
    "JM" -> Res.drawable.flag_jm
    "JP" -> Res.drawable.flag_jp
    "KR" -> Res.drawable.flag_kr
    "LI" -> Res.drawable.flag_li
    "LK" -> Res.drawable.flag_lk
    "LT" -> Res.drawable.flag_lt
    "LU" -> Res.drawable.flag_lu
    "LV" -> Res.drawable.flag_lv
    "MD" -> Res.drawable.flag_md
    "MT" -> Res.drawable.flag_mt
    "MY" -> Res.drawable.flag_my
    "NL" -> Res.drawable.flag_nl
    "NO" -> Res.drawable.flag_no
    "PH" -> Res.drawable.flag_ph
    "PL" -> Res.drawable.flag_pl
    "PT" -> Res.drawable.flag_pt
    "QA" -> Res.drawable.flag_qa
    "RO" -> Res.drawable.flag_ro
    "RS" -> Res.drawable.flag_rs
    "RU" -> Res.drawable.flag_ru
    "SA" -> Res.drawable.flag_sa
    "SE" -> Res.drawable.flag_se
    "SG" -> Res.drawable.flag_sg
    "SI" -> Res.drawable.flag_si
    "SK" -> Res.drawable.flag_sk
    "TH" -> Res.drawable.flag_th
    "TR" -> Res.drawable.flag_tr
    "TZ" -> Res.drawable.flag_tz
    "UA" -> Res.drawable.flag_ua
    "US" -> Res.drawable.flag_us
    "UZ" -> Res.drawable.flag_uz
    "VN" -> Res.drawable.flag_vn
    "ZA" -> Res.drawable.flag_za
    else -> null
  }
