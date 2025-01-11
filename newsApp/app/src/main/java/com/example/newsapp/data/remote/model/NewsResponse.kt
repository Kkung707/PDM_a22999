package com.example.newsapp.data.remote.model

data class NewsResponse(
    val results: List<Article>
)

data class Article(
    val title: String,
    val abstract: String,
    val url: String,
    val byline: String,
    val multimedia: List<Multimedia>?
)

data class Multimedia(
    val url: String,
    val format: String
)