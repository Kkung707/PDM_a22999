package com.example.newsapp.data.repository

import android.util.Log
import com.example.newsapp.data.remote.api.NewsApi
import com.example.newsapp.domain.models.Article
import com.example.newsapp.domain.repository.NewsRepository

class NewsRepositoryImpl(private val api: NewsApi) : NewsRepository {
    override suspend fun getTopStories(apiKey: String): List<Article> {
        val response = api.getTopStories(apiKey)

        return response.results.map { result ->
            Log.d("Media", "Multimedia for article: ${result.title}")
            result.multimedia?.forEach { media ->
                Log.d("Media", "Format: ${media.format}, URL: ${media.url}")
            }

            val imageUrl = result.multimedia?.let { multimedia ->
                val prioritizedFormats =
                    listOf("thumbLarge", "Large Thumbnail", "Standard Thumbnail")
                prioritizedFormats.asSequence()
                    .mapNotNull { format -> multimedia.find { it.format == format }?.url }
                    .firstOrNull()
            }

            Log.d("ImageLoading", "Final Image URL: $imageUrl")

            Article(
                title = result.title,
                description = result.abstract,
                url = result.url,
                byline = result.byline,
                imageUrl = imageUrl
            )
        }
    }
}
