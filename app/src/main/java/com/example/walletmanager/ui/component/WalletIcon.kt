package com.example.walletmanager.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

/** Small, consistent outline icons inspired by Nothing's monochrome interface. */
@Composable
fun WalletIcon(
    name: String,
    modifier: Modifier = Modifier,
    color: Color = WalletColors.cream,
    size: Dp = 20.dp
) {
    Canvas(modifier.size(size)) {
        val unit = this.size.minDimension / 24f
        val stroke = 1.7f * unit
        fun point(x: Float, y: Float) = Offset(x * this.size.width / 24f, y * this.size.height / 24f)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, point(x1, y1), point(x2, y2), stroke, cap = StrokeCap.Round)
        }
        fun path(vararg coords: Float) {
            val p = Path().apply {
                moveTo(point(coords[0], coords[1]).x, point(coords[0], coords[1]).y)
                var i = 2
                while (i < coords.size) {
                    val next = point(coords[i], coords[i + 1])
                    lineTo(next.x, next.y)
                    i += 2
                }
            }
            drawPath(p, color, style = Stroke(stroke, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        fun circle(x: Float, y: Float, r: Float) {
            drawCircle(color, r * unit, point(x, y), style = Stroke(stroke))
        }

        when (name) {
            "home" -> { path(3f, 10f, 12f, 3f, 21f, 10f); path(5f, 9f, 5f, 21f, 19f, 21f, 19f, 9f); line(10f, 21f, 10f, 14f); line(14f, 14f, 14f, 21f) }
            "accounts", "wallet" -> { path(3f, 6f, 21f, 6f, 21f, 20f, 3f, 20f, 3f, 6f); path(3f, 6f, 5f, 4f, 20f, 4f); line(15f, 13f, 21f, 13f); circle(17f, 13f, .55f) }
            "stats" -> { line(4f, 20f, 20f, 20f); path(6f, 17f, 6f, 12f); path(12f, 17f, 12f, 7f); path(18f, 17f, 18f, 4f) }
            "investments", "chart" -> { path(3f, 18f, 8f, 13f, 12f, 16f, 20f, 7f); path(15f, 7f, 20f, 7f, 20f, 12f) }
            "more" -> { circle(5f, 12f, .8f); circle(12f, 12f, .8f); circle(19f, 12f, .8f) }
            "calendar" -> { path(4f, 6f, 20f, 6f, 20f, 21f, 4f, 21f, 4f, 6f); line(8f, 3f, 8f, 8f); line(16f, 3f, 16f, 8f); line(4f, 10f, 20f, 10f); line(8f, 14f, 8f, 14f); line(12f, 14f, 12f, 14f); line(16f, 14f, 16f, 14f) }
            "plans" -> { path(5f, 5f, 19f, 5f, 19f, 20f, 5f, 20f, 5f, 5f); path(8f, 10f, 9f, 11f, 11f, 8f); line(13f, 10f, 16f, 10f); path(8f, 16f, 9f, 17f, 11f, 14f); line(13f, 16f, 16f, 16f) }
            "expense" -> { line(12f, 4f, 12f, 19f); path(6f, 13f, 12f, 19f, 18f, 13f) }
            "income" -> { line(12f, 20f, 12f, 5f); path(6f, 11f, 12f, 5f, 18f, 11f) }
            "transfer", "currency" -> { path(4f, 8f, 19f, 8f, 16f, 5f); path(20f, 16f, 5f, 16f, 8f, 19f) }
            "profile" -> { circle(12f, 8f, 3.2f); path(4f, 21f, 5f, 18f, 8f, 15.5f, 12f, 15f, 16f, 15.5f, 19f, 18f, 20f, 21f) }
            "notes" -> { path(5f, 3.5f, 16f, 3.5f, 20f, 7.5f, 20f, 20.5f, 5f, 20.5f, 5f, 3.5f); path(16f, 4f, 16f, 8f, 20f, 8f); line(8f, 12f, 17f, 12f); line(8f, 16f, 17f, 16f) }
            "reminder" -> { path(5f, 17f, 7f, 14f, 7f, 10f, 8f, 6f, 12f, 4f, 16f, 6f, 17f, 10f, 17f, 14f, 19f, 17f, 5f, 17f); path(10f, 20f, 12f, 21f, 14f, 20f) }
            "delete" -> { path(5f, 7f, 19f, 7f); path(9f, 4f, 15f, 4f); path(7f, 7f, 8f, 20f, 16f, 20f, 17f, 7f); line(10f, 10f, 10.5f, 17f); line(14f, 10f, 13.5f, 17f) }
            "category" -> { path(4f, 4f, 10f, 4f, 10f, 10f, 4f, 10f, 4f, 4f); path(14f, 4f, 20f, 4f, 20f, 10f, 14f, 10f, 14f, 4f); path(4f, 14f, 10f, 14f, 10f, 20f, 4f, 20f, 4f, 14f); path(14f, 14f, 20f, 14f, 20f, 20f, 14f, 20f, 14f, 14f) }
            "food" -> { line(7f, 3f, 7f, 10f); line(4.5f, 3f, 4.5f, 7f); line(9.5f, 3f, 9.5f, 7f); path(4.5f, 7f, 7f, 10f, 9.5f, 7f); line(7f, 10f, 7f, 21f); path(16f, 21f, 16f, 12f, 18.5f, 8f, 18.5f, 3f) }
            "people" -> { circle(9f, 8f, 2.7f); circle(17f, 9f, 2.2f); path(3f, 20f, 4f, 17f, 7f, 15f, 11f, 15f, 14f, 18f, 15f, 20f); path(15f, 15.5f, 18f, 15f, 21f, 17f, 22f, 20f) }
            "pets" -> { circle(6f, 8f, 1.25f); circle(11f, 5.5f, 1.25f); circle(17f, 6.5f, 1.25f); circle(20f, 11f, 1.25f); path(7f, 17f, 8f, 13f, 12f, 11f, 16f, 12f, 18f, 16f, 15f, 19f, 11f, 20f, 8f, 19f, 7f, 17f) }
            "transport" -> { path(3f, 15f, 5f, 9f, 8f, 7f, 16f, 7f, 19f, 9f, 21f, 15f, 21f, 18f, 3f, 18f, 3f, 15f); circle(7f, 18f, 1f); circle(17f, 18f, 1f) }
            "culture" -> { path(4f, 4f, 20f, 4f, 20f, 20f, 4f, 20f, 4f, 4f); circle(9f, 9f, 1.2f); path(5f, 17f, 10f, 12f, 13f, 15f, 16f, 12f, 19f, 16f) }
            "shopping" -> { path(4f, 8f, 20f, 8f, 19f, 21f, 5f, 21f, 4f, 8f); path(8f, 9f, 8f, 6f, 10f, 3f, 14f, 3f, 16f, 6f, 16f, 9f) }
            "health" -> { path(4f, 4f, 20f, 4f, 20f, 20f, 4f, 20f, 4f, 4f); path(10f, 7f, 14f, 7f, 14f, 10f, 17f, 10f, 17f, 14f, 14f, 14f, 14f, 17f, 10f, 17f, 10f, 14f, 7f, 14f, 7f, 10f, 10f, 10f, 10f, 7f) }
            "shield" -> { path(12f, 3f, 20f, 6f, 19f, 13f, 16f, 18f, 12f, 21f, 8f, 18f, 5f, 13f, 4f, 6f, 12f, 3f); path(9f, 12f, 11f, 14f, 15f, 10f) }
            "coin" -> { circle(12f, 12f, 8.5f); circle(12f, 12f, 5.5f); line(12f, 8f, 12f, 16f); path(14f, 9f, 12f, 8f, 10f, 9f, 10f, 11f, 14f, 13f, 14f, 15f, 12f, 16f, 10f, 15f) }
            "chevron_down" -> path(6f, 9f, 12f, 15f, 18f, 9f)
            "chevron_up" -> path(6f, 15f, 12f, 9f, 18f, 15f)
            "bills" -> { path(13f, 3f, 6f, 13f, 11f, 13f, 9f, 21f, 18f, 10f, 13f, 10f, 13f, 3f) }
            else -> { circle(12f, 12f, 7f); circle(12f, 12f, 1.2f) }
        }
    }
}

fun categoryIconName(name: String): String = when (name.substringBefore(" · ").lowercase()) {
    "food" -> "food"
    "social life", "social" -> "people"
    "pets" -> "pets"
    "transport", "travel" -> "transport"
    "culture", "entertainment" -> "culture"
    "household" -> "home"
    "education" -> "notes"
    "subscriptions" -> "calendar"
    "shopping" -> "shopping"
    "health" -> "health"
    "bills" -> "bills"
    "investments" -> "chart"
    "income", "salary" -> "income"
    "cashback" -> "transfer"
    else -> "category"
}
