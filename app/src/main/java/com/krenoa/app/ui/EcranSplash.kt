package com.krenoa.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.krenoa.app.R
import kotlinx.coroutines.delay

private val NoirEbene = Color(0xFF0A0812)
private val VioletKrenoa = Color(0xFF8B6CFF)
private val OrKrenoa = Color(0xFFFF9E3D)

@Composable
fun EcranSplash(surChargementTermine: () -> Unit) {
    val progression = remember { Animatable(0f) }
    val policeManuscrite = FontFamily(Font(R.font.dancing_script, FontWeight.Bold))

    LaunchedEffect(Unit) {
        progression.animateTo(1f, animationSpec = tween(2500))
        delay(300)
        surChargementTermine()
    }

    Box(modifier = Modifier.fillMaxSize().background(NoirEbene)) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
        ) {
            val l = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD9A0), OrKrenoa, Color(0xFF7B4CD6), NoirEbene),
                    center = Offset(l / 2f, h),
                    radius = h * 1.5f
                )
            )
            drawCircle(
                color = Color(0xFFFFEBC7).copy(alpha = 0.9f),
                radius = h * 0.13f,
                center = Offset(l / 2f, h * 0.4f)
            )
            val montagneFond = Path().apply {
                moveTo(0f, h * 0.62f); lineTo(l * 0.2f, h * 0.42f); lineTo(l * 0.38f, h * 0.55f)
                lineTo(l * 0.5f, h * 0.35f); lineTo(l * 0.65f, h * 0.5f); lineTo(l * 0.85f, h * 0.4f)
                lineTo(l, h * 0.55f); lineTo(l, h); lineTo(0f, h); close()
            }
            drawPath(montagneFond, color = Color(0xFF150E22))
            val montagneAvant = Path().apply {
                moveTo(0f, h * 0.78f); lineTo(l * 0.25f, h * 0.6f); lineTo(l * 0.45f, h * 0.72f)
                lineTo(l * 0.5f, h * 0.6f); lineTo(l * 0.7f, h * 0.7f); lineTo(l, h * 0.6f)
                lineTo(l, h); lineTo(0f, h); close()
            }
            drawPath(montagneAvant, color = NoirEbene)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoKrenoa()
            Spacer(modifier = Modifier.height(18.dp))
            Text("KRENOA", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp, color = Color(0xFFF2EFFF))
            Spacer(modifier = Modifier.height(12.dp))
            Text("ORGANISE AUJOURD'HUI,", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = Color(0xFFD8D2EE))
            Row {
                Text("CONSTRUIS ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = Color(0xFFD8D2EE))
                Text("DEMAIN.", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = OrKrenoa)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(50.dp).height(1.dp).background(VioletKrenoa.copy(alpha = 0.6f)))
                Box(modifier = Modifier.padding(horizontal = 6.dp).size(4.dp).clip(RoundedCornerShape(50)).background(OrKrenoa))
                Box(modifier = Modifier.width(50.dp).height(1.dp).background(VioletKrenoa.copy(alpha = 0.6f)))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Une conception originale de", fontSize = 12.sp, color = Color(0xFFE8E1D8))
            Text("Hama Adama.", fontFamily = policeManuscrite, fontSize = 26.sp, color = Color(0xFFFFC978))
            Spacer(modifier = Modifier.height(22.dp))
            Box(
                modifier = Modifier.width(120.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2A2438))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(progression.value).fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(VioletKrenoa, OrKrenoa, Color.White)))
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Chargement de Krenoa...", fontSize = 11.sp, color = Color(0xFF9A93B5))
        }
    }
}

@Composable
private fun LogoKrenoa() {
    Canvas(modifier = Modifier.size(140.dp)) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            brush = Brush.linearGradient(listOf(VioletKrenoa, OrKrenoa)),
            radius = size.minDimension / 2.15f,
            center = centre,
            style = Stroke(width = 3f)
        )
        drawCircle(Color.White, radius = 3f, center = Offset(size.width * 0.8f, size.height * 0.16f))
        drawCircle(Color.White, radius = 2.5f, center = Offset(size.width * 0.92f, size.height * 0.36f))
        drawCircle(Color.White, radius = 2.5f, center = Offset(size.width * 0.12f, size.height * 0.7f))

        val k = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.26f)
            lineTo(size.width * 0.38f, size.height * 0.74f)
            lineTo(size.width * 0.46f, size.height * 0.74f)
            lineTo(size.width * 0.46f, size.height * 0.54f)
            lineTo(size.width * 0.63f, size.height * 0.74f)
            lineTo(size.width * 0.74f, size.height * 0.74f)
            lineTo(size.width * 0.53f, size.height * 0.49f)
            lineTo(size.width * 0.72f, size.height * 0.26f)
            lineTo(size.width * 0.61f, size.height * 0.26f)
            lineTo(size.width * 0.46f, size.height * 0.44f)
            lineTo(size.width * 0.46f, size.height * 0.26f)
            close()
        }
        drawPath(k, brush = Brush.linearGradient(listOf(VioletKrenoa, OrKrenoa)))

        val fleche = Path().apply {
            moveTo(size.width * 0.59f, size.height * 0.30f)
            lineTo(size.width * 0.80f, size.height * 0.17f)
            lineTo(size.width * 0.78f, size.height * 0.28f)
        }
        drawPath(
            fleche,
            brush = Brush.linearGradient(listOf(VioletKrenoa, OrKrenoa)),
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
