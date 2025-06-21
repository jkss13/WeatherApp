package com.ifpe.weatherapp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        label = { Text(text = "Nome") },
        modifier = modifier.fillMaxWidth(fraction = 0.9f),
        onValueChange = onValueChange
    )
}

@Composable
fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        label = { Text(text = "E-mail") },
        modifier = modifier.fillMaxWidth(fraction = 0.9f),
        onValueChange = onValueChange
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        label = { Text(text = "Senha") },
        modifier = modifier.fillMaxWidth(fraction = 0.9f),
        onValueChange = onValueChange,
        visualTransformation = PasswordVisualTransformation()
    )
}

@Composable
fun RepeatPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        label = { Text(text = "Confirmação de senha") },
        modifier = modifier.fillMaxWidth(fraction = 0.9f),
        onValueChange = onValueChange,
        visualTransformation = PasswordVisualTransformation()
    )
}