package com.ban.currencyamountinput

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ban.currencyamountinput.ui.theme.CurrencyAmountInputTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyAmountInputTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(color = MaterialTheme.colors.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var text1 by remember { mutableStateOf("") }
                    TextField(
                        modifier = Modifier.padding(bottom = 24.dp),
                        value = text1,
                        onValueChange = {
                            text1 = if (it.startsWith("0")) {
                                ""
                            } else {
                                it
                            }
                        },
                        label = { Text("Currency Amount (cursor fixed)") },
                        visualTransformation = CurrencyAmountInputVisualTransformation(
                            fixedCursorAtTheEnd = true
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                    )
                    var text2 by remember { mutableStateOf("") }
                    TextField(
                        value = text2,
                        onValueChange = {
                            text2 = if (it.startsWith("0")) {
                                ""
                            } else {
                                it
                            }
                        },
                        label = { Text("Currency Amount") },
                        visualTransformation = CurrencyAmountInputVisualTransformation(
                            fixedCursorAtTheEnd = false
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                    )
                }
            }
        }
    }
}
