package com.example.newsapp.domain.repository

import com.example.newsapp.domain.models.Article


interface NewsRepository {
    suspend fun getTopStories(apiKey: String): List<Article>
}