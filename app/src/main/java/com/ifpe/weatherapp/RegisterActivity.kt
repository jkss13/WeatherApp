package com.ifpe.weatherapp

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ifpe.weatherapp.db.fb.FBDatabase
import com.ifpe.weatherapp.db.fb.toFBUser
import com.ifpe.weatherapp.model.User
import com.ifpe.weatherapp.ui.theme.WeatherAppTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterPage(
//                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPage(modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }

    val activity = LocalContext.current as? Activity
    Column(
        modifier = modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro",
            fontSize = 24.sp
        )

        Spacer(modifier = modifier.size(24.dp))

        NameField(value = name, onValueChange = { name = it })

        EmailField(value = email, onValueChange = { email = it })

        PasswordField(value = password, onValueChange = { password = it })

        RepeatPasswordField(value = repeatPassword, onValueChange = { repeatPassword = it })

        Spacer(modifier = modifier.size(24.dp))

        Row(modifier = modifier) {
            Button(
                onClick = {
                    Firebase.auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity!!) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity,
                                    "Registro OK!", Toast.LENGTH_LONG).show()
                                FBDatabase().register(User(name, email).toFBUser())
//                                activity.finish()
                            } else {
                                Toast.makeText(activity,
                                    "Registro FALHOU!", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty() && password == repeatPassword
            ) {
                Text(text = "Confirmar Cadastro")
            }
        }

        Row(modifier = modifier) {
            Button(
                onClick = {
                    activity?.finish()
                }
            ) {
                Text(text = "Voltar")
            }
        }
    }
}