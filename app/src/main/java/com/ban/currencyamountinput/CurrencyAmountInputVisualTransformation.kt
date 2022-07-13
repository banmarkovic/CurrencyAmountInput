package com.ban.currencyamountinput

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.lang.Integer.max
import java.text.DecimalFormat

class CurrencyAmountInputVisualTransformation : VisualTransformation {

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

        val offsetMapping = ThousandSeparatorOffsetMapping(
            originalIntegerLength = intPart.length
        )

        return TransformedText(newText, offsetMapping)
    }

    private inner class ThousandSeparatorOffsetMapping(
        val originalIntegerLength: Int
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when (offset) {
                0, 1, 2 -> 4
                else -> offset + 1 + calculateThousandsSeparatorCount(originalIntegerLength)
            }

        override fun transformedToOriginal(offset: Int): Int =
            originalIntegerLength + calculateThousandsSeparatorCount(originalIntegerLength) + 2

        private fun calculateThousandsSeparatorCount(intDigitCount: Int) =
            max((intDigitCount - 1) / 3, 0)
    }
}
