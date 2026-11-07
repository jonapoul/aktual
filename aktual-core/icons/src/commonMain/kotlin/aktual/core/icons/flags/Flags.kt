package aktual.core.icons.flags

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// Takes an ISO 3166 alpha-2 region code, plus "EU" for the European Union
@Composable
fun flagPainter(regionCode: String): Painter? =
  flagResource(regionCode)?.let { painterResource(it) }

internal fun flagResource(regionCode: String): DrawableResource? =
  when (regionCode.uppercase()) {
    "AE" -> Res.drawable.flag_ae
    "AR" -> Res.drawable.flag_ar
    "AU" -> Res.drawable.flag_au
    "BD" -> Res.drawable.flag_bd
    "BR" -> Res.drawable.flag_br
    "BY" -> Res.drawable.flag_by
    "CA" -> Res.drawable.flag_ca
    "CH" -> Res.drawable.flag_ch
    "CN" -> Res.drawable.flag_cn
    "CO" -> Res.drawable.flag_co
    "CR" -> Res.drawable.flag_cr
    "CZ" -> Res.drawable.flag_cz
    "DK" -> Res.drawable.flag_dk
    "EG" -> Res.drawable.flag_eg
    "EU" -> Res.drawable.flag_eu
    "GB" -> Res.drawable.flag_gb
    "GT" -> Res.drawable.flag_gt
    "HK" -> Res.drawable.flag_hk
    "HU" -> Res.drawable.flag_hu
    "ID" -> Res.drawable.flag_id
    "IN" -> Res.drawable.flag_in
    "JM" -> Res.drawable.flag_jm
    "JP" -> Res.drawable.flag_jp
    "KR" -> Res.drawable.flag_kr
    "LK" -> Res.drawable.flag_lk
    "MD" -> Res.drawable.flag_md
    "MY" -> Res.drawable.flag_my
    "NO" -> Res.drawable.flag_no
    "PH" -> Res.drawable.flag_ph
    "PL" -> Res.drawable.flag_pl
    "QA" -> Res.drawable.flag_qa
    "RO" -> Res.drawable.flag_ro
    "RS" -> Res.drawable.flag_rs
    "RU" -> Res.drawable.flag_ru
    "SA" -> Res.drawable.flag_sa
    "SE" -> Res.drawable.flag_se
    "SG" -> Res.drawable.flag_sg
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
