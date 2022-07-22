package com.ban.currencyamountinput

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.lang.Integer.max
import java.text.DecimalFormat

class CurrencyAmountInputVisualTransformation(
    private val fixedCursorAtTheEnd: Boolean = true
) : VisualTransformation {

    companion object {
        const val CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS = 2
    }

    private val symbols = DecimalFormat().decimalFormatSymbols
    private val thousandsReplacementPattern = Regex("\\B(?=(?:\\d{3})+(?!\\d))")

    override fun filter(text: AnnotatedString): TransformedText {
        val thousandsSeparator = symbols.groupingSeparator
        val decimalSeparator = symbols.decimalSeparator
        val zero = symbols.zeroDigit

        val inputText = text.text

        val intPart = if (inputText.length > CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS) {
            inputText.subSequence(0, inputText.length - CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS)
        } else {
            zero.toString()
        }
        var fractionPart = if (inputText.length >= CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS) {
            inputText.subSequence(
                inputText.length - CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS,
                inputText.length
            )
        } else {
            inputText
        }

        val formattedIntWithThousandsSeparator =
            intPart.replace(thousandsReplacementPattern, thousandsSeparator.toString())

        if (fractionPart.length < CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS) {
            fractionPart = fractionPart.padStart(CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS, zero)
        }

        val newText = AnnotatedString(
            formattedIntWithThousandsSeparator + decimalSeparator + fractionPart,
            text.spanStyles,
            text.paragraphStyles
        )

        val offsetMapping = if (fixedCursorAtTheEnd) {
            FixedCursorOffsetMapping(
                unmaskedTextLength = intPart.length,
                decimalDigits = CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS
            )
        } else {
            MovableCursorOffsetMapping(
                unmaskedText = text.toString(),
                maskedText = newText.toString(),
                decimalDigits = CURRENCY_AMOUNT_FORMAT_NUMBER_OF_DECIMALS
            )
        }

        return TransformedText(newText, offsetMapping)
    }

    private inner class FixedCursorOffsetMapping(
        private val unmaskedTextLength: Int,
        private val decimalDigits: Int
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when (offset) {
                0, 1, 2 -> 4
                else -> offset + 1 + calculateThousandsSeparatorCount(unmaskedTextLength)
            }

        override fun transformedToOriginal(offset: Int): Int =
            unmaskedTextLength + calculateThousandsSeparatorCount(unmaskedTextLength) + decimalDigits

        private fun calculateThousandsSeparatorCount(unmaskedTextLength: Int) =
            max((unmaskedTextLength - 1) / 3, 0)
    }

    private inner class MovableCursorOffsetMapping(
        private val unmaskedText: String,
        private val maskedText: String,
        private val decimalDigits: Int
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when {
                unmaskedText.length <= decimalDigits -> {
                    maskedText.length - (unmaskedText.length - offset)
                }
                else -> {
                    offset + offsetMaskCount(offset, maskedText)
                }
            }

        override fun transformedToOriginal(offset: Int): Int =
            when {
                unmaskedText.length <= decimalDigits -> {
                    max(unmaskedText.length - (maskedText.length - offset), 0)
                }
                else -> {
                    offset - maskedText.take(offset).count { !it.isDigit() }
                }
            }

        private fun offsetMaskCount(offset: Int, maskedText: String): Int {
            var maskOffsetCount = 0
            var dataCount = 0
            for (maskChar in maskedText) {
                if (!maskChar.isDigit()) {
                    maskOffsetCount++
                } else if (++dataCount > offset) {
                    break
                }
            }
            return maskOffsetCount
        }
    }
}
