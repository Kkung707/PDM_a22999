package com.example.ecommerce.repositories

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.ui.Cream
import com.example.ecommerce.ui.CreamLight
import com.example.ecommerce.ui.Ebony
import com.example.ecommerce.ui.EbonyLight
import com.example.ecommerce.ui.Montserrat
import com.example.ecommerce.ui.MyApplicationTheme
import com.example.ecommerce.ui.Sage
import com.example.ecommerce.ui.SageDark
import com.example.ecommerce.ui.SageLight
import com.example.ecommerce.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : ComponentActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SignUpScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SignUpScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
         val context= LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EbonyLight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Cream,
                    textDecoration = TextDecoration.Underline
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Sage) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = CreamLight,
                        cursorColor = Ebony,
                        focusedBorderColor = Sage,
                        unfocusedBorderColor = Sage,
                        focusedLabelColor = Sage,
                        unfocusedLabelColor = Sage,
                        unfocusedTextColor = Sage,
                        focusedTextColor = SageDark,
                        focusedSupportingTextColor = SageDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Sage) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = CreamLight,
                        cursorColor = Ebony,
                        focusedBorderColor = Sage,
                        unfocusedBorderColor = Sage,
                        focusedLabelColor = Sage,
                        unfocusedLabelColor = Sage,
                        unfocusedTextColor = Sage,
                        focusedTextColor = SageDark,
                        focusedSupportingTextColor = SageDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Sage) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = CreamLight,
                        cursorColor = Ebony,
                        focusedBorderColor = Sage,
                        unfocusedBorderColor = Sage,
                        focusedLabelColor = Sage,
                        unfocusedLabelColor = Sage,
                        unfocusedTextColor = Sage,
                        focusedTextColor = SageDark,
                        focusedSupportingTextColor = SageDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }

                        println("password $password")
                        println("email $email")
                        isLoading = true
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = firebaseAuth.currentUser
                                    val userData = hashMapOf(
                                        "email" to user?.email,
                                        "uid" to user?.uid
                                    )
                                    user?.uid?.let {
                                        firestore.collection("users").document(it).set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "User registered successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Error saving user data: $e",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    val errorCode = task.exception?.localizedMessage ?: "Unknown error"
                                    errorMessage = "Registration failed: $errorCode"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream,
                        disabledContainerColor = Sage,
                        disabledContentColor = Cream
                    ),
                    enabled = email.isNotBlank() && password.isNotBlank() && password == confirmPassword
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign Up",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch to Login screen
                TextButton(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = SageLight  // or something else from your palette
                    )
                ) {
                    Text(
                        text = "Already have an account? Log In",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewSignUpScreen() {
        MyApplicationTheme {
            SignUpScreen()
        }
    }
}