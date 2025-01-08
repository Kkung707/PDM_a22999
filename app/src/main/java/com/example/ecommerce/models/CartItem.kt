package com.example.ecommerce.models

data class CartItem(
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)