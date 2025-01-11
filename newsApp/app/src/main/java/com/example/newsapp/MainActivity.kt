package com.example.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.newsapp.data.remote.api.DependencyInjection
import com.example.newsapp.ui.NewsAppTheme
import com.example.newsapp.ui.news_list.NewsListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = DependencyInjection.provideNewsListViewModel()

        setContent {
            NewsListScreen(viewModel, "GDydeKCYBTW7Ul88IlfD1vATj33oRP1s")
        }
    }
}

