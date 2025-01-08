package com.example.ecommerce.ui.Article

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class ManageArticlesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Hello from ManageArticlesActivity! LINE 12")
        setContent {
            MaterialTheme {
                Surface {
                    println("Hello from ManageArticlesActivity! LINE 16")
                    ManageArticlesScreen()
                }
            }
        }
    }
}
