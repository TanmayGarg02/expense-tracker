package com.example.walletmanager.ui.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.walletmanager.data.model.Currency

/** Displays comma grouping while leaving the underlying amount text unchanged. */
class AmountGroupingVisualTransformation(
    private val currency: Currency
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val integerLength = raw.indexOf('.').let { if (it < 0) raw.length else it }
        val boundaries = buildList {
            for (index in 1 until integerLength) {
                val shouldGroup = if (currency == Currency.INR) {
                    index <= integerLength - 3 && (integerLength - index - 3) % 2 == 0
                } else {
                    (integerLength - index) % 3 == 0
                }
                if (shouldGroup) add(index)
            }
        }
        val formatted = buildString(raw.length + boundaries.size) {
            raw.forEachIndexed { index, char ->
                if (index in boundaries) append(',')
                append(char)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, raw.length)
                return safeOffset + boundaries.count { it <= safeOffset }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                return (safeOffset - boundaries.count { it < safeOffset }).coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
