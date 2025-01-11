package com.example.newsapp.ui.news_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.domain.models.Article
import com.example.newsapp.domain.use_case.GetTopStoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsListViewModel(private val getTopStoriesUseCase: GetTopStoriesUseCase) : ViewModel() {

    private val _newsList = MutableStateFlow<List<Article>>(emptyList())
    val newsList: StateFlow<List<Article>> = _newsList

    fun loadNews(apiKey: String) {
        viewModelScope.launch {
            _newsList.value = getTopStoriesUseCase(apiKey)
        }
    }
}