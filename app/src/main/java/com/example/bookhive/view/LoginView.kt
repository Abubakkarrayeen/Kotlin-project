package com.example.bookhive.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.bookhive.R
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookhive.repository.UserRepsitoryImpl
import com.example.bookhive.viewmodel.UserViewModel

class LoginView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    // State for inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // ViewModel and Repository
    val repo = remember { UserRepsitoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }

    val context = LocalContext.current
    val activity = context as Activity
    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    // Load saved credentials from SharedPreferences
    val localEmail: String = sharedPreferences.getString("email", "").toString()
    val localPassword: String = sharedPreferences.getString("password", "").toString()

    // Pre-fill email and password if saved
    LaunchedEffect(Unit) {
        email = localEmail
        password = localPassword
    }

    // Theme Colors
    val primaryBlue = Color(0xFF2196F3)
    val whiteBackground = Color.White

    // Root container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBackground)
            .padding(24.dp)
    ) {
        // Main Content - Centered and Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryBlue,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    focusedLabelColor = primaryBlue,
                    cursorColor = primaryBlue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    val iconRes = if (isPasswordVisible)
                        R.drawable.baseline_visibility_24
                    else
                        R.drawable.baseline_visibility_off_24

                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Toggle Password Visibility",
                            tint = primaryBlue
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    focusedLabelColor = primaryBlue,
                    cursorColor = primaryBlue
                )
            )

            // Forgot Password Link
            Text(
                text = "Forgot Password?",
                color = primaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
                    .clickable {
                        val intent = Intent(context, ForgetPasswordView::class.java)
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button with Firebase Logic
            Button(
                onClick = {
                    userViewModel.login(email, password) { success, message ->
                        if (success) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                            // Navigate to Dashboard
                            val intent = Intent(context, DashboardActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Login", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }

        // Footer - Sign Up Link
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account yet? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Sign up",
                color = primaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = {
                    val intent = Intent(context, RegsiterView::class.java)
                    context.startActivity(intent)
                })
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}