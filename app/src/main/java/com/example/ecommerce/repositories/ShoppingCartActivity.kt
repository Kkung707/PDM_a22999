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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.ecommerce.models.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.LinkedList

class ShoppingCartActivity : ComponentActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingCartScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShoppingCartScreen() {
        // 1) We store cart items in a LinkedList, wrapped in a mutableStateOf
        val cartItemsState = remember { mutableStateOf(LinkedList<CartItem>()) }
        // For convenience:
        val cartItems: LinkedList<CartItem> = cartItemsState.value

        // 2) Store the list of all items from Firestore's "Articles" collection
        val allItemsState = remember { mutableStateOf<List<CartItem>>(emptyList()) }

        // 3) Track whether the cart bottom sheet is open
        var isCartOpen by remember { mutableStateOf(false) }

        // 4) For user-sharing logic: fetch user emails from the "User" collection
        var usersList by remember { mutableStateOf<List<String>>(emptyList()) }
        var receiverEmail by remember { mutableStateOf("") }

        // Load articles from "Articles"
        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance().collection("Articles")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val fetched = snapshot.mapNotNull { doc ->
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val price = doc.getDouble("price") ?: 0.0
                            val qtyInStock = doc.getLong("quantityInStock")?.toInt() ?: 0
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            // Convert Firestore fields to CartItem
                            CartItem(name, price, qtyInStock, imageUrl)
                        }
                        allItemsState.value = fetched
                    } else {
                        Log.d("ShoppingCart", "No articles found in Firestore (Articles).")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ShoppingCart", "Error fetching from 'Articles': $e")
                }
        }

        // Load user emails from "User"
        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance().collection("User")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        usersList = snapshot.mapNotNull { doc ->
                            doc.getString("email")
                        }
                        Log.d("ShoppingCart", "Fetched user emails: $usersList")
                    } else {
                        Log.d("ShoppingCart", "No users in 'User' collection.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ShoppingCart", "Error fetching users: $e")
                }
        }

        // Main UI layout
        Box(modifier = Modifier.fillMaxSize()) {
            // The "shop" content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(text = "Articles for Sale", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))

                // Show list of all items (articles)
                LazyColumn {
                    items(allItemsState.value) { article ->
                        // For each article, we show an "Add to Cart" button
                        ArticleItem(
                            article = article,
                            onAddToCart = {
                                // 5) To add an item to a LinkedList and get recomposition:
                                // Create a new list from the old one, mutate it, then reassign
                                val newList = LinkedList(cartItems)
                                newList.add(article)
                                cartItemsState.value = newList

                                Toast.makeText(
                                    this@ShoppingCartActivity,
                                    "${article.name} added to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            // A FloatingActionButton to open the cart sheet
            FloatingActionButton(
                onClick = { isCartOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Cart")
            }

            // If cart is open, show bottom sheet with cart
            if (isCartOpen) {
                ModalBottomSheet(
                    onDismissRequest = { isCartOpen = false }
                ) {
                    // 6) Pass the linked list to the bottom sheet
                    CartBottomSheetContent(
                        cartItems = cartItems,           // the LinkedList
                        receiverEmail = receiverEmail,
                        usersList = usersList,
                        onSelectEmail = { receiverEmail = it },
                        onShareCart = { items, recvEmail ->
                            if (recvEmail.isNotBlank()) {
                                shareCart(items, recvEmail)
                            } else {
                                Toast.makeText(
                                    this@ShoppingCartActivity,
                                    "Pick a user to share with",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onClose = { isCartOpen = false }
                    )
                }
            }
        }
    }

    // The bottom sheet content
    @Composable
    fun CartBottomSheetContent(
        cartItems: LinkedList<CartItem>,
        receiverEmail: String,
        usersList: List<String>,
        onSelectEmail: (String) -> Unit,
        onShareCart: (List<CartItem>, String) -> Unit,
        onClose: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Cart", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // If empty, show message
            if (cartItems.isEmpty()) {
                Text("Cart is empty.")
            } else {
                // Otherwise, show them
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    // Convert the LinkedList to a List for LazyColumn
                    items(cartItems.toList()) { cartItem ->
                        CartItemView(cartItem = cartItem)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pick a user to share with:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // List of user emails
            LazyColumn(
                modifier = Modifier.heightIn(max = 150.dp)
            ) {
                items(usersList) { email ->
                    TextButton(
                        onClick = {
                            onSelectEmail(email)
                            Toast.makeText(
                                this@ShoppingCartActivity,
                                "$email selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text(email)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onShareCart(cartItems.toList(), receiverEmail) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share Cart with $receiverEmail")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { onClose() }, modifier = Modifier.fillMaxWidth()) {
                Text("Close Cart")
            }
        }
    }

    // Single article display
    @Composable
    fun ArticleItem(article: CartItem, onAddToCart: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(article.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(article.name, style = MaterialTheme.typography.bodyLarge)
                Text("Price: ${article.price}", style = MaterialTheme.typography.bodyMedium)
                Text("Stock: ${article.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onAddToCart) {
                Text("Add to Cart")
            }
        }
    }

    // CartItem display
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
                Text(cartItem.name, style = MaterialTheme.typography.bodyMedium)
                Text("Price: ${cartItem.price}", style = MaterialTheme.typography.bodySmall)
                Text("Qty: ${cartItem.quantity}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    /**
     * Store a doc in "SharedCarts" with fields that match your DB structure.
     * E.g.: CartId, ReceivedUserId, SharedUserId, etc.
     */
    private fun shareCart(cartItems: List<CartItem>, receiverEmail: String) {
        val db = FirebaseFirestore.getInstance()

        // Must have a current user logged in
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        if (currentUserId.isBlank()) {
            Toast.makeText(this, "You must be logged in to share a cart", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the receiver's doc in "User" by email
        db.collection("User")
            .whereEqualTo("email", receiverEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No user found with $receiverEmail", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val receiverUserId = snapshot.documents.first().id
                Log.d("ShoppingCart", "receiverUserId = $receiverUserId")

                // Sample doc in "SharedCarts" with fields CartId, ReceivedUserId, SharedUserId, etc.
                val cartData = hashMapOf(
                    "CartId" to "someCartId",
                    "ReceivedUserId" to receiverUserId,
                    "SharedUserId" to currentUserId
                    // You can also store the actual items if you want
                )

                db.collection("SharedCarts").add(cartData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cart shared successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding doc to SharedCarts: $e", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error searching for $receiverEmail: $e", Toast.LENGTH_SHORT).show()
            }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewShoppingCartScreen() {
        ShoppingCartScreen()
    }
}
