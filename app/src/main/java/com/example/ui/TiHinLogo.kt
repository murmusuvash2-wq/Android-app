package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.DeepForest
import com.example.ui.theme.EditorialSerif

@Composable
fun TiHinLogo(
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit = 32.sp,
    showSparkle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size((textSize.value * 1.05f).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size((textSize.value * 0.95f).dp)) {
                val w = size.width
                val h = size.height
                val stroke = Stroke(
                    width = (w * 0.075f).coerceAtLeast(2.dp.toPx()),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                val hanger = Path().apply {
                    moveTo(w * 0.50f, h * 0.25f)
                    cubicTo(w * 0.43f, h * 0.12f, w * 0.48f, h * 0.05f, w * 0.57f, h * 0.08f)
                    cubicTo(w * 0.69f, h * 0.12f, w * 0.66f, h * 0.23f, w * 0.54f, h * 0.28f)
                    lineTo(w * 0.14f, h * 0.62f)
                    cubicTo(w * 0.10f, h * 0.66f, w * 0.13f, h * 0.72f, w * 0.20f, h * 0.75f)
                    lineTo(w * 0.47f, h * 0.87f)
                    lineTo(w * 0.80f, h * 0.72f)
                    cubicTo(w * 0.87f, h * 0.69f, w * 0.89f, h * 0.64f, w * 0.83f, h * 0.61f)
                    lineTo(w * 0.58f, h * 0.47f)
                }
                drawPath(hanger, color = DeepForest, style = stroke)
            }
            if (showSparkle) {
                Canvas(modifier = Modifier.size((textSize.value * 0.9f).dp)) {
                    val p = Path().apply {
                        moveTo(size.width * 0.80f, size.height * 0.18f)
                        lineTo(size.width * 0.84f, size.height * 0.30f)
                        lineTo(size.width * 0.96f, size.height * 0.34f)
                        lineTo(size.width * 0.84f, size.height * 0.38f)
                        lineTo(size.width * 0.80f, size.height * 0.50f)
                        lineTo(size.width * 0.76f, size.height * 0.38f)
                        lineTo(size.width * 0.64f, size.height * 0.34f)
                        lineTo(size.width * 0.76f, size.height * 0.30f)
                        close()
                    }
                    drawPath(p, color = Color(0xFFC9A961))
                }
            }
        }
        Spacer(Modifier.width(2.dp))
        androidx.compose.material3.Text(
            text = "TiHin",
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Bold,
            fontSize = textSize,
            color = DeepForest,
            letterSpacing = (-0.6).sp
        )
    }
}
