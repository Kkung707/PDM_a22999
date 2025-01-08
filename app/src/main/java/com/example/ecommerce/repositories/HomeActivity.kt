package com.example.ecommerce.repositories

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecommerce.ui.MyApplicationTheme
import android.content.Context
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecommerce.ui.login.LoginActivity
import androidx.compose.ui.unit.sp
import com.example.ecommerce.ui.Article.ManageArticlesActivity
import com.example.ecommerce.ui.Cream
import com.example.ecommerce.ui.CreamLight
import com.example.ecommerce.ui.Ebony
import com.example.ecommerce.ui.EbonyLight
import com.example.ecommerce.ui.Montserrat
import com.example.ecommerce.ui.MyApplicationTheme
import com.example.ecommerce.ui.Sage
import com.example.ecommerce.ui.SageDark
import com.example.ecommerce.ui.SageLight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        LocalContext.current,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(context: Context, modifier: Modifier = Modifier) {
    var userEmail by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        userEmail = currentUser.email ?: "No Email"
        isLoggedIn = true
    }

    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val role = doc.getString("role") ?: ""
                        isAdmin = (role == "ADMIN")
                    }
                }
                .addOnFailureListener { e ->
                }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ebony)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp,40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = if (isLoggedIn) "Welcome, $userEmail" else "Please log in",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Cream,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(context, ReceivedCartItemsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage,
                    contentColor = Cream
                )
            ) {
                Text(
                    text = "Received Cart",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoggedIn) {
                Button(
                    onClick = {
                        val intent = Intent(context, ShoppingCartActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        text = "Go to Shopping Cart",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).finish()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        text = "Log Out",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        text = "Log In",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, SignUpActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if(isAdmin){
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, ManageArticlesActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Sage,
                        contentColor = Cream
                    )
                ) {
                    Text(
                        text = "Manage Articles",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    MyApplicationTheme {
       HomeScreen(LocalContext.current, modifier = Modifier)
    }
}