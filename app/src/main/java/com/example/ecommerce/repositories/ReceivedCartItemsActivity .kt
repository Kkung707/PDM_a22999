package com.example.ecommerce.repositories

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.ecommerce.R
import com.example.ecommerce.models.CartItem
import com.example.ecommerce.ui.CharcoalLight
import com.example.ecommerce.ui.Cream
import com.example.ecommerce.ui.CreamDark
import com.example.ecommerce.ui.Ebony
import com.example.ecommerce.ui.Montserrat
import com.example.ecommerce.ui.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ReceivedCartItemsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        setContent {
            MyApplicationTheme {
                ReceivedCartItemsScreen(userId)
            }
        }
    }
}

private fun fetchUserCart(
    userId: String,
    onResult: (List<CartItem>) -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("Carts")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val cartDocument = querySnapshot.documents.first()
                val items = cartDocument.get("items") as? List<Map<String, Any>> ?: emptyList()
                val cartItems = items.map {
                    CartItem(
                        name = it["name"] as String,
                        price = (it["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (it["quantity"] as? Number)?.toInt() ?: 0,
                        imageUrl = it["imageUrl"] as String
                    )
                }
                onResult(cartItems)
            } else {
                onResult(emptyList())
            }
        }
        .addOnFailureListener { e ->
            onError("Error fetching cart: ${e.message}")
            Log.e("FetchCart", "Error fetching cart: ${e.message}")
        }
}

@Composable
fun ReceivedCartItemsScreen(userId: String) {
    val context = LocalContext.current
    var receivedCarts by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String>("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            fetchUserCart(
                userId = userId,
                onResult = { cartItems ->
                    receivedCarts = cartItems
                    isLoading = false
                },
                onError = { error ->
                    errorMessage = error
                    isLoading = false
                }
            )
        } else {
            errorMessage = "User ID is invalid"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .background(Ebony)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(35.dp))
        Text(
            text = "Your Cart",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            color = Cream,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat),
                    color = Cream
                )
            } else if (receivedCarts.isEmpty()) {
                Text(
                    text = "No items in your cart.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Cream
                )
            } else {
                LazyColumn {
                    items(receivedCarts) { cartItem ->
                        CartItemView(cartItem = cartItem)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemView(cartItem: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(cartItem.imageUrl),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.name, style = MaterialTheme.typography.bodyLarge, color = CreamDark)
            Text("$${cartItem.price} x ${cartItem.quantity}", style = MaterialTheme.typography.bodyMedium, color = CreamDark)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceivedCartItemsPreview() {
    MyApplicationTheme {
        ReceivedCartItemsScreen(userId = "testUserId")
    }
}
