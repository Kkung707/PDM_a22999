package com.example.newsapp.data.remote.api

import com.example.newsapp.data.remote.model.NewsResponse
import com.example.newsapp.data.repository.NewsRepositoryImpl
import com.example.newsapp.domain.use_case.GetTopStoriesUseCase
import com.example.newsapp.ui.news_list.NewsListViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("topstories/v2/home.json")
    suspend fun getTopStories(
        @Query("api-key") apiKey: String
    ): NewsResponse
}

object DependencyInjection {

    private const val BASE_URL = "https://api.nytimes.com/svc/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: NewsApi = retrofit.create(NewsApi::class.java)
    private val repository = NewsRepositoryImpl(api)
    private val getTopStoriesUseCase = GetTopStoriesUseCase(repository)

    fun provideNewsListViewModel(): NewsListViewModel {
        return NewsListViewModel(getTopStoriesUseCase)
    }
}