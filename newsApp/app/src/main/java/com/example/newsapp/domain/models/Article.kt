package com.example.newsapp.domain.models


data class Article(
    val title: String,
    val description: String,
    val url: String,
    val byline: String,
    val imageUrl: String?
)
