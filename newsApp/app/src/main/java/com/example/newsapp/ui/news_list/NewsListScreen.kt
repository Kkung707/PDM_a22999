package com.example.newsapp.ui.news_list

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsapp.domain.models.Article
import coil.compose.AsyncImage
import com.example.newsapp.ui.components.MyTopBar


@Composable
fun NewsListScreen(viewModel: NewsListViewModel, apiKey: String) {
    val title = "News"
    val newsList = viewModel.newsList.collectAsState().value

    Scaffold(topBar = {
        MyTopBar(title = title)
    }) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(newsList) { article ->
                NewsItem(article)
            }
        }
    }
    viewModel.loadNews(apiKey)
}

@Composable
fun NewsItem(article: Article) {

    Log.d("ImageLoading", "Loading image: ${article.imageUrl}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 5.dp, 30.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Color(0xFF414A4C)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = article.imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Article Image",
                modifier = Modifier.fillMaxSize() // Ensures the image fills the Box
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = article.title,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp)) // Space between title and byline
            Text(
                text = article.byline,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}