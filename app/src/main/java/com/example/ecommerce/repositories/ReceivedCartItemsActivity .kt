package com.example.ecommerce.repositories

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecommerce.ui.MyApplicationTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.ecommerce.models.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class ReceivedCartItemsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        setContent {
            MyApplicationTheme {
                ReceivedCartItemsScreen(userId)
            }
        }
    }
}

@Composable
fun ReceivedCartItemsScreen(userId: String) {
    var receivedCarts by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String>("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("shared_carts")
                .whereArrayContains("sharedWith", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->

                    receivedCarts = querySnapshot.documents.flatMap { document ->
                        val items = document.get("items") as List<Map<String, Any>>
                        items.map {
                            CartItem(
                                name = it["name"] as String,
                                price = (it["price"] as? Number)?.toDouble() ?: 0.0, // Safely handle price
                                quantity = (it["quantity"] as? Number)?.toInt() ?: 0, // Safely handle quantity
                                imageUrl = it["imageUrl"] as String
                            )
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Failed to load cart items: ${exception.message}"
                    isLoading = false
                }
        } else {
            errorMessage = "User ID is invalid"
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Received Cart Items", style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (errorMessage.isNotEmpty()) {
                // display the error message her
                Text(errorMessage, style = MaterialTheme.typography.bodyLarge)
            } else if (receivedCarts.isEmpty()) {
                Text("No cart items shared with you.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn {
                    items(receivedCarts) { cartItem ->
                        CartItemViews(cartItem = cartItem)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemViews(cartItem: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(cartItem.imageUrl),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.name, style = MaterialTheme.typography.bodyMedium)
            Text("\$${cartItem.price} x ${cartItem.quantity}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}




@Preview(showBackground = true)
@Composable
fun ReceivedCartItemsPreview() {
    MyApplicationTheme {
        ReceivedCartItemsScreen(userId = "userId")
    }
}
