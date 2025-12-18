package com.example.bookhive.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookhive.R
import com.example.bookhive.model.UserModel
import com.example.bookhive.repository.UserRepsitoryImpl
import com.example.bookhive.viewmodel.UserViewModel

class RegsiterView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                RegisterBody()
        }
    }
}

// --- THEME CONSTANTS (Replace these with your actual Theme colors) ---
val MainBackground = Color(0xFFF5F5F5) // Light Grey
val SecondaryBackground = Color(0xFFFFFFFF) // White
val Cyan = Color(0xFF2196F3) // Blue (Primary)
val SecondPrime = Color(0xFF757575) // Grey Text


@Composable
fun RegisterBody(userViewModel: UserViewModel? = null) { // Nullable for Preview
    // --- State ---
    val repo = remember { UserRepsitoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    // --- Validation & Errors ---
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Validation Functions
    fun validateUsername(input: String): Boolean = input.isNotBlank() && input.length >= 3
    fun validateEmail(input: String): Boolean = input.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(input).matches()
    fun validatePassword(input: String): Boolean = input.isNotBlank() && input.length >= 8
    fun validateConfirmPassword(pass: String, confirmPass: String): Boolean = pass == confirmPass && confirmPass.isNotBlank()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(MainBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(R.drawable.img), // Ensure this exists
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SecondaryBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Cyan)

                    // Helper Composable defined below (same as previous answer)
                    AuthTextField(value = username, onValueChange = { username = it }, label = "Username", error = usernameError, keyboardType = KeyboardType.Text)
                    AuthTextField(value = email, onValueChange = { email = it }, label = "Email", error = emailError, keyboardType = KeyboardType.Email)

                    AuthPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isVisible = passwordVisibility,
                        onVisibilityChange = { passwordVisibility = !passwordVisibility },
                        error = passwordError,
                        imeAction = ImeAction.Next
                    )

                    AuthPasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isVisible = confirmPasswordVisibility,
                        onVisibilityChange = { confirmPasswordVisibility = !confirmPasswordVisibility },
                        error = confirmPasswordError,
                        imeAction = ImeAction.Done
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- YOUR REQUESTED LOGIC BLOCK ---
                    Button(
                        onClick = {
                            // Validate all fields before submission
                            val usernameValid = validateUsername(username)
                            val emailValid = validateEmail(email)
                            val passwordValid = validatePassword(password)
                            val confirmPassValid = validateConfirmPassword(password, confirmPassword)

                            // Update error states
                            usernameError = if (usernameValid) null else "Username must be at least 3 characters"
                            emailError = if (emailValid) null else "Invalid email address"
                            passwordError = if (passwordValid) null else "Password must be at least 8 characters"
                            confirmPasswordError = if (confirmPassValid) null else "Passwords do not match"

                            // Submit only if all validations pass
                            if (usernameValid && emailValid && passwordValid && confirmPassValid) {
                                userViewModel.register(email, password) { success, message, userId ->
                                    if (success) {
                                        val model = UserModel(
                                            userId, username, email,
                                            "", "",false
                                        )
                                        userViewModel.addUserToDatabase(userId, model) { success, message ->
                                            if (success) {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                // Navigate to login page after successful registration
                                                val intent = Intent(context, LoginView::class.java)
                                                context.startActivity(intent)
                                                // Optional: finish current activity to prevent going back
                                                (context as? Activity)?.finish()
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = SecondPrime, fontSize = 16.sp)
                Text(
                    "Sign In", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, LoginView::class.java))
                        (context as? Activity)?.finish()
                    }
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = SecondPrime, modifier = Modifier.padding(bottom = 4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Color.Red else Cyan,
                unfocusedBorderColor = if (error != null) Color.Red else Color.LightGray,
                cursorColor = Cyan
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            singleLine = true
        )

        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    error: String?,
    imeAction: ImeAction
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = SecondPrime, modifier = Modifier.padding(bottom = 4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Color.Red else Cyan,
                unfocusedBorderColor = if (error != null) Color.Red else Color.LightGray,
                cursorColor = Cyan
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            singleLine = true,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val iconRes = if (isVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                IconButton(onClick = onVisibilityChange) {
                    Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Cyan)
                }
            }
        )

        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
