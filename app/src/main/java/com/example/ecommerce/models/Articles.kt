package com.example.ecommerce.models



data class Article(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val profileImage: String = ""
)

enum class UserRole {
    ADMIN, CUSTOMER
}



data class ShoppingCart(
    val items: MutableList<CartItem> = mutableListOf()
)
