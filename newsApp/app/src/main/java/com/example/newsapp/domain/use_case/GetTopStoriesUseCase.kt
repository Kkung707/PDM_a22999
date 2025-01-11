package com.example.newsapp.domain.use_case

import com.example.newsapp.domain.models.Article
import com.example.newsapp.domain.repository.NewsRepository

class GetTopStoriesUseCase(private val repository: NewsRepository) {
    suspend operator fun invoke(apiKey: String): List<Article> {
        return repository.getTopStories(apiKey)
    }
}