package jo.aliftaa.prayertimes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jo.aliftaa.prayertimes.R

val FaSolidFontFamily = FontFamily(
    Font(R.font.fa_solid_900)
)

object FaIcons {
    const val CloudMoon = "\uF6C3"
    const val Sun = "\uF185"
    const val CloudSun = "\uF6C4"
    const val Moon = "\uF186"
    const val StarAndCrescent = "\uF699"
    const val TriangleExclamation = "\uF071"
    const val BatteryHalf = "\uF242"
}

@Composable
fun FaIcon(
    glyph: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: TextUnit = 18.sp,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    shape: Shape = RoundedCornerShape(12.dp),
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = true),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            fontFamily = FaSolidFontFamily,
            fontSize = iconSize,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}
